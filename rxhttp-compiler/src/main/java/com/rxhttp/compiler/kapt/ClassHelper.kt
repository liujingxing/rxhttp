package com.rxhttp.compiler.kapt

import com.rxhttp.compiler.getClassPath
import com.rxhttp.compiler.isDependenceRxJava
import com.rxhttp.compiler.rxHttpPackage
import java.io.BufferedWriter
import java.io.IOException
import javax.annotation.processing.Filer


/**
 * User: ljx
 * Date: 2020/3/31
 * Time: 23:36
 */
class ClassHelper(private val isAndroidPlatform: Boolean) {

    private fun isAndroid(s: String) = if (isAndroidPlatform) s else ""

    fun generatorStaticClass(filer: Filer) {
        generatorBaseRxHttp(filer)
        generatorRxHttpAbstractBodyParam(filer)
        generatorRxHttpBodyParam(filer)
        generatorRxHttpFormParam(filer)
        generatorRxHttpNoBodyParam(filer)
        generatorRxHttpJsonParam(filer)
        generatorRxHttpJsonArrayParam(filer)
        if (isDependenceRxJava()) {
            generatorObservableClass(filer)
        }
    }

    private fun generatorObservableClass(filer: Filer) {
        generatorObservableCall(filer)
        generatorObservableCallEnqueue(filer)
        generatorObservableCallExecute(filer)
        generatorObservableParser(filer)
    }

    private fun generatorBaseRxHttp(filer: Filer) {
        if (!isDependenceRxJava()) {
            generatorClass(
                filer, "BaseRxHttp", """
                package $rxHttpPackage;

                import rxhttp.wrapper.CallFactory;
                import rxhttp.wrapper.coroutines.RangeHeader;

                /**
                 * 本类存放asXxx方法(需要单独依赖RxJava，并告知RxHttp依赖的RxJava版本)
                 * 如未生成，请查看 https://github.com/liujingxing/rxhttp/wiki/FAQ
                 * User: ljx
                 * Date: 2020/4/11
                 * Time: 18:15
                 */
                public abstract class BaseRxHttp implements CallFactory, RangeHeader {

                    
                }
            """.trimIndent())
        } else {
            generatorClass(filer, "BaseRxHttp", """
            package $rxHttpPackage;
            ${isAndroid("""
            import android.content.Context;
            import android.graphics.Bitmap;
            import android.net.Uri;
            """)}
            import java.lang.reflect.Type;
            import java.util.List;
            import java.util.Map;

            import ${getClassPath("Observable")};
            import ${getClassPath("Scheduler")};
            import ${getClassPath("Consumer")};
            import ${getClassPath("RxJavaPlugins")};
            import ${getClassPath("Schedulers")};
            import okhttp3.Headers;
            import okhttp3.Response;
            import rxhttp.wrapper.CallFactory;
            import rxhttp.wrapper.OkHttpCompat;
            import rxhttp.wrapper.callback.FileOutputStreamFactory;
            import rxhttp.wrapper.callback.OutputStreamFactory;
            ${isAndroid("import rxhttp.wrapper.callback.UriOutputStreamFactory;")}
            import rxhttp.wrapper.coroutines.RangeHeader;
            import rxhttp.wrapper.entity.ParameterizedTypeImpl;
            import rxhttp.wrapper.entity.Progress;
            ${isAndroid("import rxhttp.wrapper.parse.BitmapParser;")}
            import rxhttp.wrapper.parse.OkResponseParser;
            import rxhttp.wrapper.parse.Parser;
            import rxhttp.wrapper.parse.SimpleParser;
            import rxhttp.wrapper.parse.StreamParser;
            import rxhttp.wrapper.utils.LogUtil;

            /**
             * 本类存放asXxx方法(需要单独依赖RxJava，并告知RxHttp依赖的RxJava版本)
             * 如未生成，请查看 https://github.com/liujingxing/rxhttp/wiki/FAQ
             * User: ljx
             * Date: 2020/4/11
             * Time: 18:15
             */
            public abstract class BaseRxHttp implements CallFactory, RangeHeader {

                static {                   
                    Consumer<? super Throwable> errorHandler = RxJavaPlugins.getErrorHandler();
                    if (errorHandler == null) {                                                
                        /*                                                                     
                        RxJava2的一个重要的设计理念是：不吃掉任何一个异常, 即抛出的异常无人处理，便会导致程序崩溃                      
                        这就会导致一个问题，当RxJava2“downStream”取消订阅后，“upStream”仍有可能抛出异常，                
                        这时由于已经取消订阅，“downStream”无法处理异常，此时的异常无人处理，便会导致程序崩溃                       
                        */                                                                     
                        RxJavaPlugins.setErrorHandler(LogUtil::log);                           
                    }                                                                          
                }                                                                              

                public abstract <T> Observable<T> asParser(Parser<T> parser, Scheduler scheduler, Consumer<Progress> progressConsumer);
                
                public <T> Observable<T> asParser(Parser<T> parser) {
                    return asParser(parser, null, null);
                }

                public final <T> Observable<T> asClass(Class<T> type) {
                    return asParser(new SimpleParser<>(type));
                }

                public final Observable<String> asString() {
                    return asClass(String.class);
                }

                public final <K> Observable<Map<K, K>> asMap(Class<K> kType) {
                    return asMap(kType, kType);
                }

                public final <K, V> Observable<Map<K, V>> asMap(Class<K> kType, Class<V> vType) {
                    Type tTypeMap = ParameterizedTypeImpl.getParameterized(Map.class, kType, vType);
                    return asParser(new SimpleParser<>(tTypeMap));
                }

                public final <T> Observable<List<T>> asList(Class<T> tType) {
                    Type tTypeList = ParameterizedTypeImpl.get(List.class, tType);
                    return asParser(new SimpleParser<>(tTypeList));
                }
                ${isAndroid("""
                public final Observable<Bitmap> asBitmap() {
                    return asParser(new BitmapParser());
                }
                """)}
                public final Observable<Response> asOkResponse() {
                    return asParser(new OkResponseParser());
                }

                public final Observable<Headers> asHeaders() {               
                    return asOkResponse()                                    
                        .map(response -> {                                   
                            try {                                            
                                return response.headers();                   
                            } finally {                                      
                                OkHttpCompat.closeQuietly(response);  
                            }                                                
                        });                                                  
                }

                public final Observable<String> asDownload(String destPath) {
                    return asDownload(destPath, null, null);
                }

                public final Observable<String> asDownload(String destPath,
                                                           Consumer<Progress> progressConsumer) {
                    return asDownload(destPath, null, progressConsumer);
                }
                
                public final Observable<String> asDownload(String destPath, Scheduler scheduler,
                                                           Consumer<Progress> progressConsumer) {
                    return asDownload(new FileOutputStreamFactory(destPath), scheduler, progressConsumer);
                }
                ${isAndroid("""
                public final Observable<Uri> asDownload(Context context, Uri uri) {
                    return asDownload(context, uri, null, null);   
                }                                                                  
                    
                public final Observable<Uri> asDownload(Context context, Uri uri, Scheduler scheduler,    
                                                           Consumer<Progress> progressConsumer) {            
                    return asDownload(new UriOutputStreamFactory(context, uri), scheduler, progressConsumer);
                }                                                                                            
                """)}
                public final <T> Observable<T> asDownload(OutputStreamFactory<T> osFactory) {
                    return asDownload(osFactory, null, null);             
                } 
                                                                                           
                public final <T> Observable<T> asDownload(OutputStreamFactory<T> osFactory, Scheduler scheduler,
                                                           Consumer<Progress> progressConsumer) {
                    return asParser(new StreamParser<>(osFactory), scheduler, progressConsumer);
                }
                
                public final Observable<String> asAppendDownload(String destPath) {                    
                    return asAppendDownload(destPath, null, null);                                     
                }                                                                                      
                                                                                                       
                public final Observable<String> asAppendDownload(String destPath, Scheduler scheduler, 
                                                                 Consumer<Progress> progressConsumer) {
                    return asAppendDownload(new FileOutputStreamFactory(destPath), scheduler, progressConsumer);         
                }                                                                       
                ${isAndroid("""
                public final Observable<Uri> asAppendDownload(Context context, Uri uri) {                   
                    return asAppendDownload(context, uri, null, null);                                      
                }                                                                                           
                                                                                                            
                public final Observable<Uri> asAppendDownload(Context context, Uri uri, Scheduler scheduler,
                                                              Consumer<Progress> progressConsumer) {        
                    return asAppendDownload(new UriOutputStreamFactory(context, uri), scheduler, progressConsumer);       
                }                                               
                """)}
                public final <T> Observable<T> asAppendDownload(OutputStreamFactory<T> osFactory) {                   
                    return asAppendDownload(osFactory, null, null);                                     
                }                                                                                        
                                                                                                         
                public final <T> Observable<T> asAppendDownload(OutputStreamFactory<T> osFactory, Scheduler scheduler,
                                                              Consumer<Progress> progressConsumer) {
                    return Observable
                        .fromCallable(() -> {
                            long offsetSize = osFactory.offsetSize();
                            if (offsetSize >= 0)
                                setRangeHeader(offsetSize, -1, true);
                            return new StreamParser<>(osFactory);
                        })
                        .subscribeOn(Schedulers.io())
                        .flatMap(parser -> asParser(parser, scheduler, progressConsumer));
                }
                
            }

        """.trimIndent())
        }
    }

    private fun generatorObservableCallEnqueue(filer: Filer) {
        generatorClass(filer, "ObservableCallEnqueue", """
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

    private fun generatorObservableCallExecute(filer: Filer) {
        generatorClass(filer, "ObservableCallExecute", """
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

    private fun generatorObservableCall(filer: Filer) {
        generatorClass(filer, "ObservableCall", """
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

    private fun generatorObservableParser(filer: Filer) {
        generatorClass(filer, "ObservableParser", """
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

    private fun generatorRxHttpAbstractBodyParam(filer: Filer) {
        if (!isDependenceRxJava()) {
            generatorClass(filer, "RxHttpAbstractBodyParam", """
                package $rxHttpPackage;
                
                import rxhttp.wrapper.BodyParamFactory;
                import rxhttp.wrapper.param.AbstractBodyParam;

                /**
                 * Github
                 * https://github.com/liujingxing/rxhttp
                 * https://github.com/liujingxing/rxlife
                 * https://github.com/liujingxing/rxhttp/wiki/FAQ
                 * https://github.com/liujingxing/rxhttp/wiki/更新日志
                 */
                @SuppressWarnings("unchecked")
                public class RxHttpAbstractBodyParam<P extends AbstractBodyParam<P>, R extends RxHttpAbstractBodyParam<P, R>> 
                    extends RxHttp<P, R> implements BodyParamFactory {

                    protected RxHttpAbstractBodyParam(P param) {
                        super(param);
                    }

                    public final R setUploadMaxLength(long maxLength) {
                        param.setUploadMaxLength(maxLength);
                        return (R) this;
                    }
                }
            """.trimIndent())
        } else {
            generatorClass(filer, "RxHttpAbstractBodyParam", """
                package $rxHttpPackage;
                
                import ${getClassPath("Observable")};
                import ${getClassPath("Scheduler")};
                import ${getClassPath("Consumer")};
                import rxhttp.wrapper.BodyParamFactory;
                import rxhttp.wrapper.entity.Progress;
                import rxhttp.wrapper.param.AbstractBodyParam;
                import rxhttp.wrapper.parse.Parser;
                
                /**
                 * Github
                 * https://github.com/liujingxing/rxhttp
                 * https://github.com/liujingxing/rxlife
                 * https://github.com/liujingxing/rxhttp/wiki/FAQ
                 * https://github.com/liujingxing/rxhttp/wiki/更新日志
                 */
                @SuppressWarnings("unchecked")
                public class RxHttpAbstractBodyParam<P extends AbstractBodyParam<P>, R extends RxHttpAbstractBodyParam<P, R>> 
                    extends RxHttp<P, R> implements BodyParamFactory {

                    //Controls the downstream callback thread
                    private Scheduler observeOnScheduler;

                    //Upload progress callback
                    private Consumer<Progress> progressConsumer;

                    protected RxHttpAbstractBodyParam(P param) {
                        super(param);
                    }

                    public final R setUploadMaxLength(long maxLength) {
                        param.setUploadMaxLength(maxLength);
                        return (R) this;
                    }

                    public final R upload(Consumer<Progress> progressConsumer) {
                        return upload(null, progressConsumer);
                    }

                    /**
                     * @param progressConsumer   Upload progress callback
                     * @param observeOnScheduler Controls the downstream callback thread
                     */
                    public final R upload(Scheduler observeOnScheduler, Consumer<Progress> progressConsumer) {
                        this.progressConsumer = progressConsumer;
                        this.observeOnScheduler = observeOnScheduler;
                        return (R) this;
                    }

                    @Override
                    public final <T> Observable<T> asParser(Parser<T> parser) {
                        return asParser(parser, observeOnScheduler, progressConsumer);
                    }

                    @Override
                    public final <T> Observable<T> asParser(Parser<T> parser, Scheduler scheduler,
                                                            Consumer<Progress> progressConsumer) {
                        if (progressConsumer == null) {
                            return super.asParser(parser, scheduler, null);
                        }
                        ObservableCall observableCall;
                        if (isAsync) {
                            observableCall = new ObservableCallEnqueue(this, true);
                        } else {
                            observableCall = new ObservableCallExecute(this, true);
                        }
                        return observableCall.asParser(parser, scheduler, progressConsumer);
                    }
                }

        """.trimIndent())
        }
    }

    private fun generatorRxHttpNoBodyParam(filer: Filer) {
        generatorClass(filer, "RxHttpNoBodyParam", """
            package $rxHttpPackage;

            import java.util.Map;
            
            import rxhttp.wrapper.annotations.NonNull;
            import rxhttp.wrapper.param.NoBodyParam;

            /**
             * Github
             * https://github.com/liujingxing/rxhttp
             * https://github.com/liujingxing/rxlife
             * https://github.com/liujingxing/rxhttp/wiki/FAQ
             * https://github.com/liujingxing/rxhttp/wiki/更新日志
             */
            public class RxHttpNoBodyParam extends RxHttp<NoBodyParam, RxHttpNoBodyParam> {
                public RxHttpNoBodyParam(NoBodyParam param) {
                    super(param);
                }
                
                public RxHttpNoBodyParam add(String key, Object value) {
                    return addQuery(key, value);
                }
                
                public RxHttpNoBodyParam add(String key, Object value, boolean isAdd) {
                    if (isAdd) {
                        addQuery(key, value);
                    }
                    return this;
                }
                
                public RxHttpNoBodyParam addAll(Map<String, ?> map) {
                    return addAllQuery(map);
                }

                public RxHttpNoBodyParam addEncoded(String key, Object value) {
                    return addEncodedQuery(key, value);
                }
                
                public RxHttpNoBodyParam addAllEncoded(@NonNull Map<String, ?> map) {
                    return addAllEncodedQuery(map);
                }
            }

        """.trimIndent())
    }

    private fun generatorRxHttpBodyParam(filer: Filer) {
        generatorClass(
            filer, "RxHttpBodyParam", """
            package $rxHttpPackage;
            ${isAndroid("""
            import android.content.Context;
            import android.net.Uri;
            import rxhttp.wrapper.utils.UriUtil;
            """)}
            import rxhttp.wrapper.annotations.Nullable;
            import rxhttp.wrapper.param.BodyParam;

            import java.io.File;

            import okhttp3.MediaType;
            import okhttp3.RequestBody;
            import okio.ByteString;

            /**
             * Github
             * https://github.com/liujingxing/rxhttp
             * https://github.com/liujingxing/rxlife
             */
            public class RxHttpBodyParam extends RxHttpAbstractBodyParam<BodyParam, RxHttpBodyParam> {
                public RxHttpBodyParam(BodyParam param) {
                    super(param);
                }
                
                public RxHttpBodyParam setBody(RequestBody requestBody) {
                    param.setBody(requestBody);
                    return this;
                }
                
                public RxHttpBodyParam setBody(String content, @Nullable MediaType mediaType) {
                    param.setBody(content, mediaType);
                    return this;
                }
                
                public RxHttpBodyParam setBody(ByteString content, @Nullable MediaType mediaType) {
                    param.setBody(content, mediaType);
                    return this;
                }
                
                public RxHttpBodyParam setBody(byte[] content, @Nullable MediaType mediaType) {
                    param.setBody(content, mediaType);
                    return this;
                }
                
                public RxHttpBodyParam setBody(byte[] content, @Nullable MediaType mediaType, int offset, int byteCount) {
                    param.setBody(content, mediaType, offset, byteCount);
                    return this;
                }
                
                public RxHttpBodyParam setBody(File file) {
                    param.setBody(file);
                    return this;
                }
                
                public RxHttpBodyParam setBody(File file, @Nullable MediaType mediaType) {
                    param.setBody(file, mediaType);
                    return this;
                }
                ${isAndroid("""
                public RxHttpBodyParam setBody(Uri uri, Context context) {
                    param.setBody(UriUtil.asRequestBody(uri, context));
                    return this;
                }
                
                public RxHttpBodyParam setBody(Uri uri, Context context, @Nullable MediaType contentType) {
                    param.setBody(UriUtil.asRequestBody(uri, context, 0, contentType));
                    return this;
                }
                """)}
            
                public RxHttpBodyParam setBody(Object object) {
                    param.setBody(object);
                    return this;
                }
                
                /**
                 * @deprecated please use {@link #setBody(Object)} instead, scheduled to be removed in RxHttp 3.0 release.
                 */
                @Deprecated
                public RxHttpBodyParam setJsonBody(Object object) {
                    return setBody(object);
                }
            }

        """.trimIndent()
        )
    }

    private fun generatorRxHttpFormParam(filer: Filer) {
        generatorClass(filer, "RxHttpFormParam", """
            package $rxHttpPackage;

            ${isAndroid("import android.content.Context;")}
            ${isAndroid("import android.net.Uri;")}

            import java.io.File;
            import java.util.List;
            import java.util.Map;
            import java.util.Map.Entry;

            import okhttp3.Headers;
            import okhttp3.MediaType;
            import okhttp3.MultipartBody.Part;
            import okhttp3.RequestBody;
            import rxhttp.wrapper.annotations.NonNull;
            import rxhttp.wrapper.annotations.Nullable;
            import rxhttp.wrapper.entity.UpFile;
            import rxhttp.wrapper.param.FormParam;
            import rxhttp.wrapper.utils.UriUtil;

            /**
             * Github
             * https://github.com/liujingxing/rxhttp
             * https://github.com/liujingxing/rxlife
             * https://github.com/liujingxing/rxhttp/wiki/FAQ
             * https://github.com/liujingxing/rxhttp/wiki/更新日志
             */
            public class RxHttpFormParam extends RxHttpAbstractBodyParam<FormParam, RxHttpFormParam> {
                public RxHttpFormParam(FormParam param) {
                    super(param);
                }

                public RxHttpFormParam add(String key, Object value) {
                  param.add(key,value);
                  return this;
                }
                
                public RxHttpFormParam add(String key, Object value, boolean isAdd) {
                  if(isAdd) {
                    param.add(key,value);
                  }
                  return this;
                }
                
                public RxHttpFormParam addAll(Map<String, ?> map) {
                  param.addAll(map);
                  return this;
                }
                
                public RxHttpFormParam addEncoded(String key, Object value) {
                    param.addEncoded(key, value);
                    return this;
                }
                
                public RxHttpFormParam addAllEncoded(@NonNull Map<String, ?> map) {
                    param.addAllEncoded(map);
                    return this;
                }

                public RxHttpFormParam removeAllBody() {
                    param.removeAllBody();
                    return this;
                }

                public RxHttpFormParam removeAllBody(String key) {
                    param.removeAllBody(key);
                    return this;
                }

                public RxHttpFormParam set(String key, Object value) {
                    param.set(key, value);
                    return this;
                }

                public RxHttpFormParam setEncoded(String key, Object value) {
                    param.setEncoded(key, value);
                    return this;
                }

                public RxHttpFormParam addFile(String key, File file) {
                    param.addFile(key, file);
                    return this;
                }

                public RxHttpFormParam addFile(String key, String filePath) {
                    param.addFile(key, filePath);
                    return this;
                }

                public RxHttpFormParam addFile(String key, File file, String filename) {
                    param.addFile(key, file, filename);
                    return this;
                }

                public RxHttpFormParam addFile(UpFile file) {
                    param.addFile(file);
                    return this;
                }

                /**
                 * @deprecated please use {@link #addFiles(List)} instead, scheduled to be removed in RxHttp 3.0 release.
                 */
                @Deprecated
                public RxHttpFormParam addFile(List<? extends UpFile> fileList) {
                    return addFiles(fileList);
                }
                
                /**
                 * @deprecated please use {@link #addFiles(String, List)} instead, scheduled to be removed in RxHttp 3.0 release.
                 */
                @Deprecated
                public <T> RxHttpFormParam addFile(String key, List<T> fileList) {
                    return addFiles(key, fileList);
                }

                public RxHttpFormParam addFiles(List<? extends UpFile> fileList) {
                    param.addFiles(fileList);
                    return this;
                }
                
                public <T> RxHttpFormParam addFiles(Map<String, T> fileMap) {
                    param.addFiles(fileMap);
                    return this;
                }
                
                public <T> RxHttpFormParam addFiles(String key, List<T> fileList) {
                    param.addFiles(key, fileList);
                    return this;
                }

                public RxHttpFormParam addPart(@Nullable MediaType contentType, byte[] content) {
                    param.addPart(contentType, content);
                    return this;
                }

                public RxHttpFormParam addPart(@Nullable MediaType contentType, byte[] content, int offset,
                                               int byteCount) {
                    param.addPart(contentType, content, offset, byteCount);
                    return this;
                }
                ${isAndroid("""
                public RxHttpFormParam addPart(Context context, Uri uri) {
                    param.addPart(UriUtil.asRequestBody(uri, context));
                    return this;
                }

                public RxHttpFormParam addPart(Context context, String key, Uri uri) {
                    param.addPart(UriUtil.asPart(uri, context, key));
                    return this;
                }

                public RxHttpFormParam addPart(Context context, String key, String fileName, Uri uri) {
                    param.addPart(UriUtil.asPart(uri, context, key, fileName));
                    return this;
                }

                public RxHttpFormParam addPart(Context context, Uri uri, @Nullable MediaType contentType) {
                    param.addPart(UriUtil.asRequestBody(uri, context, 0, contentType));
                    return this;
                }

                public RxHttpFormParam addPart(Context context, String key, Uri uri,
                                               @Nullable MediaType contentType) {
                    param.addPart(UriUtil.asPart(uri, context, key, UriUtil.displayName(uri, context), 0, contentType));
                    return this;
                }

                public RxHttpFormParam addPart(Context context, String key, String filename, Uri uri,
                                               @Nullable MediaType contentType) {
                    param.addPart(UriUtil.asPart(uri, context, key, filename, 0, contentType));
                    return this;
                }

                public RxHttpFormParam addParts(Context context, Map<String, Uri> uriMap) {
                    for (Entry<String, Uri> entry : uriMap.entrySet()) {
                        addPart(context, entry.getKey(), entry.getValue());
                    }
                    return this;
                }

                public RxHttpFormParam addParts(Context context, List<Uri> uris) {
                    for (Uri uri : uris) {
                        addPart(context, uri);
                    }
                    return this;
                }

                public RxHttpFormParam addParts(Context context, String key, List<Uri> uris) {
                    for (Uri uri : uris) {
                        addPart(context, key, uri);
                    }
                    return this;
                }

                public RxHttpFormParam addParts(Context context, List<Uri> uris,
                                                @Nullable MediaType contentType) {
                    for (Uri uri : uris) {
                        addPart(context, uri, contentType);
                    }
                    return this;
                }

                public RxHttpFormParam addParts(Context context, String key, List<Uri> uris,
                                                @Nullable MediaType contentType) {
                    for (Uri uri : uris) {
                        addPart(context, key, uri, contentType);
                    }
                    return this;
                }
                """)}
                public RxHttpFormParam addPart(Part part) {
                    param.addPart(part);
                    return this;
                }

                public RxHttpFormParam addPart(RequestBody requestBody) {
                    param.addPart(requestBody);
                    return this;
                }

                public RxHttpFormParam addPart(Headers headers, RequestBody requestBody) {
                    param.addPart(headers, requestBody);
                    return this;
                }

                public RxHttpFormParam addFormDataPart(String key, String fileName, RequestBody requestBody) {
                    param.addFormDataPart(key, fileName, requestBody);
                    return this;
                }

                //Set content-type to multipart/form-data
                public RxHttpFormParam setMultiForm() {
                    param.setMultiForm();
                    return this;
                }
                
                //Set content-type to multipart/mixed
                public RxHttpFormParam setMultiMixed() {
                    param.setMultiMixed();
                    return this;
                }
                
                //Set content-type to multipart/alternative
                public RxHttpFormParam setMultiAlternative() {
                    param.setMultiAlternative();
                    return this;
                }
                
                //Set content-type to multipart/digest
                public RxHttpFormParam setMultiDigest() {
                    param.setMultiDigest();
                    return this;
                }
                
                //Set content-type to multipart/parallel
                public RxHttpFormParam setMultiParallel() {
                    param.setMultiParallel();
                    return this;
                }
                
                //Set the MIME type
                public RxHttpFormParam setMultiType(MediaType multiType) {
                    param.setMultiType(multiType);
                    return this;
                }
            }

        """.trimIndent())
    }

    private fun generatorRxHttpJsonParam(filer: Filer) {
        generatorClass(filer, "RxHttpJsonParam", """
            package $rxHttpPackage;

            import com.google.gson.JsonObject;

            import java.util.Map;
            
            import rxhttp.wrapper.param.JsonParam;
            /**
             * Github
             * https://github.com/liujingxing/rxhttp
             * https://github.com/liujingxing/rxlife
             * https://github.com/liujingxing/rxhttp/wiki/FAQ
             * https://github.com/liujingxing/rxhttp/wiki/更新日志
             */
            public class RxHttpJsonParam extends RxHttpAbstractBodyParam<JsonParam, RxHttpJsonParam> {
                public RxHttpJsonParam(JsonParam param) {
                    super(param);
                }

                public RxHttpJsonParam add(String key, Object value) {
                  param.add(key,value);
                  return this;
                }
                
                public RxHttpJsonParam add(String key, Object value, boolean isAdd) {
                  if(isAdd) {
                    param.add(key,value);
                  }
                  return this;
                }
                
                public RxHttpJsonParam addAll(Map<String, ?> map) {
                  param.addAll(map);
                  return this;
                }
                
                /**
                 * 将Json对象里面的key-value逐一取出，添加到另一个Json对象中，
                 * 输入非Json对象将抛出{@link IllegalStateException}异常
                 */
                public RxHttpJsonParam addAll(String jsonObject) {
                    param.addAll(jsonObject);
                    return this;
                }

                /**
                 * 将Json对象里面的key-value逐一取出，添加到另一个Json对象中
                 */
                public RxHttpJsonParam addAll(JsonObject jsonObject) {
                    param.addAll(jsonObject);
                    return this;
                }

                /**
                 * 添加一个JsonElement对象(Json对象、json数组等)
                 */
                public RxHttpJsonParam addJsonElement(String key, String jsonElement) {
                    param.addJsonElement(key, jsonElement);
                    return this;
                }
            }

        """.trimIndent())
    }

    private fun generatorRxHttpJsonArrayParam(filer: Filer) {
        generatorClass(filer, "RxHttpJsonArrayParam", """
            package $rxHttpPackage;

            import com.google.gson.JsonArray;
            import com.google.gson.JsonObject;

            import java.util.List;
            import java.util.Map;
            
            import rxhttp.wrapper.param.JsonArrayParam;

            /**
             * Github
             * https://github.com/liujingxing/rxhttp
             * https://github.com/liujingxing/rxlife
             * https://github.com/liujingxing/rxhttp/wiki/FAQ
             * https://github.com/liujingxing/rxhttp/wiki/更新日志
             */
            public class RxHttpJsonArrayParam extends RxHttpAbstractBodyParam<JsonArrayParam, RxHttpJsonArrayParam> {
                public RxHttpJsonArrayParam(JsonArrayParam param) {
                    super(param);
                }

                public RxHttpJsonArrayParam add(String key, Object value) {
                  param.add(key,value);
                  return this;
                }
                
                public RxHttpJsonArrayParam add(String key, Object value, boolean isAdd) {
                  if(isAdd) {
                    param.add(key,value);
                  }
                  return this;
                }
                
                public RxHttpJsonArrayParam addAll(Map<String, ?> map) {
                  param.addAll(map);
                  return this;
                }

                public RxHttpJsonArrayParam add(Object object) {
                    param.add(object);
                    return this;
                }

                public RxHttpJsonArrayParam addAll(List<?> list) {
                    param.addAll(list);
                    return this;
                }

                /**
                 * 添加多个对象，将字符串转JsonElement对象,并根据不同类型,执行不同操作,可输入任意非空字符串
                 */
                public RxHttpJsonArrayParam addAll(String jsonElement) {
                    param.addAll(jsonElement);
                    return this;
                }

                public RxHttpJsonArrayParam addAll(JsonArray jsonArray) {
                    param.addAll(jsonArray);
                    return this;
                }

                /**
                 * 将Json对象里面的key-value逐一取出，添加到Json数组中，成为单独的对象
                 */
                public RxHttpJsonArrayParam addAll(JsonObject jsonObject) {
                    param.addAll(jsonObject);
                    return this;
                }

                public RxHttpJsonArrayParam addJsonElement(String jsonElement) {
                    param.addJsonElement(jsonElement);
                    return this;
                }

                /**
                 * 添加一个JsonElement对象(Json对象、json数组等)
                 */
                public RxHttpJsonArrayParam addJsonElement(String key, String jsonElement) {
                    param.addJsonElement(key, jsonElement);
                    return this;
                }
            }

        """.trimIndent())
    }

    private fun generatorClass(filer: Filer, className: String, content: String) {
        var writer: BufferedWriter? = null
        try {
            val sourceFile = filer.createSourceFile("$rxHttpPackage.$className")
            writer = BufferedWriter(sourceFile.openWriter())
            writer.write(content)
        } catch (e: Exception) {

        } finally {
            try {
                writer?.close()
            } catch (e: IOException) {
                //Silent
            }
        }
    }
}