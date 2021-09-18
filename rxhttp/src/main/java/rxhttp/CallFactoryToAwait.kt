package rxhttp

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Headers
import okhttp3.Response
import rxhttp.wrapper.coroutines.AwaitImpl
import rxhttp.wrapper.OkHttpCompat
import rxhttp.wrapper.callback.FileOutputStreamFactory
import rxhttp.wrapper.callback.OutputStreamFactory
import rxhttp.wrapper.callback.UriFactory
import rxhttp.wrapper.callback.UriOutputStreamFactory
import rxhttp.wrapper.coroutines.Await
import rxhttp.wrapper.entity.Progress
import rxhttp.wrapper.entity.ProgressT
import rxhttp.wrapper.parse.*
import rxhttp.wrapper.utils.LogUtil
import kotlin.coroutines.CoroutineContext

/**
 * User: ljx
 * Date: 2021/9/18
 * Time: 17:34
 */

fun <T> CallFactory.toParser(
    parser: Parser<T>,
): Await<T> = AwaitImpl(this, parser)

inline fun <reified T : Any> CallFactory.toClass(): Await<T> =
    toParser(object : SimpleParser<T>() {})

fun CallFactory.toStr(): Await<String> = toClass()

inline fun <reified T : Any> CallFactory.toList(): Await<MutableList<T>> = toClass()

inline fun <reified K : Any, reified V : Any> CallFactory.toMap(): Await<Map<K, V>> = toClass()

fun CallFactory.toBitmap(): Await<Bitmap> = toParser(BitmapParser())

fun CallFactory.toOkResponse(): Await<Response> = toParser(OkResponseParser())

fun CallFactory.toHeaders(): Await<Headers> = toOkResponse()
    .map {
        try {
            OkHttpCompat.headers(it)
        } finally {
            OkHttpCompat.closeQuietly(it)
        }
    }

fun <T> CallFactory.toSyncDownload(
    osFactory: OutputStreamFactory<T>,
    context: CoroutineContext? = null,
    progressCallback: (suspend (ProgressT<T>) -> Unit)? = null
): Await<T> {
    val parser = if (progressCallback != null) {
        SuspendStreamParser(osFactory) { progress, currentSize, totalSize ->
            LogUtil.logDownProgress(progress, currentSize, totalSize)
            val p = ProgressT<T>(progress, currentSize, totalSize)
            context?.let { withContext(it) { progressCallback(p) } } ?: progressCallback(p)
        }
    } else {
        SuspendStreamParser(osFactory)
    }
    return toParser(parser)
}

@Deprecated(
    level = DeprecationLevel.WARNING,
    message = "Use 'toDownload' instead",
    replaceWith = ReplaceWith("toDownload(destPath, context, true) {}")
)
fun CallFactory.toAppendDownload(
    destPath: String,
    context: CoroutineContext? = null,
    progress: (suspend (Progress) -> Unit)? = null
) = toDownload(destPath, context, true, progress)

@Deprecated(
    level = DeprecationLevel.WARNING,
    message = "Use 'toDownload' instead",
    replaceWith = ReplaceWith("toDownload(context, uri, coroutineContext, true)")
)
fun CallFactory.toAppendDownload(
    context: Context,
    uri: Uri,
    coroutineContext: CoroutineContext? = null,
    progress: (suspend (Progress) -> Unit)? = null
) = toDownload(context, uri, coroutineContext, true, progress)

@Deprecated(
    level = DeprecationLevel.WARNING,
    message = "Use 'toDownload' instead",
    replaceWith = ReplaceWith("toDownload(uriFactory, coroutineContext, true)")
)
fun CallFactory.toAppendDownload(
    uriFactory: UriFactory,
    coroutineContext: CoroutineContext? = null,
    progress: (suspend (Progress) -> Unit)? = null
) = toDownload(uriFactory, coroutineContext, true, progress)

/**
 * @param destPath Local storage path
 * @param context Use to control the thread on which the progress callback
 * @param append is append download
 * @param progress Progress callback in suspend method, The callback thread depends on the coroutine thread
 */
fun CallFactory.toDownload(
    destPath: String,
    context: CoroutineContext? = null,
    append: Boolean = false,
    progress: (suspend (Progress) -> Unit)? = null
): Await<String> = toDownload(FileOutputStreamFactory(destPath), context, append, progress)

fun CallFactory.toDownload(
    context: Context,
    uri: Uri,
    coroutineContext: CoroutineContext? = null,
    append: Boolean = false,
    progress: (suspend (Progress) -> Unit)? = null
): Await<Uri> =
    toDownload(UriOutputStreamFactory(context, uri), coroutineContext, append, progress)

fun <T> CallFactory.toDownload(
    osFactory: OutputStreamFactory<T>,
    context: CoroutineContext? = null,
    append: Boolean = false,
    progress: (suspend (Progress) -> Unit)? = null
): Await<T> =
    toSyncDownload(osFactory, context, progress)
        .onStart { setRangeHeader(osFactory, append) }
        .flowOn(Dispatchers.IO)