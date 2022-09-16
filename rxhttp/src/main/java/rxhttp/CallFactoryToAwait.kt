package rxhttp

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import kotlinx.coroutines.flow.Flow
import okhttp3.Headers
import okhttp3.Response
import rxhttp.wrapper.CallFactory
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
fun <T> CallFactory.toParser(parser: Parser<T>): Await<T> = AwaitImpl(this, parser)

inline fun <reified T> CallFactory.toClass(): Await<T> = toParser(SmartParser.wrap(javaTypeOf<T>()))

fun CallFactory.toStr(): Await<String> = toClass()

inline fun <reified T> CallFactory.toList(): Await<MutableList<T>> = toClass()

inline fun <reified V> CallFactory.toMapString(): Await<Map<String, V>> = toClass()

fun CallFactory.toBitmap(): Await<Bitmap> = toClass()

fun CallFactory.toOkResponse(): Await<Response> = toClass()

fun CallFactory.toHeaders(): Await<Headers> = toClass()

/**
 * @param destPath Local storage path
 * @param append is append download
 * @param capacity capacity of the buffer between coroutines
 * @param progress Progress callback in suspend method, The callback thread depends on the coroutine thread
 */
fun CallFactory.toDownload(
    destPath: String,
    append: Boolean = false,
    capacity: Int = 1,
    progress: (suspend (Progress) -> Unit)? = null
): Await<String> = toFlow(destPath, append, capacity, progress).toAwait()

fun CallFactory.toDownload(
    context: Context,
    uri: Uri,
    append: Boolean = false,
    capacity: Int = 1,
    progress: (suspend (Progress) -> Unit)? = null
): Await<Uri> = toFlow(context, uri, append, capacity, progress).toAwait()

fun <T> CallFactory.toDownload(
    osFactory: OutputStreamFactory<T>,
    append: Boolean = false,
    capacity: Int = 1,
    progress: (suspend (Progress) -> Unit)? = null
): Await<T> = toFlow(osFactory, append, capacity, progress).toAwait()

private fun <T> Flow<T>.toAwait(): Await<T> =
    object : Await<T> {
        override suspend fun await(): T {
            var t: T? = null
            collect { t = it }
            return t!!
        }
    }

internal fun <T> CallFactory.toSyncDownload(
    osFactory: OutputStreamFactory<T>,
    progressCallback: ((ProgressT<T>) -> Unit)? = null
): Await<T> {
    val parser = StreamParser(osFactory)
    if (progressCallback != null) {
        parser.progressCallback = ProgressCallback { progress, currentSize, totalSize ->
            //LogUtil.logDownProgress(progress, currentSize, totalSize)
            val p = ProgressT<T>(progress, currentSize, totalSize)
            progressCallback(p)
        }
    }
    return toParser(parser)
}

