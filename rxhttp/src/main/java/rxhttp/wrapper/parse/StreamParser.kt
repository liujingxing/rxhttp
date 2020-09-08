package rxhttp.wrapper.parse

import okhttp3.Response
import okhttp3.ResponseBody
import rxhttp.wrapper.OkHttpCompat
import rxhttp.wrapper.callback.FileOutputStreamFactory
import rxhttp.wrapper.callback.OutputStreamFactory
import rxhttp.wrapper.callback.ProgressCallback
import rxhttp.wrapper.callback.UriOutputStreamFactory
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
class StreamParser @JvmOverloads constructor(
    private val osFactory: OutputStreamFactory,
    var callback: ProgressCallback? = null
) : Parser<String> {

    override fun onParse(response: Response): String {
        val body = ExceptionHelper.throwIfFatal(response)
        val os = osFactory.getOutputStream(response)
        val msg = when (osFactory) {
            is FileOutputStreamFactory -> osFactory.localPath
            is UriOutputStreamFactory -> osFactory.uri.toString()
            else -> ""
        }
        LogUtil.log(response, msg)
        response.writeTo(body, os, callback)
        return msg
    }
}


@Throws(IOException::class)
fun Response.writeTo(
    body: ResponseBody,
    os: OutputStream,
    callback: ProgressCallback? = null
) {
    val offsetSize = OkHttpCompat.getDownloadOffSize(this)?.offSize ?: 0
    val contentLength = OkHttpCompat.getContentLength(this) + offsetSize

    var lastProgress = 0

    IOUtil.write(body.byteStream(), os) {
        if (callback == null) return@write
        val currentSize = it + offsetSize
        //当前进度 = 当前已读取的字节 / 总字节
        val currentProgress = ((currentSize * 100f / contentLength)).toInt()
        if (currentProgress > lastProgress) {
            lastProgress = currentProgress
            val progress = Progress(currentProgress, currentSize, contentLength)
            callback.onProgress(progress)
        }
    }
}