/**
 * Copyright (c) 2016-present, RxJava Contributors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See
 * the License for the specific language governing permissions and limitations under the License.
 */
package rxhttp;


import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.Observer;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.exceptions.Exceptions;
import io.reactivex.functions.Cancellable;
import io.reactivex.internal.disposables.CancellableDisposable;
import io.reactivex.internal.disposables.DisposableHelper;
import io.reactivex.internal.fuseable.SimpleQueue;
import io.reactivex.internal.queue.SpscLinkedArrayQueue;
import io.reactivex.internal.util.AtomicThrowable;
import io.reactivex.plugins.RxJavaPlugins;
import okhttp3.Call;
import okhttp3.Request;
import okhttp3.Response;
import rxhttp.wrapper.callback.ProgressCallback;
import rxhttp.wrapper.entity.Progress;
import rxhttp.wrapper.entity.ProgressT;
import rxhttp.wrapper.param.Param;
import rxhttp.wrapper.parse.DownloadParser;
import rxhttp.wrapper.utils.LogUtil;

public final class ObservableDownload extends Observable<Progress> {
    private final Param param;
    private final String destPath;
    private final long offsetSize;

    private Call mCall;
    private Request mRequest;

    private int lastProgress; //上次下载进度

    ObservableDownload(Param param, final String destPath) {
        this(param, destPath, 0);
    }

    ObservableDownload(Param param, String destPath, long offsetSize) {
        this.param = param;
        this.destPath = destPath;
        this.offsetSize = offsetSize;
    }

    @Override
    protected void subscribeActual(Observer<? super Progress> observer) {
        CreateEmitter<Progress> emitter = new CreateEmitter<Progress>(observer) {
            @Override
            public void dispose() {
                cancelRequest(mCall);
                super.dispose();
            }
        };
        observer.onSubscribe(emitter);

        try {
            ProgressT<String> completeProgress = new ProgressT<>();  //下载完成回调
            Response response = execute(param, (progress, currentSize, totalSize) -> {
                //这里最多回调100次,仅在进度有更新时,才会回调
                Progress p = new Progress(progress, currentSize, totalSize);
                if (offsetSize > 0) {
                    p.addCurrentSize(offsetSize);
                    p.addTotalSize(offsetSize);
                    p.updateProgress();
                    int currentProgress = p.getProgress();
                    if (currentProgress <= lastProgress) return;
                    lastProgress = currentProgress;
                }
                if (p.isFinish()) {
                    //下载完成的回调，需要带上本地存储路径，故这里先保存进度
                    completeProgress.set(p);
                } else {
                    emitter.onNext(p);
                }
            });
            String filePath = new DownloadParser(destPath).onParse(response);
            completeProgress.setResult(filePath);
            emitter.onNext(completeProgress); //最后一次回调文件下载路径
            emitter.onComplete();
        } catch (Throwable e) {
            LogUtil.log(param.getUrl(), e);
            Exceptions.throwIfFatal(e);
            emitter.onError(e);
        }
    }

    private Response execute(@NonNull Param param, @NonNull ProgressCallback callback) throws Exception {
        if (mRequest == null) { //防止失败重试时，重复构造okhttp3.Request对象
            mRequest = param.buildRequest();
        }
        Call call = mCall = HttpSender.newCall(HttpSender.clone(callback), mRequest);
        return call.execute();
    }

    //关闭请求
    private void cancelRequest(Call call) {
        if (call != null && !call.isCanceled())
            call.cancel();
    }

    static class CreateEmitter<T>
        extends AtomicReference<Disposable>
        implements ObservableEmitter<T>, Disposable {

        private static final long serialVersionUID = -3434801548987643227L;

        final Observer<? super T> observer;

        CreateEmitter(Observer<? super T> observer) {
            this.observer = observer;
        }

        @Override
        public void onNext(T t) {
            if (t == null) {
                onError(new NullPointerException("onNext called with null. Null values are generally not allowed in 2.x operators and sources."));
                return;
            }
            if (!isDisposed()) {
                observer.onNext(t);
            }
        }

        @Override
        public void onError(Throwable t) {
            if (!tryOnError(t)) {
                RxJavaPlugins.onError(t);
            }
        }

        @Override
        public boolean tryOnError(Throwable t) {
            if (t == null) {
                t = new NullPointerException("onError called with null. Null values are generally not allowed in 2.x operators and sources.");
            }
            if (!isDisposed()) {
                try {
                    observer.onError(t);
                } finally {
                    dispose();
                }
                return true;
            }
            return false;
        }

        @Override
        public void onComplete() {
            if (!isDisposed()) {
                try {
                    observer.onComplete();
                } finally {
                    dispose();
                }
            }
        }

        @Override
        public void setDisposable(Disposable d) {
            DisposableHelper.set(this, d);
        }

        @Override
        public void setCancellable(Cancellable c) {
            setDisposable(new CancellableDisposable(c));
        }

        @Override
        public ObservableEmitter<T> serialize() {
            return new SerializedEmitter<T>(this);
        }

        @Override
        public void dispose() {
            DisposableHelper.dispose(this);
        }

        @Override
        public boolean isDisposed() {
            return DisposableHelper.isDisposed(get());
        }

        @Override
        public String toString() {
            return String.format("%s{%s}", getClass().getSimpleName(), super.toString());
        }
    }

    /**
     * Serializes calls to onNext, onError and onComplete.
     *
     * @param <T> the value type
     */
    static final class SerializedEmitter<T>
        extends AtomicInteger
        implements ObservableEmitter<T> {

        private static final long serialVersionUID = 4883307006032401862L;

        final ObservableEmitter<T> emitter;

        final AtomicThrowable error;

        final SpscLinkedArrayQueue<T> queue;

        volatile boolean done;

        SerializedEmitter(ObservableEmitter<T> emitter) {
            this.emitter = emitter;
            this.error = new AtomicThrowable();
            this.queue = new SpscLinkedArrayQueue<T>(16);
        }

        @Override
        public void onNext(T t) {
            if (emitter.isDisposed() || done) {
                return;
            }
            if (t == null) {
                onError(new NullPointerException("onNext called with null. Null values are generally not allowed in 2.x operators and sources."));
                return;
            }
            if (get() == 0 && compareAndSet(0, 1)) {
                emitter.onNext(t);
                if (decrementAndGet() == 0) {
                    return;
                }
            } else {
                SimpleQueue<T> q = queue;
                synchronized (q) {
                    q.offer(t);
                }
                if (getAndIncrement() != 0) {
                    return;
                }
            }
            drainLoop();
        }

        @Override
        public void onError(Throwable t) {
            if (!tryOnError(t)) {
                RxJavaPlugins.onError(t);
            }
        }

        @Override
        public boolean tryOnError(Throwable t) {
            if (emitter.isDisposed() || done) {
                return false;
            }
            if (t == null) {
                t = new NullPointerException("onError called with null. Null values are generally not allowed in 2.x operators and sources.");
            }
            if (error.addThrowable(t)) {
                done = true;
                drain();
                return true;
            }
            return false;
        }

        @Override
        public void onComplete() {
            if (emitter.isDisposed() || done) {
                return;
            }
            done = true;
            drain();
        }

        void drain() {
            if (getAndIncrement() == 0) {
                drainLoop();
            }
        }

        void drainLoop() {
            ObservableEmitter<T> e = emitter;
            SpscLinkedArrayQueue<T> q = queue;
            AtomicThrowable error = this.error;
            int missed = 1;
            for (; ; ) {

                for (; ; ) {
                    if (e.isDisposed()) {
                        q.clear();
                        return;
                    }

                    if (error.get() != null) {
                        q.clear();
                        e.onError(error.terminate());
                        return;
                    }

                    boolean d = done;
                    T v = q.poll();

                    boolean empty = v == null;

                    if (d && empty) {
                        e.onComplete();
                        return;
                    }

                    if (empty) {
                        break;
                    }

                    e.onNext(v);
                }

                missed = addAndGet(-missed);
                if (missed == 0) {
                    break;
                }
            }
        }

        @Override
        public void setDisposable(Disposable d) {
            emitter.setDisposable(d);
        }

        @Override
        public void setCancellable(Cancellable c) {
            emitter.setCancellable(c);
        }

        @Override
        public boolean isDisposed() {
            return emitter.isDisposed();
        }

        @Override
        public ObservableEmitter<T> serialize() {
            return this;
        }

        @Override
        public String toString() {
            return emitter.toString();
        }
    }

}
