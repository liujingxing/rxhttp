package rxhttp.wrapper.parse

import okhttp3.Response
import okhttp3.ResponseBody
import rxhttp.wrapper.OkHttpCompat
import rxhttp.wrapper.callback.ProgressCallback
import rxhttp.wrapper.utils.IOUtil
import java.io.IOException
import java.io.OutputStream

/**
 * User: ljx
 * Date: 2020/9/4
 * Time: 14:34
 */
abstract class IOParser : Parser<String> {

    var callback: ProgressCallback? = null

}

@Throws(IOException::class)
fun Response.writeTo(
    body: ResponseBody,
    os: OutputStream,
    callback: ProgressCallback? = null
) {
    val contentLength = OkHttpCompat.getContentLength(this)
    val offsetSize = OkHttpCompat.getDownloadOffSize(this)?.offSize ?: 0
    val newContentLength = contentLength + offsetSize

    var lastProgress = 0

    IOUtil.write(body.byteStream(), os) {
        if (callback == null) return@write
        val currentSize = it + offsetSize
        //当前进度 = 当前已读取的字节 / 总字节
        val currentProgress = ((currentSize * 100f / newContentLength)).toInt()
        if (currentProgress > lastProgress) {
            lastProgress = currentProgress
            callback.onProgress(currentProgress, currentSize, newContentLength)
        }
    }
}