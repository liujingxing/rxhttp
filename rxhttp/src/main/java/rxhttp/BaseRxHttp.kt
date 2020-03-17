package rxhttp

import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.functions.Consumer
import okhttp3.Call
import okhttp3.Headers
import okhttp3.OkHttpClient
import rxhttp.wrapper.entity.Progress
import rxhttp.wrapper.parse.*

/**
 * RxHttp基类
 * User: ljx
 * Date: 2020/3/9
 * Time: 08:43
 */
abstract class BaseRxHttp {

    //断点下载进度偏移量，进在带进度断点下载时生效
    abstract val breakDownloadOffSize: Long

    protected abstract fun doOnStart()

    abstract fun newCall(): Call

    abstract fun newCall(client: OkHttpClient): Call

    abstract fun <T> asParser(parser: Parser<T>): Observable<T>

    abstract fun asDownload(
        destPath: String,
        progressConsumer: Consumer<Progress<String>>,
        observeOnScheduler: Scheduler? = null
    ): Observable<String>

    fun asBoolean() = asClass<Boolean>()

    fun asByte() = asClass<Byte>()

    fun asShort() = asClass<Short>()

    fun asInteger() = asClass<Int>()

    fun asLong() = asClass<Long>()

    fun asFloat() = asClass<Float>()

    fun asDouble() = asClass<Double>()

    fun asString() = asClass<String>()

    fun asBitmap() = asParser(BitmapParser())

    /**
     * please user [asClass] instead
     */
    @Deprecated("The method has been renamed", ReplaceWith("asClass(type)"))
    fun <T> asObject(type: Class<T>) = asClass(type)

    fun <T> asClass(type: Class<T>) = asParser(SimpleParser(type))

    fun asMap() = asClass<Map<Any, Any>>()

    fun <T> asMap(type: Class<T>) = asParser(MapParser(type, type))

    fun <K, V> asMap(kType: Class<K>, vType: Class<V>) = asParser(MapParser(kType, vType))

    fun <T> asList(type: Class<T>) = asParser(ListParser(type))

    /**
     * 调用此方法，订阅回调时，返回 [okhttp3.Headers] 对象
     */
    fun asHeaders(): Observable<Headers> = asOkResponse().map { it.headers() }

    /**
     * 调用此方法，订阅回调时，返回 [okhttp3.Response] 对象
     */
    fun asOkResponse() = asParser(OkResponseParser())

    fun asDownload(destPath: String) = asParser(DownloadParser(destPath))

    fun asDownload(
        destPath: String,
        progressConsumer: Consumer<Progress<String>>
    ) = asDownload(destPath, progressConsumer, null)
}