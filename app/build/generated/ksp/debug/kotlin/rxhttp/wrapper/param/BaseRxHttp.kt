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
import rxhttp.wrapper.callback.FileOutputStreamFactory
import rxhttp.wrapper.callback.OutputStreamFactory
import rxhttp.wrapper.callback.UriOutputStreamFactory
import rxhttp.wrapper.coroutines.RangeHeader
import rxhttp.wrapper.entity.ParameterizedTypeImpl
import rxhttp.wrapper.entity.Progress
import rxhttp.wrapper.parse.Parser
import rxhttp.wrapper.parse.SmartParser
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

    fun <T> asParser(parser: Parser<T>) = ObservableCall(this, parser)

    fun <T> asClass(type: Type) = asParser(SmartParser.wrap<T>(type))

    fun <T> asClass(clazz: Class<T>) = asClass<T>(clazz as Type)

    fun asString() = asClass(String::class.java)

    fun <V> asMapString(vType: Class<V>) =
        asClass<Map<String, V>>(ParameterizedTypeImpl.getParameterized(MutableMap::class.java,String::class.java,vType))

    fun <T> asList(tType: Class<T>) =
        asClass<List<T>>(ParameterizedTypeImpl[MutableList::class.java, tType])
    
    fun asBitmap() = asClass(Bitmap::class.java)
    
    fun asOkResponse() = asClass(Response::class.java)

    fun asHeaders() = asClass(Headers::class.java)

    @JvmOverloads
    fun asDownload(
        destPath: String,
        append: Boolean = false,
    ): ObservableCall<String> =
        asDownload(FileOutputStreamFactory(destPath), append)
    
    @JvmOverloads
    fun asDownload(
        context: Context,
        uri: Uri,
        append: Boolean = false,
    ): ObservableCall<Uri> =
        asDownload(UriOutputStreamFactory(context, uri), append)
    
    @JvmOverloads
    fun <T> asDownload(
        osFactory: OutputStreamFactory<T>,
        append: Boolean = false,
    ): ObservableCall<T> {
       val observableCall =  asParser(StreamParser(osFactory))
       return if (append) {
           observableCall.onSubscribe {
               // In IO Thread
               val offsetSize = osFactory.offsetSize()
               if (offsetSize >= 0) setRangeHeader(offsetSize, -1, true)
           }
       } else {
           observableCall
       }
    }
}    
