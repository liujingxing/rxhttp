package rxhttp

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.flow.Flow
import rxhttp.wrapper.CallFactory
import rxhttp.wrapper.ITag
import rxhttp.wrapper.callback.OutputStreamFactory
import rxhttp.wrapper.callback.ProgressCallback
import rxhttp.wrapper.coroutines.Await
import rxhttp.wrapper.coroutines.AwaitImpl
import rxhttp.wrapper.entity.Progress
import rxhttp.wrapper.entity.ProgressT
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

/**
 * @param destPath Local storage path
 * @param append is append download
 * @param capacity capacity of the buffer between coroutines
 * @param progress Progress callback in suspend method, The callback thread depends on the coroutine thread
 */
fun CallFactory.toDownloadAwait(
    destPath: String,
    append: Boolean = false,
    capacity: Int = 1,
    progress: (suspend (Progress) -> Unit)? = null
): Await<String> = toDownloadFlow(destPath, append, capacity, progress).toAwait()

fun CallFactory.toDownloadAwait(
    context: Context,
    uri: Uri,
    append: Boolean = false,
    capacity: Int = 1,
    progress: (suspend (Progress) -> Unit)? = null
): Await<Uri> = toDownloadFlow(context, uri, append, capacity, progress).toAwait()

fun <T> CallFactory.toDownloadAwait(
    osFactory: OutputStreamFactory<T>,
    append: Boolean = false,
    capacity: Int = 1,
    progress: (suspend (Progress) -> Unit)? = null
): Await<T> = toDownloadFlow(osFactory, append, capacity, progress).toAwait()

private fun <T> Flow<T>.toAwait(): Await<T> = newAwait {
    var t: T? = null
    collect { t = it }
    t!!
}

internal fun <T> CallFactory.toSyncDownloadAwait(
    osFactory: OutputStreamFactory<T>,
    append: Boolean = false,
    progressCallback: ((ProgressT<T>) -> Unit)? = null
): Await<T> {
    if (append && this is ITag) {
        tag(OutputStreamFactory::class.java, osFactory)
    }
    val parser = StreamParser(osFactory)
    if (progressCallback != null) {
        parser.progressCallback = ProgressCallback { progress, currentSize, totalSize ->
            val p = ProgressT<T>(progress, currentSize, totalSize)
            progressCallback(p)
        }
    }
    return toAwait(parser)
}

