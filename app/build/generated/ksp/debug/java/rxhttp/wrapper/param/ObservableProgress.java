package rxhttp.wrapper.param;

import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.core.Scheduler;
import io.reactivex.rxjava3.core.Scheduler.Worker;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.exceptions.Exceptions;
import io.reactivex.rxjava3.functions.Consumer;
import io.reactivex.rxjava3.internal.disposables.DisposableHelper;
import io.reactivex.rxjava3.internal.schedulers.TrampolineScheduler;
import io.reactivex.rxjava3.plugins.RxJavaPlugins;
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
