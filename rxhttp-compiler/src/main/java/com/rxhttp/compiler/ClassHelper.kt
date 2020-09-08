package com.rxhttp.compiler

import java.io.BufferedWriter
import java.io.IOException
import javax.annotation.processing.Filer


/**
 * User: ljx
 * Date: 2020/3/31
 * Time: 23:36
 */
object ClassHelper {

    @JvmStatic
    fun generatorBaseRxHttp(filer: Filer, isAndroid: Boolean) {
        if (!isDependenceRxJava()) {
            generatorClass(filer, "BaseRxHttp", """
                package $rxHttpPackage;

                import rxhttp.IRxHttp;

                /**
                 * 本类存放asXxx方法，如果依赖了RxJava的话
                 * User: ljx
                 * Date: 2020/4/11
                 * Time: 18:15
                 */
                public abstract class BaseRxHttp implements IRxHttp {

                    
                }
            """.trimIndent())
        } else {
            generatorClass(filer, "BaseRxHttp", """
            package $rxHttpPackage;
            ${
            if (isAndroid) """
            import android.content.Context;
            import android.graphics.Bitmap;
            import android.net.Uri;
            """ else ""
            }
            import java.io.OutputStream;
            import java.lang.reflect.Type;
            import java.util.List;
            import java.util.Map;

            import ${getClassPath("Observable")};
            import ${getClassPath("Scheduler")};
            import ${getClassPath("Consumer")};
            import ${getClassPath("RxJavaPlugins")};
            import okhttp3.Headers;
            import okhttp3.Response;
            import rxhttp.IRxHttp;
            import rxhttp.wrapper.OkHttpCompat;
            import rxhttp.wrapper.callback.FileOutputStreamFactory;
            import rxhttp.wrapper.callback.OutputStreamFactory;
            import rxhttp.wrapper.callback.UriOutputStreamFactory;
            import rxhttp.wrapper.entity.ParameterizedTypeImpl;
            import rxhttp.wrapper.entity.Progress;
            ${if (isAndroid) "import rxhttp.wrapper.parse.BitmapParser;" else ""}
            import rxhttp.wrapper.parse.OkResponseParser;
            import rxhttp.wrapper.parse.Parser;
            import rxhttp.wrapper.parse.SimpleParser;
            import rxhttp.wrapper.parse.StreamParser;
            import rxhttp.wrapper.utils.LogUtil;

            /**
             * 本类存放asXxx方法，如果依赖了RxJava的话
             * User: ljx
             * Date: 2020/4/11
             * Time: 18:15
             */
            public abstract class BaseRxHttp implements IRxHttp {

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

                public final Observable<Boolean> asBoolean() {
                    return asClass(Boolean.class);
                }

                public final Observable<Byte> asByte() {
                    return asClass(Byte.class);
                }

                public final Observable<Short> asShort() {
                    return asClass(Short.class);
                }

                public final Observable<Integer> asInteger() {
                    return asClass(Integer.class);
                }

                public final Observable<Long> asLong() {
                    return asClass(Long.class);
                }

                public final Observable<Float> asFloat() {
                    return asClass(Float.class);
                }

                public final Observable<Double> asDouble() {
                    return asClass(Double.class);
                }

                public final <K> Observable<Map<K, K>> asMap(Class<K> kType) {
                    return asMap(kType, kType);
                }

                public final <K, V> Observable<Map<K, V>> asMap(Class<K> kType, Class<V> vType) {
                    Type tTypeMap = ParameterizedTypeImpl.getParameterized(Map.class, kType, vType);
                    return asParser(new SimpleParser<Map<K, V>>(tTypeMap));
                }

                public final <T> Observable<List<T>> asList(Class<T> tType) {
                    Type tTypeList = ParameterizedTypeImpl.get(List.class, tType);
                    return asParser(new SimpleParser<List<T>>(tTypeList));
                }

                ${
                if (isAndroid) """public final <T> Observable<Bitmap> asBitmap() {
                    return asParser(new BitmapParser());
                }""" else ""
                }

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
                
                ${
                if (isAndroid) """
                public final Observable<String> asDownload(Context context, Uri uri, Scheduler scheduler,    
                                                           Consumer<Progress> progressConsumer) {            
                    return asDownload(new UriOutputStreamFactory(context, uri), scheduler, progressConsumer);
                }                                                                                            
                """ else ""
                }
                
                public final Observable<String> asDownload(OutputStreamFactory osFactory, Scheduler scheduler,
                                                           Consumer<Progress> progressConsumer) {
                    return asParser(new StreamParser(osFactory), scheduler, progressConsumer);
                }
            }

        """.trimIndent())
        }
    }

    @JvmStatic
    fun generatorObservableCallEnqueue(filer: Filer) {
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
            import rxhttp.IRxHttp;
            import rxhttp.wrapper.callback.ProgressCallback;
            import rxhttp.wrapper.entity.Progress;
            import rxhttp.wrapper.entity.ProgressT;
            import rxhttp.wrapper.utils.LogUtil;

            /**
             * User: ljx
             * Date: 2018/04/20
             * Time: 11:15
             */
            public final class ObservableCallEnqueue extends ObservableCall {

                private IRxHttp iRxHttp;
                private boolean callbackUploadProgress;

                public ObservableCallEnqueue(IRxHttp iRxHttp) {
                    this(iRxHttp, false);
                }

                public ObservableCallEnqueue(IRxHttp iRxHttp, boolean callbackUploadProgress) {
                    this.iRxHttp = iRxHttp;
                    this.callbackUploadProgress = callbackUploadProgress;
                }

                @Override
                public void subscribeActual(Observer<? super Progress> observer) {
                    HttpDisposable d = new HttpDisposable(observer, iRxHttp, callbackUploadProgress);
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
                    HttpDisposable(Observer<? super Progress> downstream, IRxHttp iRxHttp, boolean callbackUploadProgress) {
                        if (iRxHttp instanceof RxHttpBodyParam && callbackUploadProgress) {
                            RxHttpBodyParam<?, ?> bodyParam = (RxHttpBodyParam) iRxHttp;
                            bodyParam.getParam().setProgressCallback(this);
                        }
                        this.downstream = downstream;
                        this.call = iRxHttp.newCall();
                    }

                    @Override
                    public void onProgress(Progress p) {
                        if (!disposed) {
                            downstream.onNext(p);
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

    @JvmStatic
    fun generatorObservableCallExecute(filer: Filer) {
        generatorClass(filer, "ObservableCallExecute", """
            package $rxHttpPackage;

            import ${getClassPath("Observer")};
            import ${getClassPath("Disposable")};
            import ${getClassPath("Exceptions")};
            import ${getClassPath("RxJavaPlugins")};
            import okhttp3.Call;
            import okhttp3.Response;
            import rxhttp.IRxHttp;
            import rxhttp.wrapper.callback.ProgressCallback;
            import rxhttp.wrapper.entity.Progress;
            import rxhttp.wrapper.entity.ProgressT;
            import rxhttp.wrapper.utils.LogUtil;

            /**
             * User: ljx
             * Date: 2018/04/20
             * Time: 11:15
             */
            public final class ObservableCallExecute extends ObservableCall {

                private IRxHttp iRxHttp;
                private boolean callbackUploadProgress;

                public ObservableCallExecute(IRxHttp iRxHttp) {
                    this(iRxHttp, false);
                }

                public ObservableCallExecute(IRxHttp iRxHttp, boolean callbackUploadProgress) {
                    this.iRxHttp = iRxHttp;
                    this.callbackUploadProgress = callbackUploadProgress;
                }

                @Override
                public void subscribeActual(Observer<? super Progress> observer) {
                    HttpDisposable d = new HttpDisposable(observer, iRxHttp, callbackUploadProgress);
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
                    HttpDisposable(Observer<? super Progress> downstream, IRxHttp iRxHttp, boolean callbackUploadProgress) {
                        if (iRxHttp instanceof RxHttpBodyParam && callbackUploadProgress) {
                            RxHttpBodyParam<?, ?> bodyParam = (RxHttpBodyParam) iRxHttp;
                            bodyParam.getParam().setProgressCallback(this);
                        }
                        this.downstream = downstream;
                        this.call = iRxHttp.newCall();
                    }

                    @Override
                    public void onProgress(Progress p) {
                        if (!disposed) {
                            downstream.onNext(p);
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

    @JvmStatic
    fun generatorObservableCall(filer: Filer) {
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
            public abstract class ObservableCall extends Observable<Progress> {
            
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


    @JvmStatic
    fun generatorObservableParser(filer: Filer) {
        generatorClass(filer, "ObservableParser", """
            package $rxHttpPackage;

            import java.util.Objects;
            import java.util.concurrent.atomic.AtomicInteger;

            import ${getClassPath("Observable")};
            import ${getClassPath("ObservableSource")};
            import ${getClassPath("Observer")};
            import ${getClassPath("Exceptions")};
            import ${getClassPath("RxJavaPlugins")};
            import ${getClassPath("Scheduler")};
            import ${getClassPath("Disposable")};
            import ${getClassPath("Consumer")};
            import ${getClassPath("CompositeException")};
            import ${getClassPath("DisposableHelper")};
            
            import ${getClassPath("Scheduler")}.Worker;
            import ${getClassPath("SimpleQueue")};
            import ${getClassPath("SpscLinkedArrayQueue")};
            
            import okhttp3.Response;
            import rxhttp.wrapper.annotations.NonNull;
            import rxhttp.wrapper.annotations.Nullable;
            import rxhttp.wrapper.callback.ProgressCallback;
            import rxhttp.wrapper.entity.Progress;
            import rxhttp.wrapper.entity.ProgressT;
            import rxhttp.wrapper.parse.IOParser;
            import rxhttp.wrapper.parse.Parser;

            public final class ObservableParser<T> extends Observable<T> {

                private final Parser<T> parser;
                private final ObservableSource<Progress> source;
                private final Scheduler scheduler;
                private final Consumer<Progress> progressConsumer;

                public ObservableParser(@NonNull ObservableSource<Progress> source, @NonNull Parser<T> parser,
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

                        if (progressConsumer != null && parser instanceof IOParser) {
                            ((IOParser) parser).setCallback(this);
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
                    public void onProgress(Progress p) {
                        if (done) {
                            return;
                        }
                        try {
                            progressConsumer.accept(p);
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
                    private final SimpleQueue<Progress> queue;
                    private final Scheduler.Worker worker;

                    private final Consumer<Progress> progressConsumer;

                    AsyncParserObserver(Observer<? super T> actual, Scheduler.Worker worker, Consumer<Progress> progressConsumer, Parser<T> parser) {
                        this.downstream = actual;
                        this.parser = parser;
                        this.worker = worker;
                        this.progressConsumer = progressConsumer;
                        queue = new SpscLinkedArrayQueue<>(128);

                        if (progressConsumer != null && parser instanceof IOParser) {
                            ((IOParser) parser).setCallback(this);
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
                    public void onProgress(Progress p) {
                        if (done) {
                            return;
                        }
                        queue.offer(p);
                        schedule();
                    }

                    @SuppressWarnings("unchecked")
                    @Override
                    public void onNext(Progress progress) {
                        if (done) {
                            return;
                        }
                        ProgressT<T> p = null;
                        if (progress instanceof ProgressT) {
                            ProgressT<Response> progressT = (ProgressT<Response>) progress;
                            try {
                                T t = parser.onParse(progressT.getResult());
                                p = new ProgressT<>(t);
                            } catch (Throwable t) {
                                onError(t);
                                return;
                            }
                        }
                        if (p != null) {
                            queue.offer(p);
                        } else {
                            queue.offer(progress);
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

                        final SimpleQueue<Progress> q = queue;
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

    @JvmStatic
    fun generatorRxHttpBodyParam(filer: Filer) {
        if (!isDependenceRxJava()) {
            generatorClass(filer, "RxHttpBodyParam", """
                package $rxHttpPackage;

                /**
                 * Github
                 * https://github.com/liujingxing/RxHttp
                 * https://github.com/liujingxing/RxLife
                 * https://github.com/liujingxing/okhttp-RxHttp/wiki/FAQ
                 * https://github.com/liujingxing/okhttp-RxHttp/wiki/更新日志
                 */
                @SuppressWarnings("unchecked")
                public class RxHttpBodyParam<P extends BodyParam<P>, R extends RxHttpBodyParam<P, R>> extends RxHttp<P, R> {

                    protected RxHttpBodyParam(P param) {
                        super(param);
                    }

                    public final R setUploadMaxLength(long maxLength) {
                        param.setUploadMaxLength(maxLength);
                        return (R) this;
                    }
                }
            """.trimIndent())
        } else {
            generatorClass(filer, "RxHttpBodyParam", """
                package $rxHttpPackage;
                
                import ${getClassPath("Observable")};
                import ${getClassPath("Scheduler")};
                import ${getClassPath("Consumer")};
                import rxhttp.wrapper.entity.Progress;
                import rxhttp.wrapper.param.BodyParam;
                import rxhttp.wrapper.parse.Parser;
                
                /**
                 * Github
                 * https://github.com/liujingxing/RxHttp
                 * https://github.com/liujingxing/RxLife
                 * https://github.com/liujingxing/okhttp-RxHttp/wiki/FAQ
                 * https://github.com/liujingxing/okhttp-RxHttp/wiki/更新日志
                 */
                @SuppressWarnings("unchecked")
                public class RxHttpBodyParam<P extends BodyParam<P>, R extends RxHttpBodyParam<P, R>> extends RxHttp<P, R> {
                
                  //Controls the downstream callback thread
                  private Scheduler observeOnScheduler;
                
                  //Upload progress callback
                  private Consumer<Progress> progressConsumer;
                
                  protected RxHttpBodyParam(P param) {
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

    @JvmStatic
    fun generatorObservableHttp(filer: Filer) {
        generatorClass(filer, "ObservableHttp", """
                package $rxHttpPackage;


                import java.io.IOException;
                import java.util.concurrent.Callable;

                import ${getClassPath("Observable")};
                import ${getClassPath("Observer")};
                import ${getClassPath("Exceptions")};
                import ${getClassPath("DeferredScalarDisposable")};
                import ${getClassPath("RxJavaPlugins")};
                import okhttp3.Call;
                import okhttp3.OkHttpClient;
                import okhttp3.Request;
                import okhttp3.Response;
                import rxhttp.HttpSender;
                import rxhttp.RxHttpPlugins;
                import rxhttp.wrapper.annotations.NonNull;
                import rxhttp.wrapper.annotations.Nullable;
                import rxhttp.wrapper.cahce.CacheMode;
                import rxhttp.wrapper.cahce.InternalCache;
                import rxhttp.wrapper.exception.CacheReadFailedException;
                import rxhttp.wrapper.param.Param;
                import rxhttp.wrapper.parse.Parser;
                import rxhttp.wrapper.utils.LogUtil;

                /**
                 * 发送Http请求的观察者，管道中断时，请求还未执行完毕，会将请求cancel
                 * User: ljx
                 * Date: 2018/04/20
                 * Time: 11:15
                 */
                final class ObservableHttp<T> extends Observable<T> implements Callable<T> {
                    private final Param param;
                    private final Parser<T> parser;

                    private Call mCall;
                    private Request request;
                    private InternalCache cache;
                    private OkHttpClient okClient;

                    ObservableHttp(OkHttpClient okClient, @NonNull Param param, @NonNull Parser<T> parser) {
                        this.param = param;
                        this.parser = parser;
                        this.okClient = okClient;
                        cache = RxHttpPlugins.getCache();
                    }

                    @Override
                    public void subscribeActual(Observer<? super T> observer) {
                        HttpDisposable d = new HttpDisposable(observer);
                        observer.onSubscribe(d);
                        if (d.isDisposed()) {
                            return;
                        }
                        T value;
                        try {
                            value = requireNonNull(execute(param), "Callable returned null");
                        } catch (Throwable e) {
                            LogUtil.log(param.getUrl(), e);
                            Exceptions.throwIfFatal(e);
                            if (!d.isDisposed()) {
                                observer.onError(e);
                            } else {
                                RxJavaPlugins.onError(e);
                            }
                            return;
                        }
                        d.complete(value);
                    }

                    @Override
                    public T call() throws Exception {
                        return requireNonNull(execute(param), "The callable returned a null value");
                    }


                    //执行请求
                    private T execute(Param param) throws Exception {
                        if (request == null) { //防止失败重试时，重复构造okhttp3.Request对象
                            request = param.buildRequest();
                        }
                        CacheMode cacheMode = param.getCacheMode();
                        if (cacheModeIs(CacheMode.ONLY_CACHE, CacheMode.READ_CACHE_FAILED_REQUEST_NETWORK)) {
                            //读取缓存
                            Response cacheResponse = getCacheResponse(request, param.getCacheValidTime());
                            if (cacheResponse != null) {
                                return parser.onParse(cacheResponse);
                            }
                            if (cacheModeIs(CacheMode.ONLY_CACHE)) //仅读缓存模式下，缓存读取失败，直接抛出异常
                                throw new CacheReadFailedException("Cache read failed");
                        }
                        Call call = mCall = HttpSender.newCall(okClient, request);
                        Response networkResponse = null;
                        try {
                            networkResponse = call.execute();
                            if (cache != null && cacheMode != CacheMode.ONLY_NETWORK) {
                                //非ONLY_NETWORK模式下,请求成功，写入缓存
                                networkResponse = cache.put(networkResponse, param.getCacheKey());
                            }
                        } catch (Exception e) {
                            if (cacheModeIs(CacheMode.REQUEST_NETWORK_FAILED_READ_CACHE)) {
                                //请求失败，读取缓存
                                networkResponse = getCacheResponse(request, param.getCacheValidTime());
                            }
                            if (networkResponse == null)
                                throw e;
                        }
                        return parser.onParse(networkResponse);
                    }

                    private boolean cacheModeIs(CacheMode... cacheModes) {
                        if (cacheModes == null || cache == null) return false;
                        CacheMode cacheMode = param.getCacheMode();
                        for (CacheMode mode : cacheModes) {
                            if (mode == cacheMode) return true;
                        }
                        return false;
                    }
                    
                    private <T> T requireNonNull(T object, String message) {
                        if (object == null) {
                            throw new NullPointerException(message);
                        }
                        return object;
                    }

                    @Nullable
                    private Response getCacheResponse(Request request, long validTime) throws IOException {
                        if (cache == null) return null;
                        Response cacheResponse = cache.get(request, param.getCacheKey());
                        if (cacheResponse != null) {
                            long receivedTime = cacheResponse.receivedResponseAtMillis();
                            if (validTime != -1 && System.currentTimeMillis() - receivedTime > validTime)
                                return null; //缓存过期，返回null
                            return cacheResponse;
                        }
                        return null;
                    }

                    class HttpDisposable extends DeferredScalarDisposable<T> {

                        /**
                         * Constructs a DeferredScalarDisposable by wrapping the Observer.
                         *
                         * @param downstream the Observer to wrap, not null (not verified)
                         */
                        HttpDisposable(Observer<? super T> downstream) {
                            super(downstream);
                        }

                        @Override
                        public void dispose() {
                            cancelRequest(mCall);
                            super.dispose();
                        }
                    }


                    //关闭请求
                    private void cancelRequest(Call call) {
                        if (call != null && !call.isCanceled())
                            call.cancel();
                    }
                }

            """.trimIndent())
    }

    @JvmStatic
    fun generatorObservableUpload(filer: Filer) {
        generatorClass(filer, "ObservableUpload", """
                package $rxHttpPackage;

                import java.util.concurrent.atomic.AtomicInteger;
                import java.util.concurrent.atomic.AtomicReference;

                import ${getClassPath("Observable")};
                import ${getClassPath("ObservableEmitter")};
                import ${getClassPath("Observer")};
                import ${getClassPath("Disposable")};
                import ${getClassPath("Exceptions")};
                import ${getClassPath("Cancellable")};
                import ${getClassPath("CancellableDisposable")};
                import ${getClassPath("DisposableHelper")};
                import ${getClassPath("SimpleQueue")};
                import ${getClassPath("SpscLinkedArrayQueue")};
                import ${getClassPath("AtomicThrowable")};
                import ${getClassPath("ExceptionHelper")};
                import ${getClassPath("RxJavaPlugins")};
                
                import okhttp3.Call;
                import okhttp3.OkHttpClient;
                import okhttp3.Request;
                import okhttp3.Response;
                import rxhttp.HttpSender;
                import rxhttp.wrapper.entity.Progress;
                import rxhttp.wrapper.entity.ProgressT;
                import rxhttp.wrapper.param.IFile;
                import rxhttp.wrapper.param.Param;
                import rxhttp.wrapper.parse.Parser;
                import rxhttp.wrapper.utils.LogUtil;

                final class ObservableUpload<T> extends Observable<Progress> {
                    private final Param param;
                    private final Parser<T> parser;

                    private Call mCall;
                    private Request mRequest;
                    private OkHttpClient okClient;

                    ObservableUpload(OkHttpClient okClient, Param param, final Parser<T> parser) {
                        this.param = param;
                        this.parser = parser;
                        this.okClient = okClient;
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
                            ProgressT<T> completeProgress = new ProgressT<>(); //上传完成回调
                            ((IFile) param).setProgressCallback((progress, currentSize, totalSize) -> {
                                //这里最多回调100次,仅在进度有更新时,才会回调
                                Progress p = new Progress(progress, currentSize, totalSize);
                                if (p.isFinish()) {
                                    //上传完成的回调，需要带上请求返回值，故这里先保存进度
                                    completeProgress.set(p);
                                } else {
                                    emitter.onNext(p);
                                }
                            });
                            completeProgress.setResult(execute(param));
                            emitter.onNext(completeProgress); //最后一次回调Http执行结果
                            emitter.onComplete();
                        } catch (Throwable e) {
                            LogUtil.log(param.getUrl(), e);
                            Exceptions.throwIfFatal(e);
                            emitter.onError(e);
                        }
                    }

                    //执行请求
                    private T execute(Param param) throws Exception {
                        if (mRequest == null) { //防止失败重试时，重复构造okhttp3.Request对象
                            mRequest = param.buildRequest();
                        }
                        Call call = mCall = HttpSender.newCall(okClient, mRequest);
                        Response response = call.execute();
                        return parser.onParse(response);
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
                            if (ExceptionHelper.addThrowable(error, t)) {
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
            """.trimIndent())
    }

    @JvmStatic
    fun generatorObservableDownload(filer: Filer) {
        generatorClass(filer, "ObservableDownload", """
                package $rxHttpPackage;

                import java.util.concurrent.atomic.AtomicInteger;
                import java.util.concurrent.atomic.AtomicReference;
                
                import ${getClassPath("Observable")};
                import ${getClassPath("ObservableEmitter")};
                import ${getClassPath("Observer")};
                import ${getClassPath("Disposable")};
                import ${getClassPath("Exceptions")};
                import ${getClassPath("Cancellable")};
                import ${getClassPath("CancellableDisposable")};
                import ${getClassPath("DisposableHelper")};
                import ${getClassPath("SimpleQueue")};
                import ${getClassPath("SpscLinkedArrayQueue")};
                import ${getClassPath("AtomicThrowable")};
                import ${getClassPath("ExceptionHelper")};
                import ${getClassPath("RxJavaPlugins")};
                import okhttp3.Call;
                import okhttp3.OkHttpClient;
                import okhttp3.Request;
                import okhttp3.Response;
                import rxhttp.HttpSender;
                import rxhttp.wrapper.annotations.NonNull;
                import rxhttp.wrapper.entity.Progress;
                import rxhttp.wrapper.entity.ProgressT;
                import rxhttp.wrapper.parse.DownloadParser;
                import rxhttp.wrapper.utils.LogUtil;

                final class ObservableDownload extends Observable<Progress> {
                    private final Param param;
                    private final String destPath;
                    private final long offsetSize;

                    private Call mCall;
                    private Request mRequest;
                    private OkHttpClient okClient;

                    private int lastProgress; //上次下载进度

                    ObservableDownload(OkHttpClient okClient, Param param, String destPath, long offsetSize) {
                        this.param = param;
                        this.okClient = okClient;
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
                            Response response = execute(param);
                            String filePath = new DownloadParser(destPath, (progress, currentSize, totalSize) -> {
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
                            }).onParse(response);
                            completeProgress.setResult(filePath);
                            emitter.onNext(completeProgress); //最后一次回调文件下载路径
                            emitter.onComplete();
                        } catch (Throwable e) {
                            LogUtil.log(param.getUrl(), e);
                            Exceptions.throwIfFatal(e);
                            emitter.onError(e);
                        }
                    }
                    
                    private Response execute(@NonNull Param param) throws Exception {
                        if (mRequest == null) { //防止失败重试时，重复构造okhttp3.Request对象
                            mRequest = param.buildRequest();
                        }
                        Call call = mCall = HttpSender.newCall(okClient, mRequest);
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
                            if (ExceptionHelper.addThrowable(error, t)) {
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

            """.trimIndent())
    }


    @JvmStatic
    fun generatorClass(filer: Filer, className: String, content: String) {
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