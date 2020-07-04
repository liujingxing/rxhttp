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
    fun generatorBaseRxHttp(filer: Filer) {
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

            import android.graphics.Bitmap;

            import java.lang.reflect.Type;
            import java.util.List;
            import java.util.Map;

            import ${getClassPath("Observable")};
            import ${getClassPath("Scheduler")};
            import ${getClassPath("Consumer")};
            import okhttp3.Headers;
            import okhttp3.Response;
            import rxhttp.IRxHttp;
            import rxhttp.wrapper.OkHttpCompat;
            import rxhttp.wrapper.annotations.Nullable;
            import rxhttp.wrapper.entity.ParameterizedTypeImpl;
            import rxhttp.wrapper.entity.Progress;
            import rxhttp.wrapper.parse.BitmapParser;
            import rxhttp.wrapper.parse.DownloadParser;
            import rxhttp.wrapper.parse.OkResponseParser;
            import rxhttp.wrapper.parse.Parser;
            import rxhttp.wrapper.parse.SimpleParser;

            /**
             * 本类存放asXxx方法，如果依赖了RxJava的话
             * User: ljx
             * Date: 2020/4/11
             * Time: 18:15
             */
            public abstract class BaseRxHttp implements IRxHttp {

                public abstract <T> Observable<T> asParser(Parser<T> parser);
                
                /**                                                           
                 * 监听下载进度时，调用此方法                                              
                 *                                                                                                          
                 * @param destPath           文件存储路径                                                                         
                 * @param observeOnScheduler 控制回调所在线程，传入null，则默认在请求所在线程(子线程)回调                                              
                 * @param progressConsumer   进度回调                                                                           
                 * @return Observable                                                                                       
                 */                                                                                                          
                public abstract Observable<String> asDownload(String destPath,
                                                              @Nullable Scheduler observeOnScheduler,
                                                              Consumer<Progress> progressConsumer);      

                /**
                 * @deprecated please user {@link BaseRxHttp#asDownload(String,Scheduler,Consumer)} instead
                 */
                @Deprecated
                public final Observable<String> asDownload(String destPath,
                                                     Consumer<Progress> progressConsumer,
                                                     @Nullable Scheduler observeOnScheduler) {
                    return asDownload(destPath, observeOnScheduler, progressConsumer);                                          
                }

                /**
                 * @deprecated please user {@link BaseRxHttp#asClass(Class)} instead
                 */
                @Deprecated
                public final <T> Observable<T> asObject(Class<T> type) {
                    return asClass(type);
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

                public final <T> Observable<Bitmap> asBitmap() {
                    return asParser(new BitmapParser());
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
                    return asParser(new DownloadParser(destPath));
                }

                public final Observable<String> asDownload(String destPath,
                                                           Consumer<Progress> progressConsumer) {
                    return asDownload(destPath, null, progressConsumer);
                }

            }

        """.trimIndent())
        }
    }

    @JvmStatic
    fun generatorObservableErrorHandler(filer: Filer) {
        generatorClass(filer, "ObservableErrorHandler", """
                package $rxHttpPackage;

                import ${getClassPath("Observable")};
                import ${getClassPath("Consumer")};
                import ${getClassPath("RxJavaPlugins")};
                import rxhttp.wrapper.utils.LogUtil;

                /**
                 * User: ljx
                 * Date: 2020/4/11
                 * Time: 16:19
                 */
                public abstract class ObservableErrorHandler<T> extends Observable<T> {
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
                }

            """.trimIndent())
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
                final class ObservableHttp<T> extends ObservableErrorHandler<T> implements Callable<T> {
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

                final class ObservableUpload<T> extends ObservableErrorHandler<Progress> {
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
                import rxhttp.wrapper.callback.ProgressCallback;
                import rxhttp.wrapper.entity.Progress;
                import rxhttp.wrapper.entity.ProgressT;
                import rxhttp.wrapper.param.Param;
                import rxhttp.wrapper.parse.DownloadParser;
                import rxhttp.wrapper.utils.LogUtil;

                final class ObservableDownload extends ObservableErrorHandler<Progress> {
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
                        Call call = mCall = HttpSender.newCall(HttpSender.clone(okClient, callback), mRequest);
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