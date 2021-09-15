package rxhttp.wrapper.parse

import okhttp3.Response
import okhttp3.ResponseBody
import rxhttp.wrapper.OkHttpCompat
import rxhttp.wrapper.callback.OutputStreamFactory
import rxhttp.wrapper.callback.ProgressCallback
import rxhttp.wrapper.entity.Progress
import rxhttp.wrapper.exception.ExceptionHelper
import rxhttp.wrapper.utils.IOUtil
import rxhttp.wrapper.utils.LogUtil
import java.io.IOException
import java.io.OutputStream

/**
 * User: ljx
 * Date: 2020/9/4
 * Time: 21:39
 */
class StreamParser<T> @JvmOverloads constructor(
    private val osFactory: OutputStreamFactory<T>,
    var progressCallback: ProgressCallback? = null,
) : Parser<T> {

    override fun onParse(response: Response): T {
        val body = ExceptionHelper.throwIfFatal(response)
        val expandOutputStream = osFactory.getOutputStream(response)
        val expand = expandOutputStream.expand
        LogUtil.log(response, expand.toString())
        progressCallback?.let {
            response.writeTo(body, expandOutputStream, it)
        } ?: IOUtil.write(body.byteStream(), expandOutputStream)
        return expand
    }
}

@Throws(IOException::class)
private fun Response.writeTo(
    body: ResponseBody,
    os: OutputStream,
    callback: ProgressCallback,
) {
    val offsetSize = OkHttpCompat.getDownloadOffSize(this)?.offSize ?: 0
    var contentLength = OkHttpCompat.getContentLength(this)
    if (contentLength != -1L) contentLength += offsetSize

    var lastProgress = 0
    var lastSize = 0L
    var lastRefreshTime = 0L

    IOUtil.write(body.byteStream(), os) {
        val currentSize = it + offsetSize
        lastSize = currentSize
        if (contentLength == -1L) {
            //响应头里取不到contentLength，仅回调已下载字节数
            val curTime = System.currentTimeMillis()
            if (curTime - lastRefreshTime > 500) {
                val progress = Progress(0, currentSize, contentLength)
                LogUtil.log(progress)
                callback.onProgress(progress)
                lastRefreshTime = curTime
            }
        } else {
            //当前进度 = 当前已读取的字节 * 100 / 总字节
            val currentProgress = ((currentSize * 100 / contentLength)).toInt()
            if (currentProgress > lastProgress) {
                lastProgress = currentProgress
                val progress = Progress(currentProgress, currentSize, contentLength)
                LogUtil.log(progress)
                callback.onProgress(progress)
            }
        }
    }
    if (contentLength == -1L) {
        //响应头里取不到contentLength时，保证下载完成事件能回调
        val progress = Progress(100, lastSize, contentLength)
        LogUtil.log(progress)
        callback.onProgress(progress)
    }
}