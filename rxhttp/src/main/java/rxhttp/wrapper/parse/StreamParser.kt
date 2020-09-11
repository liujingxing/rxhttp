package rxhttp.wrapper.parse

import android.content.Context
import android.net.Uri
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
class StreamParser<T> @JvmOverloads constructor(
    private val osFactory: OutputStreamFactory<T>,
    var progressCallback: ProgressCallback? = null
) : Parser<T> {

    companion object {

        @JvmStatic
        operator fun get(
            destPath: String,
        ): StreamParser<String> = StreamParser(FileOutputStreamFactory(destPath))

        @JvmStatic
        operator fun get(
            context: Context,
            uri: Uri,
        ): StreamParser<Uri> = StreamParser(UriOutputStreamFactory(context, uri))
    }

    override fun onParse(response: Response): T {
        val body = ExceptionHelper.throwIfFatal(response)
        val os = osFactory.getOutputStream(response)
        val data = osFactory.data
        LogUtil.log(response, data.toString())
        progressCallback?.let {
            response.writeTo(body, os, it)
        } ?: IOUtil.write(body.byteStream(), os)
        return data
    }
}

@Throws(IOException::class)
private fun Response.writeTo(
    body: ResponseBody,
    os: OutputStream,
    callback: ProgressCallback
) {
    val offsetSize = OkHttpCompat.getDownloadOffSize(this)?.offSize ?: 0
    val contentLength = OkHttpCompat.getContentLength(this) + offsetSize

    var lastProgress = 0

    IOUtil.write(body.byteStream(), os) {
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