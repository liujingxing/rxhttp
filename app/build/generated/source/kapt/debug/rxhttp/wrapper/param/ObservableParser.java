package rxhttp.wrapper.param;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableSource;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.core.Scheduler;
import io.reactivex.rxjava3.core.Scheduler.Worker;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.exceptions.Exceptions;
import io.reactivex.rxjava3.functions.Consumer;
import io.reactivex.rxjava3.internal.disposables.DisposableHelper;
import io.reactivex.rxjava3.operators.SpscArrayQueue;
import io.reactivex.rxjava3.plugins.RxJavaPlugins;
import okhttp3.Response;
import rxhttp.wrapper.annotations.NonNull;
import rxhttp.wrapper.annotations.Nullable;
import rxhttp.wrapper.callback.ProgressCallback;
import rxhttp.wrapper.entity.Progress;
import rxhttp.wrapper.entity.ProgressT;
import rxhttp.wrapper.parse.Parser;
import rxhttp.wrapper.parse.StreamParser;
import rxhttp.wrapper.utils.LogUtil;

final class ObservableParser<T> extends Observable<T> {

    private final Parser<T> parser;
    private final ObservableSource<Progress> source;
    private final Scheduler scheduler;
    private final Consumer<Progress> progressConsumer;

    ObservableParser(@NonNull ObservableSource<Progress> source, @NonNull Parser<T> parser,
                            @Nullable Scheduler scheduler, @Nullable Consumer<Progress> progressConsumer) {
        this.source = source;
        this.parser = parser;
        this.scheduler = scheduler;
        this.progressConsumer = progressConsumer;
    }

    @Override
    protected void subscribeActual(@NonNull Observer<? super T> observer) {
        if (scheduler == null) {
            source.subscribe(new SyncParserObserver<>(observer, parser, progressConsumer));
        } else {
            Worker worker = scheduler.createWorker();
            source.subscribe(new AsyncParserObserver<>(observer, worker, progressConsumer, parser));
        }
    }

    private static final class SyncParserObserver<T> implements Observer<Progress>, Disposable, ProgressCallback {
        private final Parser<T> parser;

        private Disposable upstream;
        private final Observer<? super T> downstream;
        private final Consumer<Progress> progressConsumer;
        private boolean done;

        SyncParserObserver(Observer<? super T> actual, Parser<T> parser, Consumer<Progress> progressConsumer) {
            this.downstream = actual;
            this.parser = parser;
            this.progressConsumer = progressConsumer;

            if (progressConsumer != null && parser instanceof StreamParser) {
                ((StreamParser) parser).setProgressCallback(this);
            }
        }

        @Override
        public void onSubscribe(Disposable d) {
            if (DisposableHelper.validate(this.upstream, d)) {
                this.upstream = d;
                downstream.onSubscribe(this);
            }
        }

        //download progress callback
        @Override
        public void onProgress(int progress, long currentSize, long totalSize) {
            if (done) {
                return;
            }
            try {
                LogUtil.logDownProgress(progress, currentSize, totalSize);
                progressConsumer.accept(new Progress(progress, currentSize, totalSize));
            } catch (Throwable t) {
                fail(t);
            }
        }

        @SuppressWarnings("unchecked")
        @Override
        public void onNext(Progress progress) {
            if (done) {
                return;
            }
            if (progress instanceof ProgressT) {
                ProgressT<Response> p = (ProgressT<Response>) progress;
                T v;
                try {
                    v = Objects.requireNonNull(parser.onParse(p.getResult()), "The onParse function returned a null value.");
                } catch (Throwable t) {
                    LogUtil.log(p.getResult().request().url().toString(), t);
                    fail(t);
                    return;
                }
                downstream.onNext(v);
            } else {
                try {
                    progressConsumer.accept(progress);
                } catch (Throwable t) {
                    fail(t);
                }
            }
        }

        @Override
        public void onError(Throwable t) {
            if (done) {
                RxJavaPlugins.onError(t);
                return;
            }
            done = true;
            downstream.onError(t);
        }

        @Override
        public void onComplete() {
            if (done) {
                return;
            }
            done = true;
            downstream.onComplete();
        }

        @Override
        public void dispose() {
            upstream.dispose();
        }

        @Override
        public boolean isDisposed() {
            return upstream.isDisposed();
        }

        private void fail(Throwable t) {
            Exceptions.throwIfFatal(t);
            upstream.dispose();
            onError(t);
        }
    }


    private static final class AsyncParserObserver<T> extends AtomicInteger
        implements Observer<Progress>, Disposable, ProgressCallback, Runnable {

        private final Parser<T> parser;
        private final Observer<? super T> downstream;

        private Disposable upstream;
        private Throwable error;

        private volatile boolean done;
        private volatile boolean disposed;
        private final SpscArrayQueue<Progress> queue;
        private final Scheduler.Worker worker;

        private final Consumer<Progress> progressConsumer;

        AsyncParserObserver(Observer<? super T> actual, Scheduler.Worker worker, Consumer<Progress> progressConsumer, Parser<T> parser) {
            this.downstream = actual;
            this.parser = parser;
            this.worker = worker;
            this.progressConsumer = progressConsumer;
            queue = new SpscArrayQueue<>(2);

            if (progressConsumer != null && parser instanceof StreamParser) {
                ((StreamParser) parser).setProgressCallback(this);
            }
        }

        @Override
        public void onSubscribe(@NonNull Disposable d) {
            if (DisposableHelper.validate(this.upstream, d)) {
                this.upstream = d;
                downstream.onSubscribe(this);
            }
        }

        //download progress callback
        @Override
        public void onProgress(int progress, long currentSize, long totalSize) {
            if (done) {
                return;
            }
            LogUtil.logDownProgress(progress, currentSize, totalSize);
            offer(new Progress(progress,currentSize,totalSize));
        }

        @SuppressWarnings("unchecked")
        @Override
        public void onNext(Progress progress) {
            if (done) {
                return;
            }
            ProgressT<T> pt = null;
            if (progress instanceof ProgressT) {
                ProgressT<Response> progressT = (ProgressT<Response>) progress;
                try {
                    T t = Objects.requireNonNull(parser.onParse(progressT.getResult()), "The onParse function returned a null value.");
                    pt = new ProgressT<>(t);
                } catch (Throwable t) {
                    LogUtil.log(progressT.getResult().request().url().toString(), t);
                    onError(t);
                    return;
                }
            }
            Progress p = pt != null ? pt : progress;
            offer(p);
        }
        
        private void offer(Progress p) {
            if (!queue.offer(p)) {
                queue.poll();
                queue.offer(p);
            }
            schedule();
        }

        @Override
        public void onError(Throwable t) {
            if (done) {
                RxJavaPlugins.onError(t);
                return;
            }
            error = t;
            done = true;
            schedule();
        }

        @Override
        public void onComplete() {
            if (done) {
                return;
            }
            done = true;
            schedule();
        }


        void schedule() {
            if (getAndIncrement() == 0) {
                worker.schedule(this);
            }
        }

        @SuppressWarnings("unchecked")
        @Override
        public void run() {
            int missed = 1;

            final SpscArrayQueue<Progress> q = queue;
            final Observer<? super T> a = downstream;
            while (!checkTerminated(done, q.isEmpty(), a)) {
                for (; ; ) {
                    boolean d = done;
                    Progress p;
                    try {
                        p = q.poll();

                        boolean empty = p == null;

                        if (checkTerminated(d, empty, a)) {
                            return;
                        }
                        if (empty) {
                            break;
                        }
                        if (p instanceof ProgressT) {
                            a.onNext(((ProgressT<T>) p).getResult());
                        } else {
                            progressConsumer.accept(p);
                        }
                    } catch (Throwable ex) {
                        Exceptions.throwIfFatal(ex);
                        disposed = true;
                        upstream.dispose();
                        q.clear();
                        a.onError(ex);
                        worker.dispose();
                        return;
                    }
                }
                missed = addAndGet(-missed);
                if (missed == 0) {
                    break;
                }
            }
        }

        boolean checkTerminated(boolean d, boolean empty, Observer<? super T> a) {
            if (isDisposed()) {
                queue.clear();
                return true;
            }
            if (d) {
                Throwable e = error;
                if (e != null) {
                    disposed = true;
                    queue.clear();
                    a.onError(e);
                    worker.dispose();
                    return true;
                } else if (empty) {
                    disposed = true;
                    a.onComplete();
                    worker.dispose();
                    return true;
                }
            }
            return false;
        }

        @Override
        public void dispose() {
            if (!disposed) {
                disposed = true;
                upstream.dispose();
                worker.dispose();
                if (getAndIncrement() == 0) {
                    queue.clear();
                }
            }
        }

        @Override
        public boolean isDisposed() {
            return disposed;
        }
    }
}
