@file:Suppress("BlockingMethodInNonBlockingContext")

package rxhttp.wrapper.parse

import kotlinx.coroutines.withContext
import okhttp3.Response
import okhttp3.ResponseBody
import rxhttp.wrapper.OkHttpCompat
import rxhttp.wrapper.callback.FileOutputStreamFactory
import rxhttp.wrapper.callback.OutputStreamFactory
import rxhttp.wrapper.callback.UriOutputStreamFactory
import rxhttp.wrapper.entity.Progress
import rxhttp.wrapper.exception.ExceptionHelper
import rxhttp.wrapper.utils.IOUtil
import rxhttp.wrapper.utils.LogUtil
import java.io.IOException
import java.io.OutputStream
import kotlin.coroutines.CoroutineContext

/**
 * User: ljx
 * Date: 2020/9/5
 * Time: 14:09
 */
class SuspendStreamParser(
    private val osFactory: OutputStreamFactory,
    private val context: CoroutineContext? = null,
    private val progress: suspend (Progress) -> Unit,
) {

    @Throws(IOException::class)
    suspend fun onParse(response: Response): String {
        val body = ExceptionHelper.throwIfFatal(response)
        val os = osFactory.getOutputStream(response)
        val msg = when (osFactory) {
            is FileOutputStreamFactory -> osFactory.localPath
            is UriOutputStreamFactory -> osFactory.uri.toString()
            else -> ""
        }
        LogUtil.log(response, msg)
        response.writeTo(body, os, context, progress)
        return msg
    }
}

@Throws(IOException::class)
suspend fun Response.writeTo(
    body: ResponseBody,
    os: OutputStream,
    context: CoroutineContext? = null,
    progress: suspend (Progress) -> Unit
) {
    val offsetSize = OkHttpCompat.getDownloadOffSize(this)?.offSize ?: 0
    val contentLength = OkHttpCompat.getContentLength(this) + offsetSize

    var lastProgress = 0

    IOUtil.suspendWrite(body.byteStream(), os) {
        val currentSize = it + offsetSize
        //当前进度 = 当前已读取的字节 / 总字节
        val currentProgress = ((currentSize * 100f / contentLength)).toInt()
        if (currentProgress > lastProgress) {
            lastProgress = currentProgress
            val p = Progress(currentProgress, currentSize, contentLength)
            if (context != null) {
                withContext(context) { progress.invoke(p) }
            } else {
                progress.invoke(p)
            }
        }
    }
}