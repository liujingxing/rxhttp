package rxhttp.wrapper.parse

import okhttp3.Response
import rxhttp.wrapper.OkHttpCompat
import rxhttp.wrapper.callback.OutputStreamFactory
import rxhttp.wrapper.callback.ProgressCallback
import rxhttp.wrapper.entity.DownloadOffSize
import rxhttp.wrapper.utils.LogUtil
import rxhttp.wrapper.utils.isPartialContent
import rxhttp.wrapper.utils.writeTo
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

/**
 * User: ljx
 * Date: 2020/9/4
 * Time: 21:39
 */
open class StreamParser<T> constructor(
    private val osFactory: OutputStreamFactory<T>,
) : Parser<T> {

    var progressCallback: ProgressCallback? = null

    override fun onParse(response: Response): T {
        val body = OkHttpCompat.throwIfFail(response)
        val expandOs = osFactory.openOutputStream(response)
        val expand: T = expandOs.expand
        expandOs.use { os ->
            LogUtil.log(response, expand.toString())
            body.byteStream().use { it.writeTo(os, response, progressCallback) }
        }
        return expand
    }

    @Throws(IOException::class)
    private fun InputStream.writeTo(
        os: OutputStream,
        response: Response,
        callback: ProgressCallback?,
    ) {
        if (callback == null) {
            writeTo(os)
            return
        }

        val offsetSize = if (response.isPartialContent()) {
            OkHttpCompat.request(response).tag(DownloadOffSize::class.java)?.offSize ?: 0
        } else {
            0
        }
        var contentLength = OkHttpCompat.getContentLength(response)
        if (contentLength != -1L) contentLength += offsetSize
        if (contentLength == -1L) {
            LogUtil.log("Unable to calculate callback progress without `Content-Length` response header")
        }

        var lastProgress = 0
        var lastSize = 0L
        var lastRefreshTime = 0L

        writeTo(os) {
            val currentSize = it + offsetSize
            lastSize = currentSize
            if (contentLength == -1L) {
                //Callback only the number of bytes read
                val curTime = System.currentTimeMillis()
                if (curTime - lastRefreshTime > 500) {
                    callback.onProgress(0, currentSize, contentLength)
                    lastRefreshTime = curTime
                }
            } else {
                val currentProgress = ((currentSize * 100 / contentLength)).toInt()
                if (currentProgress > lastProgress) {
                    lastProgress = currentProgress
                    callback.onProgress(currentProgress, currentSize, contentLength)
                }
            }
        }
        if (contentLength == -1L) {
            //Promise that download end events can be callback if contentLength is -1
            callback.onProgress(100, lastSize, contentLength)
        }
    }
}
