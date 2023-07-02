package rxhttp

import android.content.Context
import android.net.Uri
import rxhttp.wrapper.CallFactory
import rxhttp.wrapper.ITag
import rxhttp.wrapper.callback.FileOutputStreamFactory
import rxhttp.wrapper.callback.OutputStreamFactory
import rxhttp.wrapper.callback.UriOutputStreamFactory
import rxhttp.wrapper.coroutines.Await
import rxhttp.wrapper.coroutines.AwaitImpl
import rxhttp.wrapper.parse.Parser
import rxhttp.wrapper.parse.SmartParser
import rxhttp.wrapper.parse.StreamParser
import rxhttp.wrapper.utils.javaTypeOf

/**
 * User: ljx
 * Date: 2021/9/18
 * Time: 17:34
 */
fun <T> CallFactory.toAwait(parser: Parser<T>): Await<T> = AwaitImpl(this, parser)

inline fun <reified T> CallFactory.toAwait(): Await<T> = toAwait(SmartParser.wrap(javaTypeOf<T>()))

fun CallFactory.toAwaitString(): Await<String> = toAwait()

inline fun <reified T> CallFactory.toAwaitList(): Await<MutableList<T>> = toAwait()

inline fun <reified V> CallFactory.toAwaitMapString(): Await<Map<String, V>> = toAwait()



fun <T> CallFactory.toFlow(parser: Parser<T>): CallFlow<T> = CallFlow(this, parser)

inline fun <reified T> CallFactory.toFlow(): CallFlow<T> = toFlow(SmartParser.wrap(javaTypeOf<T>()))

fun CallFactory.toFlowString(): CallFlow<String> = toFlow()

inline fun <reified T> CallFactory.toFlowList(): CallFlow<List<T>> = toFlow()

inline fun <reified V> CallFactory.toFlowMapString(): CallFlow<Map<String, V>> = toFlow()

/**
 * @param destPath Local storage path
 * @param append is append download
 */
fun CallFactory.toDownloadFlow(
    destPath: String,
    append: Boolean = false,
): CallFlow<String> = toDownloadFlow(FileOutputStreamFactory(destPath), append)

fun CallFactory.toDownloadFlow(
    context: Context,
    uri: Uri,
    append: Boolean = false,
): CallFlow<Uri> = toDownloadFlow(UriOutputStreamFactory(context, uri), append)

fun <T> CallFactory.toDownloadFlow(
    osFactory: OutputStreamFactory<T>,
    append: Boolean = false,
): CallFlow<T> {
    if (append && this is ITag) {
        tag(OutputStreamFactory::class.java, osFactory)
    }
    return toFlow(StreamParser(osFactory))
}