package rxhttp

import android.content.Context
import android.net.Uri
import rxhttp.wrapper.CallFactory
import rxhttp.wrapper.ITag
import rxhttp.wrapper.callback.FileOutputStreamFactory
import rxhttp.wrapper.callback.OutputStreamFactory
import rxhttp.wrapper.callback.UriOutputStreamFactory
import rxhttp.wrapper.coroutines.Await
import rxhttp.wrapper.coroutines.CallAwait
import rxhttp.wrapper.coroutines.CallFlow
import rxhttp.wrapper.parse.Parser
import rxhttp.wrapper.parse.SmartParser
import rxhttp.wrapper.parse.StreamParser
import rxhttp.wrapper.utils.javaTypeOf

/**
 * User: ljx
 * Date: 2021/9/18
 * Time: 17:34
 */
fun <T> CallFactory.toAwait(parser: Parser<T>): CallAwait<T> = CallAwait(this, parser)

inline fun <reified T> CallFactory.toAwait(): CallAwait<T> = toAwait(SmartParser.wrap(javaTypeOf<T>()))

fun CallFactory.toAwaitString(): CallAwait<String> = toAwait()

inline fun <reified T> CallFactory.toAwaitList(): CallAwait<MutableList<T>> = toAwait()

fun CallFactory.toDownloadAwait(
    destPath: String,
    append: Boolean = false,
): Await<String> = toDownloadAwait(FileOutputStreamFactory(destPath), append)

fun CallFactory.toDownloadAwait(
    context: Context,
    uri: Uri,
    append: Boolean = false,
): Await<Uri> = toDownloadAwait(UriOutputStreamFactory(context, uri), append)

fun <T> CallFactory.toDownloadAwait(
    osFactory: OutputStreamFactory<T>,
    append: Boolean = false,
): Await<T> {
    if (append && this is ITag) {
        tag(OutputStreamFactory::class.java, osFactory)
    }
    return toAwait(StreamParser(osFactory))
}


fun <T> CallFactory.toFlow(parser: Parser<T>): CallFlow<T> = CallFlow(this, parser)

inline fun <reified T> CallFactory.toFlow(): CallFlow<T> = toFlow(SmartParser.wrap(javaTypeOf<T>()))

fun CallFactory.toFlowString(): CallFlow<String> = toFlow()

inline fun <reified T> CallFactory.toFlowList(): CallFlow<List<T>> = toFlow()

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