@file:Suppress("BlockingMethodInNonBlockingContext")

package rxhttp.wrapper.parse

import okhttp3.Response
import okhttp3.ResponseBody
import rxhttp.wrapper.OkHttpCompat
import rxhttp.wrapper.callback.OutputStreamFactory
import rxhttp.wrapper.exception.ExceptionHelper
import rxhttp.wrapper.utils.IOUtil
import rxhttp.wrapper.utils.LogUtil
import java.io.IOException
import java.io.OutputStream

/**
 * User: ljx
 * Date: 2020/9/5
 * Time: 14:09
 */
class SuspendStreamParser<T>(
    private val osFactory: OutputStreamFactory<T>,
    private val progress: (suspend (Int, Long, Long) -> Unit)? = null,
) : SuspendParser<T>() {

    @Throws(IOException::class)
    override suspend fun onSuspendParse(response: Response): T {
        val body = ExceptionHelper.throwIfFatal(response)
        val expandOutputStream = osFactory.getOutputStream(response)
        val expand = expandOutputStream.expand
        LogUtil.log(response, expand.toString())
        progress?.let {
            response.writeTo(body, expandOutputStream, it)
        } ?: IOUtil.write(body.byteStream(), expandOutputStream)
        return expand
    }
}

@Throws(IOException::class)
private suspend fun Response.writeTo(
    body: ResponseBody,
    outStream: OutputStream,
    progress: suspend (Int, Long, Long) -> Unit
) {
    val offsetSize = OkHttpCompat.getDownloadOffSize(this)?.offSize ?: 0
    var contentLength = OkHttpCompat.getContentLength(this)
    if (contentLength != -1L) contentLength += offsetSize

    var lastProgress = 0
    var lastSize = 0L
    var lastRefreshTime = 0L

    IOUtil.suspendWrite(body.byteStream(), outStream) {
        val currentSize = it + offsetSize
        lastSize = currentSize
        if (contentLength == -1L) {
            //响应头里取不到contentLength，仅回调已下载字节数
            val curTime = System.currentTimeMillis()
            if (curTime - lastRefreshTime > 500) {
                progress(0, currentSize, contentLength)
                lastRefreshTime = curTime
            }
        } else {
            //当前进度 = 当前已读取的字节 * 100 / 总字节
            val currentProgress = ((currentSize * 100 / contentLength)).toInt()
            if (currentProgress > lastProgress) {
                lastProgress = currentProgress
                progress(currentProgress, currentSize, contentLength)
            }
        }
    }

    if (contentLength == -1L) {
        //响应头里取不到contentLength时，保证下载完成事件能回调
        progress(100, lastSize, contentLength)
    }
}