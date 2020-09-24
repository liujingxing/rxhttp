@file:Suppress("BlockingMethodInNonBlockingContext")

package rxhttp.wrapper.parse

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.withContext
import okhttp3.Response
import okhttp3.ResponseBody
import rxhttp.wrapper.OkHttpCompat
import rxhttp.wrapper.callback.OutputStreamFactory
import rxhttp.wrapper.callback.newOutputStreamFactory
import rxhttp.wrapper.entity.OutputStreamWrapper
import rxhttp.wrapper.entity.ProgressT
import rxhttp.wrapper.exception.ExceptionHelper
import rxhttp.wrapper.utils.IOUtil
import rxhttp.wrapper.utils.LogUtil
import java.io.IOException
import kotlin.coroutines.CoroutineContext

/**
 * User: ljx
 * Date: 2020/9/5
 * Time: 14:09
 */
class SuspendStreamParser<T>(
    private val osFactory: OutputStreamFactory<T>,
    private val context: CoroutineContext? = null,
    private val progress: (suspend (ProgressT<T>) -> Unit)? = null,
) : SuspendParser<T>() {

    companion object {

        operator fun get(
            destPath: String,
            coroutineContext: CoroutineContext? = null,
            progress: (suspend (ProgressT<String>) -> Unit)? = null,
        ): SuspendStreamParser<String> = SuspendStreamParser(newOutputStreamFactory(destPath), coroutineContext, progress)

        operator fun get(
            context: Context,
            uri: Uri,
            coroutineContext: CoroutineContext? = null,
            progress: (suspend (ProgressT<Uri>) -> Unit)? = null,
        ): SuspendStreamParser<Uri> = SuspendStreamParser(newOutputStreamFactory(context, uri), coroutineContext, progress)
    }

    @Throws(IOException::class)
    override suspend fun onSuspendParse(response: Response): T {
        val body = ExceptionHelper.throwIfFatal(response)
        val osWrapper = osFactory.getOutputStream(response)
        val result = osWrapper.result
        LogUtil.log(response, result.toString())
        progress?.let {
            response.writeTo(body, osWrapper, context, it)
        } ?: IOUtil.write(body.byteStream(), osWrapper.os)
        return result
    }
}

@Throws(IOException::class)
private suspend fun <T> Response.writeTo(
    body: ResponseBody,
    osWrapper: OutputStreamWrapper<T>,
    context: CoroutineContext? = null,
    progress: suspend (ProgressT<T>) -> Unit
) {
    val offsetSize = OkHttpCompat.getDownloadOffSize(this)?.offSize ?: 0
    val contentLength = OkHttpCompat.getContentLength(this) + offsetSize

    var lastProgress = 0

    IOUtil.suspendWrite(body.byteStream(), osWrapper.os) {
        val currentSize = it + offsetSize
        //当前进度 = 当前已读取的字节 / 总字节
        val currentProgress = ((currentSize * 100 / contentLength)).toInt()
        if (currentProgress > lastProgress) {
            lastProgress = currentProgress
            val p = ProgressT<T>(currentProgress, currentSize, contentLength)
            if (currentProgress == 100) p.result = osWrapper.result
            context?.apply {
                withContext(this) { progress(p) }
            } ?: progress(p)
        }
    }
}