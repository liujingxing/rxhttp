package com.rxhttp.compiler.ksp

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.rxhttp.compiler.getClassPath
import com.rxhttp.compiler.isDependenceRxJava
import com.rxhttp.compiler.rxHttpPackage


/**
 * User: ljx
 * Date: 2020/3/31
 * Time: 23:36
 */
class KClassHelper(private val isAndroidPlatform: Boolean) {

    private fun isAndroid(s: String) = if (isAndroidPlatform) s else ""

    fun generatorStaticClass(codeGenerator: CodeGenerator) {
        generatorBaseRxHttp(codeGenerator)
        generatorRxHttpAbstractBodyParam(codeGenerator)
        generatorRxHttpBodyParam(codeGenerator)
        generatorRxHttpFormParam(codeGenerator)
        generatorRxHttpNoBodyParam(codeGenerator)
        generatorRxHttpJsonParam(codeGenerator)
        generatorRxHttpJsonArrayParam(codeGenerator)
//        if (isDependenceRxJava()) {
//            generatorObservableClass(codeGenerator)
//        }
    }

    private fun generatorObservableClass(codeGenerator: CodeGenerator) {
        generatorObservableCall(codeGenerator)
        generatorObservableCallEnqueue(codeGenerator)
        generatorObservableCallExecute(codeGenerator)
        generatorObservableParser(codeGenerator)
    }

    private fun generatorBaseRxHttp(codeGenerator: CodeGenerator) {
        if (!isDependenceRxJava()) {
            generatorClass(
                codeGenerator, "BaseRxHttp", """
                package $rxHttpPackage

                import rxhttp.wrapper.CallFactory
                import rxhttp.wrapper.coroutines.RangeHeader

                /**
                 * 本类存放asXxx方法(需要单独依赖RxJava，并告知RxHttp依赖的RxJava版本)
                 * 如未生成，请查看 https://github.com/liujingxing/rxhttp/wiki/FAQ
                 * User: ljx
                 * Date: 2020/4/11
                 * Time: 18:15
                 */
                abstract class BaseRxHttp : CallFactory, RangeHeader {

                    
                }
            """.trimIndent()
            )
        } else {
            generatorClass(
                codeGenerator, "BaseRxHttp", """
            package $rxHttpPackage
            ${isAndroid("""
            import android.content.Context
            import android.graphics.Bitmap
            import android.net.Uri
            """)}
            import java.lang.reflect.Type

            import ${getClassPath("Observable")}
            import ${getClassPath("Scheduler")}
            import ${getClassPath("Consumer")}
            import ${getClassPath("RxJavaPlugins")}
            import ${getClassPath("Schedulers")}
            import okhttp3.Headers
            import okhttp3.Response
            import rxhttp.wrapper.CallFactory
            import rxhttp.wrapper.OkHttpCompat
            import rxhttp.wrapper.callback.FileOutputStreamFactory
            import rxhttp.wrapper.callback.OutputStreamFactory
            ${isAndroid("import rxhttp.wrapper.callback.UriOutputStreamFactory")}
            import rxhttp.wrapper.coroutines.RangeHeader
            import rxhttp.wrapper.entity.ParameterizedTypeImpl
            import rxhttp.wrapper.entity.Progress
            ${isAndroid("import rxhttp.wrapper.parse.BitmapParser")}
            import rxhttp.wrapper.parse.OkResponseParser
            import rxhttp.wrapper.parse.Parser
            import rxhttp.wrapper.parse.SimpleParser
            import rxhttp.wrapper.parse.StreamParser
            import rxhttp.wrapper.utils.LogUtil

            /**
             * 本类存放asXxx方法(需要单独依赖RxJava，并告知RxHttp依赖的RxJava版本)
             * 如未生成，请查看 https://github.com/liujingxing/rxhttp/wiki/FAQ
             * User: ljx
             * Date: 2020/4/11
             * Time: 18:15
             */
            abstract class BaseRxHttp : CallFactory, RangeHeader {

                abstract fun <T> asParser(
                    parser: Parser<T>,
                    scheduler: Scheduler?,
                    progressConsumer: Consumer<Progress>?
                ): Observable<T>
            
                open fun <T> asParser(parser: Parser<T>): Observable<T> {
                    return asParser(parser, null, null)
                }
            
                fun <T> asClass(type: Class<T>): Observable<T> {
                    return asParser(SimpleParser(type))
                }
            
                fun asString(): Observable<String> {
                    return asClass(String::class.java)
                }
            
                fun <K> asMap(kType: Class<K>): Observable<Map<K, K>> {
                    return asMap(kType, kType)
                }
            
                fun <K, V> asMap(kType: Class<K>, vType: Class<V>): Observable<Map<K, V>> {
                    val tTypeMap: Type = ParameterizedTypeImpl.getParameterized(MutableMap::class.java, kType, vType)
                    return asParser(SimpleParser(tTypeMap))
                }
            
                fun <T> asList(tType: Class<T>): Observable<List<T>> {
                    val tTypeList: Type = ParameterizedTypeImpl[MutableList::class.java, tType]
                    return asParser(SimpleParser(tTypeList))
                }
                ${isAndroid("""
                fun <T> asBitmap(): Observable<Bitmap> {
                    return asParser(BitmapParser())
                }
                """)}
                fun asOkResponse(): Observable<Response> {
                    return asParser(OkResponseParser())
                }
            
                fun asHeaders(): Observable<Headers> {
                    return asOkResponse()
                        .map { response: Response ->
                            try {
                                return@map response.headers
                            } finally {
                                OkHttpCompat.closeQuietly(response)
                            }
                        }
                }
            
                fun asDownload(
                    destPath: String,
                    progressConsumer: Consumer<Progress>?
                ): Observable<String> {
                    return asDownload(destPath, null, progressConsumer)
                }
            
                @JvmOverloads
                fun asDownload(
                    destPath: String,
                    scheduler: Scheduler? = null,
                    progressConsumer: Consumer<Progress>? = null
                ): Observable<String> {
                    return asDownload(FileOutputStreamFactory(destPath), scheduler, progressConsumer)
                }
                ${isAndroid("""
                @JvmOverloads
                fun asDownload(
                    context: Context,
                    uri: Uri,
                    scheduler: Scheduler? = null,
                    progressConsumer: Consumer<Progress>? = null
                ): Observable<Uri> {
                    return asDownload(UriOutputStreamFactory(context, uri), scheduler, progressConsumer)
                }
                """)}
                fun <T> asDownload(osFactory: OutputStreamFactory<T>): Observable<T> {
                    return asDownload(osFactory, null, null)
                }
            
                fun <T> asDownload(
                    osFactory: OutputStreamFactory<T>,
                    scheduler: Scheduler?,
                    progressConsumer: Consumer<Progress>?
                ): Observable<T> {
                    return asParser(StreamParser(osFactory), scheduler, progressConsumer)
                }
            
                @JvmOverloads
                fun asAppendDownload(
                    destPath: String,
                    scheduler: Scheduler? = null,
                    progressConsumer: Consumer<Progress>? = null
                ): Observable<String> {
                    return asAppendDownload(FileOutputStreamFactory(destPath), scheduler, progressConsumer)
                }
                ${isAndroid("""
                @JvmOverloads
                fun asAppendDownload(
                    context: Context,
                    uri: Uri,
                    scheduler: Scheduler? = null,
                    progressConsumer: Consumer<Progress>? = null
                ): Observable<Uri> {
                    return asAppendDownload(
                        UriOutputStreamFactory(context, uri),
                        scheduler,
                        progressConsumer
                    )
                } 
                """)}
                fun <T> asAppendDownload(osFactory: OutputStreamFactory<T>): Observable<T> {
                    return asAppendDownload(osFactory, null, null)
                }
            
                fun <T> asAppendDownload(
                    osFactory: OutputStreamFactory<T>, scheduler: Scheduler?,
                    progressConsumer: Consumer<Progress>?
                ): Observable<T> {
                    return Observable
                        .fromCallable {
                            val offsetSize = osFactory.offsetSize()
                            if (offsetSize >= 0) setRangeHeader(offsetSize, -1, true)
                            StreamParser(osFactory)
                        }
                        .subscribeOn(Schedulers.io())
                        .flatMap { asParser(it, scheduler, progressConsumer) }
                }
            }    

        """.trimIndent()
            )
        }
    }

    private fun generatorObservableCallEnqueue(codeGenerator: CodeGenerator) {
        generatorClass(
            codeGenerator, "ObservableCallEnqueue", """
            package $rxHttpPackage
 
            import java.io.IOException
            
            import ${getClassPath("Observer")}
            import ${getClassPath("Disposable")}
            import ${getClassPath("Exceptions")}
            import ${getClassPath("RxJavaPlugins")}
            import okhttp3.Call
            import okhttp3.Callback
            import okhttp3.Response
            import rxhttp.wrapper.BodyParamFactory
            import rxhttp.wrapper.CallFactory
            import rxhttp.wrapper.callback.ProgressCallback
            import rxhttp.wrapper.entity.Progress
            import rxhttp.wrapper.entity.ProgressT
            import rxhttp.wrapper.utils.LogUtil

            /**
             * User: ljx
             * Date: 2018/04/20
             * Time: 11:15
             */
            final class ObservableCallEnqueue extends ObservableCall {

                private CallFactory callFactory
                private boolean callbackUploadProgress

                ObservableCallEnqueue(CallFactory callFactory) {
                    this(callFactory, false)
                }

                ObservableCallEnqueue(CallFactory callFactory, boolean callbackUploadProgress) {
                    this.callFactory = callFactory
                    this.callbackUploadProgress = callbackUploadProgress
                }

                @Override
                public void subscribeActual(Observer<? super Progress> observer) {
                    HttpDisposable d = new HttpDisposable(observer, callFactory, callbackUploadProgress)
                    observer.onSubscribe(d)
                    if (d.isDisposed()) {
                        return
                    }
                    d.run()
                }


                private static class HttpDisposable implements Disposable, Callback, ProgressCallback {

                    private volatile boolean disposed

                    private final Call call
                    private final Observer<? super Progress> downstream

                    /**
                     * Constructs a DeferredScalarDisposable by wrapping the Observer.
                     *
                     * @param downstream the Observer to wrap, not null (not verified)
                     */
                    HttpDisposable(Observer<? super Progress> downstream, CallFactory callFactory, boolean callbackUploadProgress) {
                        if (callFactory instanceof BodyParamFactory && callbackUploadProgress) {
                            ((BodyParamFactory) callFactory).getParam().setProgressCallback(this)
                        }
                        this.downstream = downstream
                        this.call = callFactory.newCall()
                    }

                    @Override
                    public void onProgress(int progress, long currentSize, long totalSize) {
                        if (!disposed) {
                            downstream.onNext(new Progress(progress, currentSize, totalSize))
                        }
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        if (!disposed) {
                            downstream.onNext(new ProgressT<>(response))
                        }
                        if (!disposed) {
                            downstream.onComplete()
                        }
                    }

                    @Override
                    public void onFailure(Call call, IOException e) {
                        LogUtil.log(call.request().url().toString(), e)
                        Exceptions.throwIfFatal(e)
                        if (!disposed) {
                            downstream.onError(e)
                        } else {
                            RxJavaPlugins.onError(e)
                        }
                    }

                    @Override
                    public void dispose() {
                        disposed = true
                        call.cancel()
                    }

                    @Override
                    public boolean isDisposed() {
                        return disposed
                    }

                    public void run() {
                        call.enqueue(this)
                    }
                }
            }

        """.trimIndent()
        )
    }

    private fun generatorObservableCallExecute(codeGenerator: CodeGenerator) {
        generatorClass(
            codeGenerator, "ObservableCallExecute", """
            package $rxHttpPackage

            import ${getClassPath("Observer")}
            import ${getClassPath("Disposable")}
            import ${getClassPath("Exceptions")}
            import ${getClassPath("RxJavaPlugins")}
            import okhttp3.Call
            import okhttp3.Response
            import rxhttp.wrapper.BodyParamFactory
            import rxhttp.wrapper.CallFactory
            import rxhttp.wrapper.callback.ProgressCallback
            import rxhttp.wrapper.entity.Progress
            import rxhttp.wrapper.entity.ProgressT
            import rxhttp.wrapper.utils.LogUtil

            /**
             * User: ljx
             * Date: 2018/04/20
             * Time: 11:15
             */
            final class ObservableCallExecute extends ObservableCall {

                private CallFactory callFactory
                private boolean callbackUploadProgress

                ObservableCallExecute(CallFactory callFactory) {
                    this(callFactory, false)
                }

                ObservableCallExecute(CallFactory callFactory, boolean callbackUploadProgress) {
                    this.callFactory = callFactory
                    this.callbackUploadProgress = callbackUploadProgress
                }

                @Override
                public void subscribeActual(Observer<? super Progress> observer) {
                    HttpDisposable d = new HttpDisposable(observer, callFactory, callbackUploadProgress)
                    observer.onSubscribe(d)
                    if (d.isDisposed()) {
                        return
                    }
                    d.run()
                }

                private static class HttpDisposable implements Disposable, ProgressCallback {

                    private boolean fusionMode
                    private volatile boolean disposed

                    private final Call call
                    private final Observer<? super Progress> downstream

                    /**
                     * Constructs a DeferredScalarDisposable by wrapping the Observer.
                     *
                     * @param downstream the Observer to wrap, not null (not verified)
                     */
                    HttpDisposable(Observer<? super Progress> downstream, CallFactory callFactory, boolean callbackUploadProgress) {
                        if (callFactory instanceof BodyParamFactory && callbackUploadProgress) {
                            ((BodyParamFactory) callFactory).getParam().setProgressCallback(this)
                        }
                        this.downstream = downstream
                        this.call = callFactory.newCall()
                    }

                    @Override
                    public void onProgress(int progress, long currentSize, long totalSize) {
                        if (!disposed) {
                            downstream.onNext(new Progress(progress, currentSize, totalSize))
                        }
                    }

                    public void run() {
                        Response value
                        try {
                            value = call.execute()
                        } catch (Throwable e) {
                            LogUtil.log(call.request().url().toString(), e)
                            Exceptions.throwIfFatal(e)
                            if (!disposed) {
                                downstream.onError(e)
                            } else {
                                RxJavaPlugins.onError(e)
                            }
                            return
                        }
                        if (!disposed) {
                            downstream.onNext(new ProgressT<>(value))
                        }
                        if (!disposed) {
                            downstream.onComplete()
                        }
                    }

                    @Override
                    public void dispose() {
                        disposed = true
                        call.cancel()
                    }

                    @Override
                    public boolean isDisposed() {
                        return disposed
                    }
                }
            }

        """.trimIndent()
        )
    }

    private fun generatorObservableCall(codeGenerator: CodeGenerator) {
        generatorClass(
            codeGenerator, "ObservableCall", """
            package $rxHttpPackage

            import ${getClassPath("Observable")}
            import ${getClassPath("Scheduler")}
            import ${getClassPath("Consumer")}
            import rxhttp.wrapper.entity.Progress
            import rxhttp.wrapper.parse.Parser
            
            /**
             * User: ljx
             * Date: 2020/9/5
             * Time: 21:59
             */
            abstract class ObservableCall extends Observable<Progress> {
            
                public <T> Observable<T> asParser(Parser<T> parser) {
                    return asParser(parser, null, null)
                }
            
                public <T> Observable<T> asParser(Parser<T> parser, Consumer<Progress> progressConsumer) {
                    return asParser(parser, null, progressConsumer)
                }
            
                public <T> Observable<T> asParser(Parser<T> parser, Scheduler scheduler, Consumer<Progress> progressConsumer) {
                    return new ObservableParser<>(this, parser, scheduler, progressConsumer)
                }
            }

        """.trimIndent()
        )
    }

    private fun generatorObservableParser(codeGenerator: CodeGenerator) {
        generatorClass(
            codeGenerator, "ObservableParser", """
            package $rxHttpPackage

            import java.util.Objects
            import java.util.concurrent.atomic.AtomicInteger

            import ${getClassPath("Observable")}
            import ${getClassPath("ObservableSource")}
            import ${getClassPath("Observer")}
            import ${getClassPath("Scheduler")}
            import ${getClassPath("Scheduler")}.Worker
            import ${getClassPath("Disposable")}
            import ${getClassPath("Exceptions")}
            import ${getClassPath("Consumer")}
            import ${getClassPath("DisposableHelper")}
            import ${getClassPath("SpscArrayQueue")}
            import ${getClassPath("RxJavaPlugins")}
            import okhttp3.Response
            import rxhttp.wrapper.annotations.NonNull
            import rxhttp.wrapper.annotations.Nullable
            import rxhttp.wrapper.callback.ProgressCallback
            import rxhttp.wrapper.entity.Progress
            import rxhttp.wrapper.entity.ProgressT
            import rxhttp.wrapper.parse.Parser
            import rxhttp.wrapper.parse.StreamParser
            import rxhttp.wrapper.utils.LogUtil

            final class ObservableParser<T> extends Observable<T> {

                private final Parser<T> parser
                private final ObservableSource<Progress> source
                private final Scheduler scheduler
                private final Consumer<Progress> progressConsumer

                ObservableParser(@NonNull ObservableSource<Progress> source, @NonNull Parser<T> parser,
                                        @Nullable Scheduler scheduler, @Nullable Consumer<Progress> progressConsumer) {
                    this.source = source
                    this.parser = parser
                    this.scheduler = scheduler
                    this.progressConsumer = progressConsumer
                }

                @Override
                protected void subscribeActual(@NonNull Observer<? super T> observer) {
                    if (scheduler == null) {
                        source.subscribe(new SyncParserObserver<>(observer, parser, progressConsumer))
                    } else {
                        Worker worker = scheduler.createWorker()
                        source.subscribe(new AsyncParserObserver<>(observer, worker, progressConsumer, parser))
                    }
                }

                private static final class SyncParserObserver<T> implements Observer<Progress>, Disposable, ProgressCallback {
                    private final Parser<T> parser

                    private Disposable upstream
                    private final Observer<? super T> downstream
                    private final Consumer<Progress> progressConsumer
                    private boolean done

                    SyncParserObserver(Observer<? super T> actual, Parser<T> parser, Consumer<Progress> progressConsumer) {
                        this.downstream = actual
                        this.parser = parser
                        this.progressConsumer = progressConsumer

                        if (progressConsumer != null && parser instanceof StreamParser) {
                            ((StreamParser) parser).setProgressCallback(this)
                        }
                    }

                    @Override
                    public void onSubscribe(Disposable d) {
                        if (DisposableHelper.validate(this.upstream, d)) {
                            this.upstream = d
                            downstream.onSubscribe(this)
                        }
                    }

                    //download progress callback
                    @Override
                    public void onProgress(int progress, long currentSize, long totalSize) {
                        if (done) {
                            return
                        }
                        try {
                            //LogUtil.logDownProgress(progress, currentSize, totalSize)
                            progressConsumer.accept(new Progress(progress, currentSize, totalSize))
                        } catch (Throwable t) {
                            fail(t)
                        }
                    }

                    @SuppressWarnings("unchecked")
                    @Override
                    public void onNext(Progress progress) {
                        if (done) {
                            return
                        }
                        if (progress instanceof ProgressT) {
                            ProgressT<Response> p = (ProgressT<Response>) progress
                            T v
                            try {
                                v = Objects.requireNonNull(parser.onParse(p.getResult()), "The onParse function returned a null value.")
                            } catch (Throwable t) {
                                LogUtil.log(p.getResult().request().url().toString(), t)
                                fail(t)
                                return
                            }
                            downstream.onNext(v)
                        } else {
                            try {
                                progressConsumer.accept(progress)
                            } catch (Throwable t) {
                                fail(t)
                            }
                        }
                    }

                    @Override
                    public void onError(Throwable t) {
                        if (done) {
                            RxJavaPlugins.onError(t)
                            return
                        }
                        done = true
                        downstream.onError(t)
                    }

                    @Override
                    public void onComplete() {
                        if (done) {
                            return
                        }
                        done = true
                        downstream.onComplete()
                    }

                    @Override
                    public void dispose() {
                        upstream.dispose()
                    }

                    @Override
                    public boolean isDisposed() {
                        return upstream.isDisposed()
                    }

                    private void fail(Throwable t) {
                        Exceptions.throwIfFatal(t)
                        upstream.dispose()
                        onError(t)
                    }
                }


                private static final class AsyncParserObserver<T> extends AtomicInteger
                    implements Observer<Progress>, Disposable, ProgressCallback, Runnable {

                    private final Parser<T> parser
                    private final Observer<? super T> downstream

                    private Disposable upstream
                    private Throwable error

                    private volatile boolean done
                    private volatile boolean disposed
                    private final SpscArrayQueue<Progress> queue
                    private final Scheduler.Worker worker

                    private final Consumer<Progress> progressConsumer

                    AsyncParserObserver(Observer<? super T> actual, Scheduler.Worker worker, Consumer<Progress> progressConsumer, Parser<T> parser) {
                        this.downstream = actual
                        this.parser = parser
                        this.worker = worker
                        this.progressConsumer = progressConsumer
                        queue = new SpscArrayQueue<>(2)

                        if (progressConsumer != null && parser instanceof StreamParser) {
                            ((StreamParser) parser).setProgressCallback(this)
                        }
                    }

                    @Override
                    public void onSubscribe(@NonNull Disposable d) {
                        if (DisposableHelper.validate(this.upstream, d)) {
                            this.upstream = d
                            downstream.onSubscribe(this)
                        }
                    }

                    //download progress callback
                    @Override
                    public void onProgress(int progress, long currentSize, long totalSize) {
                        if (done) {
                            return
                        }
                        //LogUtil.logDownProgress(progress, currentSize, totalSize)
                        offer(new Progress(progress,currentSize,totalSize))
                    }

                    @SuppressWarnings("unchecked")
                    @Override
                    public void onNext(Progress progress) {
                        if (done) {
                            return
                        }
                        ProgressT<T> pt = null
                        if (progress instanceof ProgressT) {
                            ProgressT<Response> progressT = (ProgressT<Response>) progress
                            try {
                                T t = Objects.requireNonNull(parser.onParse(progressT.getResult()), "The onParse function returned a null value.")
                                pt = new ProgressT<>(t)
                            } catch (Throwable t) {
                                LogUtil.log(progressT.getResult().request().url().toString(), t)
                                onError(t)
                                return
                            }
                        }
                        Progress p = pt != null ? pt : progress
                        offer(p)
                    }
                    
                    private void offer(Progress p) {
                        if (!queue.offer(p)) {
                            queue.poll()
                            queue.offer(p)
                        }
                        schedule()
                    }

                    @Override
                    public void onError(Throwable t) {
                        if (done) {
                            RxJavaPlugins.onError(t)
                            return
                        }
                        error = t
                        done = true
                        schedule()
                    }

                    @Override
                    public void onComplete() {
                        if (done) {
                            return
                        }
                        done = true
                        schedule()
                    }


                    void schedule() {
                        if (getAndIncrement() == 0) {
                            worker.schedule(this)
                        }
                    }

                    @SuppressWarnings("unchecked")
                    @Override
                    public void run() {
                        int missed = 1

                        final SpscArrayQueue<Progress> q = queue
                        final Observer<? super T> a = downstream
                        while (!checkTerminated(done, q.isEmpty(), a)) {
                            for (  ) {
                                boolean d = done
                                Progress p
                                try {
                                    p = q.poll()

                                    boolean empty = p == null

                                    if (checkTerminated(d, empty, a)) {
                                        return
                                    }
                                    if (empty) {
                                        break
                                    }
                                    if (p instanceof ProgressT) {
                                        a.onNext(((ProgressT<T>) p).getResult())
                                    } else {
                                        progressConsumer.accept(p)
                                    }
                                } catch (Throwable ex) {
                                    Exceptions.throwIfFatal(ex)
                                    disposed = true
                                    upstream.dispose()
                                    q.clear()
                                    a.onError(ex)
                                    worker.dispose()
                                    return
                                }
                            }
                            missed = addAndGet(-missed)
                            if (missed == 0) {
                                break
                            }
                        }
                    }

                    boolean checkTerminated(boolean d, boolean empty, Observer<? super T> a) {
                        if (isDisposed()) {
                            queue.clear()
                            return true
                        }
                        if (d) {
                            Throwable e = error
                            if (e != null) {
                                disposed = true
                                queue.clear()
                                a.onError(e)
                                worker.dispose()
                                return true
                            } else if (empty) {
                                disposed = true
                                a.onComplete()
                                worker.dispose()
                                return true
                            }
                        }
                        return false
                    }

                    @Override
                    public void dispose() {
                        if (!disposed) {
                            disposed = true
                            upstream.dispose()
                            worker.dispose()
                            if (getAndIncrement() == 0) {
                                queue.clear()
                            }
                        }
                    }

                    @Override
                    public boolean isDisposed() {
                        return disposed
                    }
                }
            }

        """.trimIndent()
        )
    }

    private fun generatorRxHttpAbstractBodyParam(codeGenerator: CodeGenerator) {
        if (!isDependenceRxJava()) {
            generatorClass(
                codeGenerator, "RxHttpAbstractBodyParam", """
                package $rxHttpPackage
                
                import rxhttp.wrapper.BodyParamFactory
                import rxhttp.wrapper.param.AbstractBodyParam

                /**
                 * Github
                 * https://github.com/liujingxing/rxhttp
                 * https://github.com/liujingxing/rxlife
                 * https://github.com/liujingxing/rxhttp/wiki/FAQ
                 * https://github.com/liujingxing/rxhttp/wiki/更新日志
                 */
                @SuppressWarnings("unchecked")
                open class RxHttpAbstractBodyParam<P : AbstractBodyParam<P>, R : RxHttpAbstractBodyParam<P, R>> 
                protected constructor(
                    param: P
                ) : RxHttp<P, R>(param), BodyParamFactory {

                    fun setUploadMaxLength(maxLength: Long): R {
                        param.setUploadMaxLength(maxLength)
                        return this as R
                    }
                }
            """.trimIndent()
            )
        } else {
            generatorClass(
                codeGenerator, "RxHttpAbstractBodyParam", """
                package $rxHttpPackage
                
                import ${getClassPath("Observable")}
                import ${getClassPath("Scheduler")}
                import ${getClassPath("Consumer")}
                import rxhttp.wrapper.BodyParamFactory
                import rxhttp.wrapper.entity.Progress
                import rxhttp.wrapper.param.AbstractBodyParam
                import rxhttp.wrapper.parse.Parser
                
                /**
                 * Github
                 * https://github.com/liujingxing/rxhttp
                 * https://github.com/liujingxing/rxlife
                 * https://github.com/liujingxing/rxhttp/wiki/FAQ
                 * https://github.com/liujingxing/rxhttp/wiki/更新日志
                 */
                open class RxHttpAbstractBodyParam<P : AbstractBodyParam<P>, R : RxHttpAbstractBodyParam<P, R>> 
                protected constructor(
                    param: P
                ) : RxHttp<P, R>(param), BodyParamFactory {
                    //Controls the downstream callback thread
                    private var observeOnScheduler: Scheduler? = null
                
                    //Upload progress callback
                    private var progressConsumer: Consumer<Progress>? = null
                    
                    fun setUploadMaxLength(maxLength: Long): R {
                        param.setUploadMaxLength(maxLength)
                        return this as R
                    }
                
                    fun upload(progressConsumer: Consumer<Progress>?): R {
                        return upload(null, progressConsumer)
                    }
                
                    /**
                     * @param progressConsumer   Upload progress callback
                     * @param observeOnScheduler Controls the downstream callback thread
                     */
                    fun upload(observeOnScheduler: Scheduler?, progressConsumer: Consumer<Progress>?): R {
                        this.progressConsumer = progressConsumer
                        this.observeOnScheduler = observeOnScheduler
                        return this as R
                    }
                
                    override fun <T> asParser(parser: Parser<T>): Observable<T> {
                        return asParser(parser, observeOnScheduler, progressConsumer)
                    }
                
                    override fun <T> asParser(
                        parser: Parser<T>,
                        scheduler: Scheduler?,
                        progressConsumer: Consumer<Progress>?
                    ): Observable<T> {
                        if (progressConsumer == null) {
                            return super.asParser(parser, scheduler, null)
                        }
                        val observableCall: ObservableCall
                        observableCall = if (isAsync) {
                            ObservableCallEnqueue(this, true)
                        } else {
                            ObservableCallExecute(this, true)
                        }
                        return observableCall.asParser(parser, scheduler, progressConsumer)
                    }
                }

        """.trimIndent()
            )
        }
    }

    private fun generatorRxHttpNoBodyParam(codeGenerator: CodeGenerator) {
        generatorClass(
            codeGenerator, "RxHttpNoBodyParam", """
            package $rxHttpPackage

            import rxhttp.wrapper.param.NoBodyParam

            /**
             * Github
             * https://github.com/liujingxing/rxhttp
             * https://github.com/liujingxing/rxlife
             * https://github.com/liujingxing/rxhttp/wiki/FAQ
             * https://github.com/liujingxing/rxhttp/wiki/更新日志
             */
            open class RxHttpNoBodyParam(param: NoBodyParam) : RxHttp<NoBodyParam, RxHttpNoBodyParam>(param) {
            
                @JvmOverloads
                fun add(key: String, value: Any?, isAdd: Boolean = true): RxHttpNoBodyParam {
                    if (isAdd) addQuery(key, value)
                    return this
                }
            
                fun addAll(map: Map<String, *>) = addAllQuery(map)
            
                fun addEncoded(key: String, value: Any?) = addEncodedQuery(key, value)
            
                fun addAllEncoded(map: Map<String, *>) = addAllEncodedQuery(map)
            }

        """.trimIndent()
        )
    }


    private fun generatorRxHttpBodyParam(codeGenerator: CodeGenerator) {
        generatorClass(
            codeGenerator, "RxHttpBodyParam", """
            package $rxHttpPackage
            ${isAndroid("""
            import android.content.Context
            import android.net.Uri
            import rxhttp.wrapper.utils.asRequestBody
            """)}
            import okhttp3.MediaType
            import okhttp3.RequestBody
            import okio.ByteString
            import rxhttp.wrapper.annotations.Nullable
            import rxhttp.wrapper.param.BodyParam
            import java.io.File

            /**
             * Github
             * https://github.com/liujingxing/rxhttp
             * https://github.com/liujingxing/rxlife
             */
            open class RxHttpBodyParam(param: BodyParam) : RxHttpAbstractBodyParam<BodyParam, RxHttpBodyParam>(param) {
                fun setBody(requestBody: RequestBody): RxHttpBodyParam {
                    param.setBody(requestBody)
                    return this
                }
            
                fun setBody(content: String, @Nullable mediaType: MediaType?): RxHttpBodyParam {
                    param.setBody(content, mediaType)
                    return this
                }
            
                fun setBody(content: ByteString, @Nullable mediaType: MediaType?): RxHttpBodyParam {
                    param.setBody(content, mediaType)
                    return this
                }
            
                fun setBody(content: ByteArray, @Nullable mediaType: MediaType?): RxHttpBodyParam {
                    param.setBody(content, mediaType)
                    return this
                }
            
                fun setBody(
                    content: ByteArray,
                    @Nullable mediaType: MediaType?,
                    offset: Int,
                    byteCount: Int
                ): RxHttpBodyParam {
                    param.setBody(content, mediaType, offset, byteCount)
                    return this
                }
            
                fun setBody(file: File): RxHttpBodyParam {
                    param.setBody(file)
                    return this
                }
            
                fun setBody(file: File, @Nullable mediaType: MediaType?): RxHttpBodyParam {
                    param.setBody(file, mediaType)
                    return this
                }
                ${isAndroid("""
                fun setBody(uri: Uri, context: Context): RxHttpBodyParam {
                    param.setBody(uri.asRequestBody(context))
                    return this
                }
            
                fun setBody(uri: Uri, context: Context, @Nullable contentType: MediaType?): RxHttpBodyParam {
                    param.setBody(uri.asRequestBody(context, 0, contentType))
                    return this
                }
                """)}
            
                fun setBody(any: Any): RxHttpBodyParam {
                    param.setBody(any)
                    return this
                }
            
                @Deprecated("please user {@link #setBody(Object)} instead")
                fun setJsonBody(any: Any): RxHttpBodyParam {
                    return setBody(any)
                }
            }

        """.trimIndent()
        )
    }

    private fun generatorRxHttpFormParam(codeGenerator: CodeGenerator) {
        generatorClass(
            codeGenerator, "RxHttpFormParam", """
            package $rxHttpPackage

            ${isAndroid("import android.content.Context")}
            ${isAndroid("import android.net.Uri")}

            import java.io.File

            import okhttp3.Headers
            import okhttp3.MediaType
            import okhttp3.MultipartBody
            import okhttp3.MultipartBody.Part
            import okhttp3.RequestBody
            import rxhttp.wrapper.annotations.NonNull
            import rxhttp.wrapper.annotations.Nullable
            import rxhttp.wrapper.entity.UpFile
            import rxhttp.wrapper.param.FormParam
            import rxhttp.wrapper.utils.asPart
            import rxhttp.wrapper.utils.asRequestBody
            import rxhttp.wrapper.utils.displayName

            /**
             * Github
             * https://github.com/liujingxing/rxhttp
             * https://github.com/liujingxing/rxlife
             * https://github.com/liujingxing/rxhttp/wiki/FAQ
             * https://github.com/liujingxing/rxhttp/wiki/更新日志
             */
            open class RxHttpFormParam(param: FormParam) : RxHttpAbstractBodyParam<FormParam, RxHttpFormParam>(param) {
                
                @JvmOverloads
                fun add(key: String, value: Any?, isAdd: Boolean = true): RxHttpFormParam {
                    if (isAdd) param.add(key, value)
                    return this
                }
            
                fun addAll(map: Map<String, *>): RxHttpFormParam {
                    param.addAll(map)
                    return this
                }
            
                fun addEncoded(key: String, value: Any?): RxHttpFormParam {
                    param.addEncoded(key, value)
                    return this
                }
            
                fun addAllEncoded(map: Map<String, *>): RxHttpFormParam {
                    param.addAllEncoded(map)
                    return this
                }
            
                fun removeAllBody(): RxHttpFormParam {
                    param.removeAllBody()
                    return this
                }
            
                fun removeAllBody(key: String): RxHttpFormParam {
                    param.removeAllBody(key)
                    return this
                }
            
                operator fun set(key: String, value: Any?): RxHttpFormParam {
                    param[key] = value
                    return this
                }
            
                fun setEncoded(key: String, value: Any?): RxHttpFormParam {
                    param.setEncoded(key, value)
                    return this
                }
            
                fun addFile(key: String, file: File): RxHttpFormParam {
                    param.addFile(key, file)
                    return this
                }
            
                fun addFile(key: String, filePath: String): RxHttpFormParam {
                    param.addFile(key, filePath)
                    return this
                }
            
                fun addFile(key: String, file: File, filename: String): RxHttpFormParam {
                    param.addFile(key, file, filename)
                    return this
                }
            
                fun addFile(file: UpFile): RxHttpFormParam {
                    param.addFile(file)
                    return this
                }
            
                @Deprecated("please user {@link #addFiles(List)} instead")
                fun addFile(fileList: List<UpFile>): RxHttpFormParam {
                    return addFiles(fileList)
                }
            
                @Deprecated("please user {@link #addFiles(String, List)} instead")
                fun <T> addFile(key: String, fileList: List<T>): RxHttpFormParam {
                    return addFiles(key, fileList)
                }
            
                fun addFiles(fileList: List<UpFile>): RxHttpFormParam {
                    param.addFiles(fileList)
                    return this
                }
            
                fun <T> addFiles(fileMap: Map<String, T>): RxHttpFormParam {
                    param.addFiles(fileMap)
                    return this
                }
            
                fun <T> addFiles(key: String, fileList: List<T>): RxHttpFormParam {
                    param.addFiles(key, fileList)
                    return this
                }
            
                fun addPart(contentType: MediaType?, content: ByteArray): RxHttpFormParam {
                    param.addPart(contentType, content)
                    return this
                }
            
                fun addPart(
                    contentType: MediaType?, 
                    content: ByteArray, 
                    offset: Int,
                    byteCount: Int
                ): RxHttpFormParam {
                    param.addPart(contentType, content, offset, byteCount)
                    return this
                }
                ${isAndroid("""
                fun addPart(context: Context, uri: Uri): RxHttpFormParam {
                    param.addPart(uri.asRequestBody(context))
                    return this
                }
            
                fun addPart(context: Context, key: String, uri: Uri): RxHttpFormParam {
                    param.addPart(uri.asPart(context, key))
                    return this
                }
            
                fun addPart(context: Context, key: String, fileName: String?, uri: Uri): RxHttpFormParam {
                    param.addPart(uri.asPart(context, key, fileName))
                    return this
                }
            
                fun addPart(context: Context, uri: Uri, contentType: MediaType?): RxHttpFormParam {
                    param.addPart(uri.asRequestBody(context, 0, contentType))
                    return this
                }
            
                fun addPart(
                    context: Context,
                    key: String,
                    uri: Uri,
                    contentType: MediaType?
                ): RxHttpFormParam {
                    param.addPart(uri.asPart(context, key, uri.displayName(context), 0, contentType))
                    return this
                }
            
                fun addPart(
                    context: Context,
                    key: String,
                    filename: String,
                    uri: Uri,
                    contentType: MediaType?
                ): RxHttpFormParam {
                    param.addPart(uri.asPart(context, key, filename, 0, contentType))
                    return this
                }
            
                fun addParts(context: Context, uriMap: Map<String, Uri>): RxHttpFormParam {
                    for ((key, value) in uriMap) {
                        addPart(context, key, value)
                    }
                    return this
                }
            
                fun addParts(context: Context, uris: List<Uri>): RxHttpFormParam {
                    for (uri in uris) {
                        addPart(context, uri)
                    }
                    return this
                }
            
                fun addParts(context: Context, key: String, uris: List<Uri>): RxHttpFormParam {
                    for (uri in uris) {
                        addPart(context, key, uri)
                    }
                    return this
                }
            
                fun addParts(
                    context: Context, 
                    uris: List<Uri>,
                    contentType: MediaType?
                ): RxHttpFormParam {
                    for (uri in uris) {
                        addPart(context, uri, contentType)
                    }
                    return this
                }
            
                fun addParts(
                    context: Context,
                    key: String, 
                    uris: List<Uri>,
                    contentType: MediaType?
                ): RxHttpFormParam {
                    for (uri in uris) {
                        addPart(context, key, uri, contentType)
                    }
                    return this
                }
                """)}
                fun addPart(part: MultipartBody.Part): RxHttpFormParam {
                    param.addPart(part)
                    return this
                }
            
                fun addPart(requestBody: RequestBody?): RxHttpFormParam {
                    param.addPart(requestBody)
                    return this
                }
            
                fun addPart(headers: Headers?, requestBody: RequestBody?): RxHttpFormParam {
                    param.addPart(headers, requestBody)
                    return this
                }
            
                fun addFormDataPart(
                    key: String,
                    fileName: String?,
                    requestBody: RequestBody?
                ): RxHttpFormParam {
                    param.addFormDataPart(key, fileName, requestBody)
                    return this
                }
            
                //Set content-type to multipart/form-data
                fun setMultiForm(): RxHttpFormParam {
                    param.setMultiForm()
                    return this
                }
            
                //Set content-type to multipart/mixed
                fun setMultiMixed(): RxHttpFormParam {
                    param.setMultiMixed()
                    return this
                }
            
                //Set content-type to multipart/alternative
                fun setMultiAlternative(): RxHttpFormParam {
                    param.setMultiAlternative()
                    return this
                }
            
                //Set content-type to multipart/digest
                fun setMultiDigest(): RxHttpFormParam {
                    param.setMultiDigest()
                    return this
                }
            
                //Set content-type to multipart/parallel
                fun setMultiParallel(): RxHttpFormParam {
                    param.setMultiParallel()
                    return this
                }
            
                //Set the MIME type
                fun setMultiType(multiType: MediaType?): RxHttpFormParam {
                    param.setMultiType(multiType)
                    return this
                }
            }

        """.trimIndent()
        )
    }

    private fun generatorRxHttpJsonParam(codeGenerator: CodeGenerator) {
        generatorClass(
            codeGenerator, "RxHttpJsonParam", """
            package $rxHttpPackage

            import com.google.gson.JsonObject
            
            import rxhttp.wrapper.param.JsonParam
            /**
             * Github
             * https://github.com/liujingxing/rxhttp
             * https://github.com/liujingxing/rxlife
             * https://github.com/liujingxing/rxhttp/wiki/FAQ
             * https://github.com/liujingxing/rxhttp/wiki/更新日志
             */
            open class RxHttpJsonParam(param: JsonParam) : RxHttpAbstractBodyParam<JsonParam, RxHttpJsonParam>(param) {
            
                @JvmOverloads
                fun add(key: String, value: Any?, isAdd: Boolean = true): RxHttpJsonParam {
                    if (isAdd) param.add(key, value)
                    return this
                }
            
                fun addAll(map: Map<String, *>): RxHttpJsonParam {
                    param.addAll(map)
                    return this
                }
            
                /**
                 * 将Json对象里面的key-value逐一取出，添加到另一个Json对象中，
                 * 输入非Json对象将抛出[IllegalStateException]异常
                 */
                fun addAll(jsonObject: String): RxHttpJsonParam {
                    param.addAll(jsonObject)
                    return this
                }
            
                /**
                 * 将Json对象里面的key-value逐一取出，添加到另一个Json对象中
                 */
                fun addAll(jsonObject: JsonObject): RxHttpJsonParam {
                    param.addAll(jsonObject)
                    return this
                }
            
                /**
                 * 添加一个JsonElement对象(Json对象、json数组等)
                 */
                fun addJsonElement(key: String, jsonElement: String): RxHttpJsonParam {
                    param.addJsonElement(key, jsonElement)
                    return this
                }
            }

        """.trimIndent()
        )
    }

    private fun generatorRxHttpJsonArrayParam(codeGenerator: CodeGenerator) {
        generatorClass(
            codeGenerator, "RxHttpJsonArrayParam", """
            package $rxHttpPackage

            import com.google.gson.JsonArray
            import com.google.gson.JsonObject
            
            import rxhttp.wrapper.param.JsonArrayParam

            /**
             * Github
             * https://github.com/liujingxing/rxhttp
             * https://github.com/liujingxing/rxlife
             * https://github.com/liujingxing/rxhttp/wiki/FAQ
             * https://github.com/liujingxing/rxhttp/wiki/更新日志
             */
            class RxHttpJsonArrayParam(param: JsonArrayParam) : RxHttpAbstractBodyParam<JsonArrayParam, RxHttpJsonArrayParam>(param) {
            
                @JvmOverloads
                fun add(key: String, value: Any?, isAdd: Boolean = true): RxHttpJsonArrayParam {
                    if (isAdd) param.add(key, value)
                    return this
                }
            
                fun addAll(map: Map<String, *>): RxHttpJsonArrayParam {
                    param.addAll(map)
                    return this
                }
            
                fun add(any: Any): RxHttpJsonArrayParam {
                    param.add(any)
                    return this
                }
            
                fun addAll(list: List<*>): RxHttpJsonArrayParam {
                    param.addAll(list)
                    return this
                }
            
                /**
                 * 添加多个对象，将字符串转JsonElement对象,并根据不同类型,执行不同操作,可输入任意非空字符串
                 */
                fun addAll(jsonElement: String): RxHttpJsonArrayParam {
                    param.addAll(jsonElement)
                    return this
                }
            
                fun addAll(jsonArray: JsonArray): RxHttpJsonArrayParam {
                    param.addAll(jsonArray)
                    return this
                }
            
                /**
                 * 将Json对象里面的key-value逐一取出，添加到Json数组中，成为单独的对象
                 */
                fun addAll(jsonObject: JsonObject): RxHttpJsonArrayParam {
                    param.addAll(jsonObject)
                    return this
                }
            
                fun addJsonElement(jsonElement: String): RxHttpJsonArrayParam {
                    param.addJsonElement(jsonElement)
                    return this
                }
            
                /**
                 * 添加一个JsonElement对象(Json对象、json数组等)
                 */
                fun addJsonElement(key: String, jsonElement: String): RxHttpJsonArrayParam {
                    param.addJsonElement(key, jsonElement)
                    return this
                }
            }

        """.trimIndent()
        )
    }

    private fun generatorClass(codeGenerator: CodeGenerator, className: String, content: String) {
        codeGenerator.createNewFile(
            Dependencies(false),
            rxHttpPackage,
            className,
        ).use {
            it.write(content.toByteArray())
        }
    }
}