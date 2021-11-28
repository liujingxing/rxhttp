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
    
    fun <T> asBitmap(): Observable<Bitmap> {
        return asParser(BitmapParser())
    }
    
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
    
    @JvmOverloads
    fun asDownload(
        context: Context,
        uri: Uri,
        scheduler: Scheduler? = null,
        progressConsumer: Consumer<Progress>? = null
    ): Observable<Uri> {
        return asDownload(UriOutputStreamFactory(context, uri), scheduler, progressConsumer)
    }
    
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
