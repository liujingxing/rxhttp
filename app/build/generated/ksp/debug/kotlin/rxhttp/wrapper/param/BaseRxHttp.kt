package rxhttp.wrapper.param

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri

import java.lang.reflect.Type

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
import rxhttp.wrapper.parse.BitmapParser
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

    abstract fun <T> asParser(
        parser: Parser<T>,
        scheduler: Scheduler?,
        progressConsumer: Consumer<Progress>?
    ): Observable<T>

    open fun <T> asParser(parser: Parser<T>) = asParser(parser, null, null)

    fun <T> asClass(type: Class<T>) = asParser(SimpleParser<T>(type))

    fun asString() = asClass(String::class.java)

    fun <K> asMap(kType: Class<K>) = asMap(kType, kType)

    fun <K, V> asMap(kType: Class<K>, vType: Class<V>): Observable<Map<K, V>> {
        val tTypeMap: Type = ParameterizedTypeImpl.getParameterized(MutableMap::class.java, kType, vType)
        return asParser(SimpleParser(tTypeMap))
    }

    fun <T> asList(tType: Class<T>): Observable<List<T>> {
        val tTypeList: Type = ParameterizedTypeImpl[MutableList::class.java, tType]
        return asParser(SimpleParser(tTypeList))
    }
    
    fun asBitmap() = asParser(BitmapParser())
    
    fun asOkResponse() = asParser(OkResponseParser())

    fun asHeaders(): Observable<Headers> =
        asOkResponse()
            .map { response: Response ->
                try {
                    return@map response.headers
                } finally {
                    OkHttpCompat.closeQuietly(response)
                }
            }

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
        asParser(StreamParser(osFactory), scheduler, progressConsumer)

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
            .flatMap { asParser(it, scheduler, progressConsumer) }
}    
