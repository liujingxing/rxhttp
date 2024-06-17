package rxhttp.wrapper.parse

import okhttp3.Response
import rxhttp.wrapper.OkHttpCompat
import rxhttp.wrapper.callback.OutputStreamFactory
import rxhttp.wrapper.callback.ProgressCallback
import rxhttp.wrapper.callback.ProgressCallbackHelper
import rxhttp.wrapper.entity.DownloadOffSize
import rxhttp.wrapper.utils.LogTime
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
open class StreamParser<T>(
    private val osFactory: OutputStreamFactory<T>,
) : Parser<T> {

    private var callback: ProgressCallbackHelper? = null

    fun setProgressCallback(minPeriod: Int, progressCallback: ProgressCallback) {
        callback = ProgressCallbackHelper(minPeriod, progressCallback)
    }

    override fun onParse(response: Response): T {
        val body = OkHttpCompat.throwIfFail(response)
        val expandOs = osFactory.openOutputStream(response)
        val expand: T = expandOs.expand
        expandOs.use { os ->
            LogUtil.log("Download start: $expand")
            val logTime = LogTime()
            body.byteStream().use { it.writeTo(os, response, callback) }
            LogUtil.log("Download end, cost ${logTime.tookMs()}ms: $expand")
        }
        return expand
    }

    @Throws(IOException::class)
    private fun InputStream.writeTo(
        os: OutputStream,
        response: Response,
        callback: ProgressCallbackHelper?,
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

        callback.onStart(offsetSize)
        writeTo(os) { callback.onProgress(it.toLong(), contentLength) }
        if (contentLength == -1L) {
            //Promise that download end events can be callback if contentLength is -1
            callback.onProgress(-1, contentLength)
        }
    }
}
