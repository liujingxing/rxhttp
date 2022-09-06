package rxhttp.wrapper.param

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri

import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Scheduler
import io.reactivex.rxjava3.functions.Consumer
import io.reactivex.rxjava3.plugins.RxJavaPlugins
import io.reactivex.rxjava3.schedulers.Schedulers
import okhttp3.Headers
import okhttp3.Response
import rxhttp.wrapper.CallFactory
import rxhttp.wrapper.OkHttpCompat
import rxhttp.wrapper.callback.FileOutputStreamFactory
import rxhttp.wrapper.callback.OutputStreamFactory
import rxhttp.wrapper.callback.UriOutputStreamFactory
import rxhttp.wrapper.coroutines.RangeHeader
import rxhttp.wrapper.entity.ParameterizedTypeImpl
import rxhttp.wrapper.entity.Progress
import rxhttp.wrapper.parse.Parser
import rxhttp.wrapper.parse.SimpleParser
import rxhttp.wrapper.parse.StreamParser
import rxhttp.wrapper.utils.LogUtil
import java.lang.reflect.Type

/**
 * 本类存放asXxx方法(需要单独依赖RxJava，并告知RxHttp依赖的RxJava版本)
 * 如未生成，请查看 https://github.com/liujingxing/rxhttp/wiki/FAQ
 * User: ljx
 * Date: 2020/4/11
 * Time: 18:15
 */
@Suppress("UPPER_BOUND_VIOLATED_BASED_ON_JAVA_ANNOTATIONS")
abstract class BaseRxHttp : CallFactory, RangeHeader {

    companion object {
        init {
            val errorHandler = RxJavaPlugins.getErrorHandler()
            if (errorHandler == null) {
                /*                                                                     
                 RxJava2的一个重要的设计理念是：不吃掉任何一个异常, 即抛出的异常无人处理，便会导致程序崩溃                      
                 这就会导致一个问题，当RxJava2“downStream”取消订阅后，“upStream”仍有可能抛出异常，                
                 这时由于已经取消订阅，“downStream”无法处理异常，此时的异常无人处理，便会导致程序崩溃                       
                */
                RxJavaPlugins.setErrorHandler { LogUtil.log(it) }
            }
        }
    }

    protected fun <T> asObservable(
        parser: Parser<T>,
        scheduler: Scheduler? = null,
        progressConsumer: Consumer<Progress>? = null
    ) = ObservableParser(this, parser, scheduler, progressConsumer)

    fun <T> asParser(parser: Parser<T>) = asObservable(parser)

    fun <T> asClass(type: Class<T>) = asParser(SimpleParser<T>(type))

    fun asString() = asClass(String::class.java)

    fun <V> asMapString(vType: Class<V>) =
        asParser(SimpleParser<Map<String, V>>(ParameterizedTypeImpl.getParameterized(MutableMap::class.java, String::class.java, vType)))

    fun <T> asList(tType: Class<T>) =
        asParser(SimpleParser<List<T>>(ParameterizedTypeImpl[MutableList::class.java, tType]))
        
    
    fun asBitmap() = asClass(Bitmap::class.java)
    
    fun asOkResponse() = asClass(Response::class.java)

    fun asHeaders() =  asClass(Headers::class.java)

    fun asDownload(
        destPath: String,
        progressConsumer: Consumer<Progress>?
    ): Observable<String> =
        asDownload(destPath, null, progressConsumer)

    @JvmOverloads
    fun asDownload(
        destPath: String,
        scheduler: Scheduler? = null,
        progressConsumer: Consumer<Progress>? = null
    ): Observable<String> =
        asDownload(FileOutputStreamFactory(destPath), scheduler, progressConsumer)
    
    @JvmOverloads
    fun asDownload(
        context: Context,
        uri: Uri,
        scheduler: Scheduler? = null,
        progressConsumer: Consumer<Progress>? = null
    ): Observable<Uri> =
        asDownload(UriOutputStreamFactory(context, uri), scheduler, progressConsumer)
    
    @JvmOverloads
    fun <T> asDownload(
        osFactory: OutputStreamFactory<T>,
        scheduler: Scheduler? = null,
        progressConsumer: Consumer<Progress>? = null
    ): Observable<T> =
        asObservable(StreamParser(osFactory), scheduler, progressConsumer)

    @JvmOverloads
    fun asAppendDownload(
        destPath: String,
        scheduler: Scheduler? = null,
        progressConsumer: Consumer<Progress>? = null
    ): Observable<String> =
        asAppendDownload(FileOutputStreamFactory(destPath), scheduler, progressConsumer)
    
    @JvmOverloads
    fun asAppendDownload(
        context: Context,
        uri: Uri,
        scheduler: Scheduler? = null,
        progressConsumer: Consumer<Progress>? = null
    ): Observable<Uri> =
        asAppendDownload(UriOutputStreamFactory(context, uri), scheduler, progressConsumer)
    
    @JvmOverloads
    fun <T> asAppendDownload(
        osFactory: OutputStreamFactory<T>, 
        scheduler: Scheduler? = null,
        progressConsumer: Consumer<Progress>? = null
    ): Observable<T> =
        Observable
            .fromCallable {
                val offsetSize = osFactory.offsetSize()
                if (offsetSize >= 0) setRangeHeader(offsetSize, -1, true)
                StreamParser(osFactory)
            }
            .subscribeOn(Schedulers.io())
            .flatMap { asObservable(it, scheduler, progressConsumer) }
}    
