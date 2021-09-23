package rxhttp

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import okhttp3.Headers
import okhttp3.Response
import rxhttp.wrapper.CallFactory
import rxhttp.wrapper.OkHttpCompat
import rxhttp.wrapper.callback.OutputStreamFactory
import rxhttp.wrapper.coroutines.Await
import rxhttp.wrapper.coroutines.AwaitImpl
import rxhttp.wrapper.entity.Progress
import rxhttp.wrapper.entity.ProgressT
import rxhttp.wrapper.parse.*
import rxhttp.wrapper.utils.LogUtil

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
    progressCallback: (suspend (ProgressT<T>) -> Unit)? = null
): Await<T> {
    val parser = if (progressCallback != null) {
        SuspendStreamParser(osFactory) { progress, currentSize, totalSize ->
            LogUtil.logDownProgress(progress, currentSize, totalSize)
            val p = ProgressT<T>(progress, currentSize, totalSize)
            progressCallback(p)
        }
    } else {
        SuspendStreamParser(osFactory)
    }
    return toParser(parser)
}

/**
 * @param destPath Local storage path
 * @param append is append download
 * @param progress Progress callback in suspend method, The callback thread depends on the coroutine thread
 */
fun CallFactory.toDownload(
    destPath: String,
    append: Boolean = false,
    progress: (suspend (Progress) -> Unit)? = null
): Await<String> = toFlow(destPath, append, progress).toAwait()

fun CallFactory.toDownload(
    context: Context,
    uri: Uri,
    append: Boolean = false,
    progress: (suspend (Progress) -> Unit)? = null
): Await<Uri> = toFlow(context, uri, append, progress).toAwait()

fun <T> CallFactory.toDownload(
    osFactory: OutputStreamFactory<T>,
    append: Boolean = false,
    progress: (suspend (Progress) -> Unit)? = null
): Await<T> = toFlow(osFactory, append, progress).toAwait()

private fun <T> Flow<T>.toAwait(): Await<T> =
    object : Await<T> {
        override suspend fun await(): T {
            var t: T? = null
            collect { t = it }
            return t!!
        }
    }
