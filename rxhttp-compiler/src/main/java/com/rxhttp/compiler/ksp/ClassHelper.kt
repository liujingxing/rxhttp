package com.rxhttp.compiler.ksp

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.symbol.KSFile
import com.rxhttp.compiler.getClassPath
import com.rxhttp.compiler.isDependenceRxJava
import com.rxhttp.compiler.rxHttpPackage


/**
 * User: ljx
 * Date: 2020/3/31
 * Time: 23:36
 */
class ClassHelper(
    private val isAndroidPlatform: Boolean,
    private val ksFiles: Collection<KSFile>
) {

    fun generatorStaticClass(codeGenerator: CodeGenerator) {
        if (isDependenceRxJava()) {
            generatorObservableClass(codeGenerator)
        }
    }

    private fun generatorObservableClass(codeGenerator: CodeGenerator) {
        generatorObservableCall(codeGenerator)
        generatorObservableCallEnqueue(codeGenerator)
        generatorObservableCallExecute(codeGenerator)
        generatorObservableParser(codeGenerator)
    }

    private fun generatorObservableCallEnqueue(codeGenerator: CodeGenerator) {
        generatorClass(codeGenerator, "ObservableCallEnqueue", """
            package $rxHttpPackage;
 
            import java.io.IOException;
            
            import ${getClassPath("Observer")};
            import ${getClassPath("Disposable")};
            import ${getClassPath("Exceptions")};
            import ${getClassPath("RxJavaPlugins")};
            import okhttp3.Call;
            import okhttp3.Callback;
            import okhttp3.Response;
            import rxhttp.wrapper.BodyParamFactory;
            import rxhttp.wrapper.CallFactory;
            import rxhttp.wrapper.callback.ProgressCallback;
            import rxhttp.wrapper.entity.Progress;
            import rxhttp.wrapper.entity.ProgressT;
            import rxhttp.wrapper.utils.LogUtil;

            /**
             * User: ljx
             * Date: 2018/04/20
             * Time: 11:15
             */
            final class ObservableCallEnqueue extends ObservableCall {

                private CallFactory callFactory;
                private boolean callbackUploadProgress;

                ObservableCallEnqueue(CallFactory callFactory) {
                    this(callFactory, false);
                }

                ObservableCallEnqueue(CallFactory callFactory, boolean callbackUploadProgress) {
                    this.callFactory = callFactory;
                    this.callbackUploadProgress = callbackUploadProgress;
                }

                @Override
                public void subscribeActual(Observer<? super Progress> observer) {
                    HttpDisposable d = new HttpDisposable(observer, callFactory, callbackUploadProgress);
                    observer.onSubscribe(d);
                    if (d.isDisposed()) {
                        return;
                    }
                    d.run();
                }


                private static class HttpDisposable implements Disposable, Callback, ProgressCallback {

                    private volatile boolean disposed;

                    private final Call call;
                    private final Observer<? super Progress> downstream;

                    /**
                     * Constructs a DeferredScalarDisposable by wrapping the Observer.
                     *
                     * @param downstream the Observer to wrap, not null (not verified)
                     */
                    HttpDisposable(Observer<? super Progress> downstream, CallFactory callFactory, boolean callbackUploadProgress) {
                        if (callFactory instanceof BodyParamFactory && callbackUploadProgress) {
                            ((BodyParamFactory) callFactory).getParam().setProgressCallback(this);
                        }
                        this.downstream = downstream;
                        this.call = callFactory.newCall();
                    }

                    @Override
                    public void onProgress(int progress, long currentSize, long totalSize) {
                        if (!disposed) {
                            downstream.onNext(new Progress(progress, currentSize, totalSize));
                        }
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        if (!disposed) {
                            downstream.onNext(new ProgressT<>(response));
                        }
                        if (!disposed) {
                            downstream.onComplete();
                        }
                    }

                    @Override
                    public void onFailure(Call call, IOException e) {
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
                        disposed = true;
                        call.cancel();
                    }

                    @Override
                    public boolean isDisposed() {
                        return disposed;
                    }

                    public void run() {
                        call.enqueue(this);
                    }
                }
            }

        """.trimIndent())
    }

    private fun generatorObservableCallExecute(codeGenerator: CodeGenerator) {
        generatorClass(codeGenerator, "ObservableCallExecute", """
            package $rxHttpPackage;

            import ${getClassPath("Observer")};
            import ${getClassPath("Disposable")};
            import ${getClassPath("Exceptions")};
            import ${getClassPath("RxJavaPlugins")};
            import okhttp3.Call;
            import okhttp3.Response;
            import rxhttp.wrapper.BodyParamFactory;
            import rxhttp.wrapper.CallFactory;
            import rxhttp.wrapper.callback.ProgressCallback;
            import rxhttp.wrapper.entity.Progress;
            import rxhttp.wrapper.entity.ProgressT;
            import rxhttp.wrapper.utils.LogUtil;

            /**
             * User: ljx
             * Date: 2018/04/20
             * Time: 11:15
             */
            final class ObservableCallExecute extends ObservableCall {

                private CallFactory callFactory;
                private boolean callbackUploadProgress;

                ObservableCallExecute(CallFactory callFactory) {
                    this(callFactory, false);
                }

                ObservableCallExecute(CallFactory callFactory, boolean callbackUploadProgress) {
                    this.callFactory = callFactory;
                    this.callbackUploadProgress = callbackUploadProgress;
                }

                @Override
                public void subscribeActual(Observer<? super Progress> observer) {
                    HttpDisposable d = new HttpDisposable(observer, callFactory, callbackUploadProgress);
                    observer.onSubscribe(d);
                    if (d.isDisposed()) {
                        return;
                    }
                    d.run();
                }

                private static class HttpDisposable implements Disposable, ProgressCallback {

                    private boolean fusionMode;
                    private volatile boolean disposed;

                    private final Call call;
                    private final Observer<? super Progress> downstream;

                    /**
                     * Constructs a DeferredScalarDisposable by wrapping the Observer.
                     *
                     * @param downstream the Observer to wrap, not null (not verified)
                     */
                    HttpDisposable(Observer<? super Progress> downstream, CallFactory callFactory, boolean callbackUploadProgress) {
                        if (callFactory instanceof BodyParamFactory && callbackUploadProgress) {
                            ((BodyParamFactory) callFactory).getParam().setProgressCallback(this);
                        }
                        this.downstream = downstream;
                        this.call = callFactory.newCall();
                    }

                    @Override
                    public void onProgress(int progress, long currentSize, long totalSize) {
                        if (!disposed) {
                            downstream.onNext(new Progress(progress, currentSize, totalSize));
                        }
                    }

                    public void run() {
                        Response value;
                        try {
                            value = call.execute();
                        } catch (Throwable e) {
                            LogUtil.log(call.request().url().toString(), e);
                            Exceptions.throwIfFatal(e);
                            if (!disposed) {
                                downstream.onError(e);
                            } else {
                                RxJavaPlugins.onError(e);
                            }
                            return;
                        }
                        if (!disposed) {
                            downstream.onNext(new ProgressT<>(value));
                        }
                        if (!disposed) {
                            downstream.onComplete();
                        }
                    }

                    @Override
                    public void dispose() {
                        disposed = true;
                        call.cancel();
                    }

                    @Override
                    public boolean isDisposed() {
                        return disposed;
                    }
                }
            }

        """.trimIndent())
    }

    private fun generatorObservableCall(codeGenerator: CodeGenerator) {
        generatorClass(codeGenerator, "ObservableCall", """
            package $rxHttpPackage;

            import ${getClassPath("Observable")};
            import ${getClassPath("Scheduler")};
            import ${getClassPath("Consumer")};
            import rxhttp.wrapper.entity.Progress;
            import rxhttp.wrapper.parse.Parser;
            
            /**
             * User: ljx
             * Date: 2020/9/5
             * Time: 21:59
             */
            abstract class ObservableCall extends Observable<Progress> {
            
                public <T> Observable<T> asParser(Parser<T> parser) {
                    return asParser(parser, null, null);
                }
            
                public <T> Observable<T> asParser(Parser<T> parser, Consumer<Progress> progressConsumer) {
                    return asParser(parser, null, progressConsumer);
                }
            
                public <T> Observable<T> asParser(Parser<T> parser, Scheduler scheduler, Consumer<Progress> progressConsumer) {
                    return new ObservableParser<>(this, parser, scheduler, progressConsumer);
                }
            }

        """.trimIndent())
    }

    private fun generatorObservableParser(codeGenerator: CodeGenerator) {
        generatorClass(codeGenerator, "ObservableParser", """
            package $rxHttpPackage;

            import java.util.Objects;
            import java.util.concurrent.LinkedBlockingQueue;
            import java.util.concurrent.atomic.AtomicInteger;

            import ${getClassPath("Observable")};
            import ${getClassPath("ObservableSource")};
            import ${getClassPath("Observer")};
            import ${getClassPath("Scheduler")};
            import ${getClassPath("Scheduler")}.Worker;
            import ${getClassPath("Disposable")};
            import ${getClassPath("Exceptions")};
            import ${getClassPath("Consumer")};
            import ${getClassPath("DisposableHelper")};
            import ${getClassPath("RxJavaPlugins")};
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
                            //LogUtil.logDownProgress(progress, currentSize, totalSize);
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
                    private final LinkedBlockingQueue<Progress> queue;
                    private final Scheduler.Worker worker;

                    private final Consumer<Progress> progressConsumer;

                    AsyncParserObserver(Observer<? super T> actual, Scheduler.Worker worker, Consumer<Progress> progressConsumer, Parser<T> parser) {
                        this.downstream = actual;
                        this.parser = parser;
                        this.worker = worker;
                        this.progressConsumer = progressConsumer;
                        queue = new LinkedBlockingQueue<>(2);

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
                        //LogUtil.logDownProgress(progress, currentSize, totalSize);
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
                        while (!queue.offer(p)) {
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

                        final LinkedBlockingQueue<Progress> q = queue;
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

        """.trimIndent())
    }

    private fun generatorClass(codeGenerator: CodeGenerator, className: String, content: String) {
        codeGenerator.createNewFile(
            Dependencies(false, *ksFiles.toTypedArray()),
            rxHttpPackage,
            className,
            "java"
        ).use { 
            it.write(content.toByteArray())
        }
    }
}