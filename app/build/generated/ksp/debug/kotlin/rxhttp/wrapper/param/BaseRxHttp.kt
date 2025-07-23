package rxhttp.wrapper.`param`

import android.content.Context
import android.net.Uri
import com.example.httpsender.entity.PageList
import com.example.httpsender.parser.ResponseParser
import io.reactivex.rxjava3.plugins.RxJavaPlugins
import java.io.IOException
import java.lang.Class
import java.lang.reflect.Type
import kotlin.Boolean
import kotlin.String
import kotlin.Suppress
import kotlin.collections.List
import kotlin.jvm.JvmOverloads
import kotlin.jvm.Throws
import okhttp3.Response
import rxhttp.wrapper.CallFactory
import rxhttp.wrapper.ITag
import rxhttp.wrapper.callback.FileOutputStreamFactory
import rxhttp.wrapper.callback.OutputStreamFactory
import rxhttp.wrapper.callback.UriOutputStreamFactory
import rxhttp.wrapper.parse.OkResponseParser
import rxhttp.wrapper.parse.Parser
import rxhttp.wrapper.parse.SmartParser
import rxhttp.wrapper.parse.StreamParser
import rxhttp.wrapper.utils.LogUtil
import rxhttp.wrapper.utils.TypeUtil
import rxhttp.wrapper.utils.parameterizedBy

/**
 * User: ljx
 * Date: 2020/4/11
 * Time: 18:15
 */
public abstract class BaseRxHttp : ITag, CallFactory {
    public fun <T> toObservable(parser: Parser<T>): ObservableCall<T> = ObservableCall(this, parser)

    public fun <T> toObservable(type: Type): ObservableCall<T> = toObservable(SmartParser.wrap<T>(type))

    public fun <T> toObservable(clazz: Class<T>): ObservableCall<T> = toObservable(clazz as Type)

    public fun toObservableString(): ObservableCall<String> = toObservable(String::class.java)

    public fun <T> toObservableList(clazz: Class<T>): ObservableCall<List<T>> {
        val typeList = List::class.parameterizedBy(clazz)
        return toObservable(typeList)
    }

    @JvmOverloads
    public fun toDownloadObservable(destPath: String, append: Boolean = false): ObservableCall<String> = toDownloadObservable(FileOutputStreamFactory(destPath), append)

    @JvmOverloads
    public fun toDownloadObservable(
        context: Context,
        uri: Uri,
        append: Boolean = false,
    ): ObservableCall<Uri> = toDownloadObservable(UriOutputStreamFactory(context, uri), append)

    @JvmOverloads
    public fun <T> toDownloadObservable(osFactory: OutputStreamFactory<T>, append: Boolean = false): ObservableCall<T> {
        if (append) {
            tag(OutputStreamFactory::class.java, osFactory)
        }
        return toObservable(StreamParser(osFactory))
    }

    public fun <T> toObservableResponse(type: Type): ObservableCall<T> = toObservable(wrapResponseParser(type))

    public fun <T> toObservableResponse(type: Class<T>): ObservableCall<T> = toObservableResponse<T>(type as Type)

    public fun <T> toObservableResponseList(type: Class<T>): ObservableCall<List<T>> {
        val typeList = List::class.parameterizedBy(type)
        return toObservableResponse(typeList)
    }

    public fun <T> toObservableResponsePageList(type: Class<T>): ObservableCall<PageList<T>> {
        val typePageList = PageList::class.parameterizedBy(type)
        return toObservableResponse(typePageList)
    }

    @Throws(IOException::class)
    public fun execute(): Response = newCall().execute()

    @Throws(IOException::class)
    public fun <T> execute(parser: Parser<T>): T = parser.onParse(execute())

    @Throws(IOException::class)
    public fun <T> executeClass(type: Type): T = execute(SmartParser.wrap(type))

    @Throws(IOException::class)
    public fun <T> executeClass(clazz: Class<T>): T = executeClass(clazz as Type)

    @Throws(IOException::class)
    public fun executeString(): String = executeClass(String::class.java)

    @Throws(IOException::class)
    public fun <T> executeList(clazz: Class<T>): List<T> {
        val typeList = List::class.parameterizedBy(clazz)
        return executeClass(typeList)
    }

    public companion object {
        init {
            val errorHandler = RxJavaPlugins.getErrorHandler()
            if (errorHandler == null) {
                /*                                                                     
                 RxJava的一个重要的设计理念是：不吃掉任何一个异常, 即抛出的异常无人处理，便会导致程序崩溃                      
                 这就会导致一个问题，当RxJava“downStream”取消订阅后，“upStream”仍有可能抛出异常，                
                 这时由于已经取消订阅，“downStream”无法处理异常，此时的异常无人处理，便会导致程序崩溃                       
                */
                RxJavaPlugins.setErrorHandler { LogUtil.logRxJavaError(it) }
            }
        }

        @Suppress("UNCHECKED_CAST")
        public fun <T> wrapResponseParser(type: Type): Parser<T> {
            val actualType = TypeUtil.getActualType(type) ?: type
            val parser = ResponseParser<Any>(actualType)
            val actualParser = if (actualType == type) parser else OkResponseParser(parser)
            return actualParser as Parser<T>
        }
    }
}
