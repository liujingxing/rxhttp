package com.rxhttp.compiler.common

import com.rxhttp.compiler.getClassPath
import com.rxhttp.compiler.rxHttpPackage

/**
 * User: ljx
 * Date: 2022/9/22
 * Time: 14:59
 */
fun getObservableClass(): Map<String, String> {
    val map = mutableMapOf<String, String>()
    map["ObservableCall"] = """
        package $rxHttpPackage;

        import androidx.annotation.NonNull;

        import java.io.IOException;
        import java.util.Objects;
        import java.util.concurrent.atomic.AtomicReference;

        import ${getClassPath("AndroidSchedulers")};
        import ${getClassPath("Observable")};
        import ${getClassPath("Observer")};
        import ${getClassPath("Scheduler")};
        import ${getClassPath("Disposable")};
        import ${getClassPath("Exceptions")};
        import ${getClassPath("Consumer")};
        import ${getClassPath("DisposableHelper")};
        import ${getClassPath("RxJavaPlugins")};
        import ${getClassPath("Schedulers")};
        import okhttp3.Call;
        import okhttp3.Callback;
        import okhttp3.Response;
        import rxhttp.wrapper.BodyParamFactory;
        import rxhttp.wrapper.CallFactory;
        import rxhttp.wrapper.callback.ProgressCallback;
        import rxhttp.wrapper.entity.Progress;
        import rxhttp.wrapper.parse.Parser;
        import rxhttp.wrapper.parse.StreamParser;
        import rxhttp.wrapper.utils.LogUtil;
        
        /**
         * User: ljx
         * Date: 2020/9/5
         * Time: 21:59
         */
        public final class ObservableCall<T> extends Observable<T> {

            private final Parser<T> parser;
            private final CallFactory callFactory;
            private boolean syncRequest = false;
            private boolean callbackProgress = false;
            private Runnable onSubscribe;

            public ObservableCall(CallFactory callFactory, Parser<T> parser) {
                this.callFactory = callFactory;
                this.parser = parser;
            }

            @Override
            protected void subscribeActual(Observer<? super T> observer) {
                CallExecuteDisposable d = syncRequest ? new CallExecuteDisposable(observer, callFactory, parser) :
                    new CallEnqueueDisposable(observer, callFactory, parser);
                observer.onSubscribe(d);
                if (d.isDisposed()) {
                    return;
                }
                if (callbackProgress && observer instanceof ProgressCallback) {
                    ProgressCallback pc = (ProgressCallback) observer;
                    if (parser instanceof StreamParser) {
                        ((StreamParser) parser).setProgressCallback(pc);
                    } else if (callFactory instanceof BodyParamFactory) {
                        ((BodyParamFactory) callFactory).getParam().setProgressCallback(pc);
                    }
                }
                if (syncRequest || onSubscribe == null) {
                    //must be in IO Thread if syncRequest is true
                    if (onSubscribe != null) onSubscribe.run();
                    d.run();
                } else {
                    d.setDisposable(Schedulers.io().scheduleDirect(new Runnable() {
                        @Override
                        public void run() {
                            // In IO Thread
                            onSubscribe.run();
                            d.run();
                        }
                    }));
                }
            }

            ObservableCall<T> onSubscribe(Runnable onSubscribe) {
                this.onSubscribe = onSubscribe;
                return this;
            }

            public ObservableCall<T> syncRequest() {
                syncRequest = true;
                return this;
            }

            public Observable<T> onProgress(Consumer<Progress> progressConsumer) {
                return onProgress(Schedulers.io(), progressConsumer);
            }

            public Observable<T> onProgress(Scheduler scheduler, Consumer<Progress> progressConsumer) {
                return onProgress(2, scheduler, progressConsumer);
            }

            public Observable<T> onMainProgress(Consumer<Progress> progressConsumer) {
                return onMainProgress(2, progressConsumer);
            }
            
            public Observable<T> onMainProgress(int capacity, Consumer<Progress> progressConsumer) {
                return onProgress(capacity, AndroidSchedulers.mainThread(), progressConsumer);
            }
            
            /**
             * Upload or Download progress callback
             *
             * @param capacity         queue size, must be in [2..100], is invalid when the scheduler is TrampolineScheduler
             * @param scheduler        the Scheduler to notify Observers on
             * @param progressConsumer progress callback
             * @return the new Observable instance
             */
            public Observable<T> onProgress(int capacity, @NonNull Scheduler scheduler, Consumer<Progress> progressConsumer) {
                if (capacity < 2 || capacity > 100) {
                    throw new IllegalArgumentException("capacity must be in [2..100], but it was " + capacity);
                }
                Objects.requireNonNull(scheduler, "scheduler is null");
                if (!(parser instanceof StreamParser) && !(callFactory instanceof BodyParamFactory)) {
                    throw new UnsupportedOperationException("parser is " + parser.getClass().getSimpleName() + ", callFactory is " + callFactory.getClass().getSimpleName());
                }
                callbackProgress = true;
                return new ObservableProgress(this, capacity, scheduler, progressConsumer);
            }

            private static class CallEnqueueDisposable<T> extends CallExecuteDisposable<T> implements Callback {

                CallEnqueueDisposable(Observer<? super T> downstream, CallFactory callFactory, Parser<T> parser) {
                    super(downstream, callFactory, parser);
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    try {
                        T t = Objects.requireNonNull(parser.onParse(response), "The onParse function returned a null value.");
                        if (!disposed) {
                            downstream.onNext(t);
                        }
                        if (!disposed) {
                            downstream.onComplete();
                        }
                    } catch (Throwable t) {
                        onError(call, t);
                        return;
                    }
                }

                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    onError(call, e);
                }

                @Override
                public void run() {
                    call = callFactory.newCall();
                    call.enqueue(this);
                }
            }


            private static class CallExecuteDisposable<T> implements Disposable {

                protected final Observer<? super T> downstream;
                protected final Parser<T> parser;
                protected final CallFactory callFactory;
                protected volatile boolean disposed;
                protected Call call;
                private AtomicReference<Disposable> upstream;

                CallExecuteDisposable(Observer<? super T> downstream, CallFactory callFactory, Parser<T> parser) {
                    this.downstream = downstream;
                    this.callFactory = callFactory;
                    this.parser = parser;
                    upstream = new AtomicReference<>();
                }

                public void run() {
                    call = callFactory.newCall();
                    try {
                        Response response = call.execute();
                        T t = Objects.requireNonNull(parser.onParse(response), "The onParse function returned a null value.");
                        if (!disposed) {
                            downstream.onNext(t);
                        }
                        if (!disposed) {
                            downstream.onComplete();
                        }
                    } catch (Throwable e) {
                        onError(call, e);
                        return;
                    }
                }

                void onError(Call call, Throwable e) {
                    LogUtil.log(call.request().url().toString(), e);
                    Exceptions.throwIfFatal(e);
                    if (!disposed) {
                        downstream.onError(e);
                    } else {
                        RxJavaPlugins.onError(e);
                    }
                }

                @Override
                public void dispose() {
                    DisposableHelper.dispose(upstream);
                    disposed = true;
                    call.cancel();
                }

                @Override
                public boolean isDisposed() {
                    return disposed;
                }

                public void setDisposable(Disposable d) {
                    DisposableHelper.setOnce(upstream, d);
                }
            }
        }

    """.trimIndent()

    map["ObservableProgress"] ="""
        package $rxHttpPackage;

        import java.util.Queue;
        import java.util.concurrent.LinkedBlockingQueue;
        import java.util.concurrent.atomic.AtomicInteger;

        import ${getClassPath("Observable")};
        import ${getClassPath("Observer")};
        import ${getClassPath("Scheduler")};
        import ${getClassPath("Scheduler")}.Worker;
        import ${getClassPath("Disposable")};
        import ${getClassPath("Exceptions")};
        import ${getClassPath("Consumer")};
        import ${getClassPath("DisposableHelper")};
        import ${getClassPath("TrampolineScheduler")};
        import ${getClassPath("RxJavaPlugins")};
        import rxhttp.wrapper.callback.ProgressCallback;
        import rxhttp.wrapper.entity.Progress;

        public final class ObservableProgress<T> extends Observable<T> {

            private final Observable<T> source;
            private final int capacity;
            private final Scheduler scheduler;
            private final Consumer<Progress> progressConsumer;

            ObservableProgress(Observable<T> source, int capacity, Scheduler scheduler, Consumer<Progress> progressConsumer) {
                this.source = source;
                this.capacity = capacity;
                this.scheduler = scheduler;
                this.progressConsumer = progressConsumer;
            }

            @Override
            protected void subscribeActual(Observer<? super T> observer) {
                if (scheduler instanceof TrampolineScheduler) {
                    source.subscribe(new SyncObserver<>(observer, progressConsumer));
                } else {
                    Worker worker = scheduler.createWorker();
                    source.subscribe(new AsyncObserver<>(worker, observer, capacity, progressConsumer));
                }
            }

            private static final class SyncObserver<T> implements Observer<T>, Disposable, ProgressCallback {

                private final Observer<? super T> downstream;
                private final Consumer<Progress> progressConsumer;
                private Disposable upstream;
                private boolean done;

                SyncObserver(Observer<? super T> actual, Consumer<Progress> progressConsumer) {
                    this.downstream = actual;
                    this.progressConsumer = progressConsumer;
                }

                @Override
                public void onSubscribe(Disposable d) {
                    if (DisposableHelper.validate(this.upstream, d)) {
                        this.upstream = d;
                        downstream.onSubscribe(this);
                    }
                }

                // upload/download progress callback
                @Override
                public void onProgress(int progress, long currentSize, long totalSize) {
                    if (done) {
                        return;
                    }
                    try {
                        progressConsumer.accept(new Progress(progress, currentSize, totalSize));
                    } catch (Throwable t) {
                        fail(t);
                    }
                }

                @Override
                public void onNext(T t) {
                    if (done) {
                        return;
                    }
                    downstream.onNext(t);
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


            private static final class AsyncObserver<T> extends AtomicInteger implements Observer<T>,
                Disposable, ProgressCallback, Runnable {

                private final Observer<? super T> downstream;
                private final Queue<Object> queue;
                private final Scheduler.Worker worker;
                private final Consumer<Progress> progressConsumer;
                private Disposable upstream;
                private Throwable error;
                private volatile boolean done;
                private volatile boolean disposed;

                AsyncObserver(Scheduler.Worker worker, Observer<? super T> actual, int capacity, Consumer<Progress> progressConsumer) {
                    this.downstream = actual;
                    this.worker = worker;
                    this.progressConsumer = progressConsumer;
                    queue = new LinkedBlockingQueue<>(capacity);
                }

                @Override
                public void onSubscribe(Disposable d) {
                    if (DisposableHelper.validate(this.upstream, d)) {
                        this.upstream = d;
                        downstream.onSubscribe(this);
                    }
                }

                // upload/download progress callback
                @Override
                public void onProgress(int progress, long currentSize, long totalSize) {
                    if (done) {
                        return;
                    }
                    offer(new Progress(progress, currentSize, totalSize));
                }

                @Override
                public void onNext(T t) {
                    if (done) {
                        return;
                    }
                    offer(t);
                }

                private void offer(Object o) {
                    while (!queue.offer(o)) {
                        queue.poll();
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

                    final Queue<?> q = queue;
                    final Observer<? super T> a = downstream;
                    while (!checkTerminated(done, q.isEmpty(), a)) {
                        for (; ; ) {
                            boolean d = done;
                            Object o;
                            try {
                                o = q.poll();

                                boolean empty = o == null;

                                if (checkTerminated(d, empty, a)) {
                                    return;
                                }
                                if (empty) {
                                    break;
                                }
                                if (o instanceof Progress) {
                                    progressConsumer.accept((Progress) o);
                                } else {
                                    a.onNext((T) o);
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

    """.trimIndent()
    return map;
}