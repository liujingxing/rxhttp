package rxhttp.wrapper.parse

import okhttp3.Response
import okhttp3.ResponseBody
import rxhttp.wrapper.OkHttpCompat
import rxhttp.wrapper.callback.OutputStreamFactory
import rxhttp.wrapper.callback.ProgressCallback
import rxhttp.wrapper.entity.DownloadOffSize
import rxhttp.wrapper.utils.LogUtil
import rxhttp.wrapper.utils.writeTo
import java.io.IOException
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
        val expandOutputStream = osFactory.getOutputStream(response)
        val expand = expandOutputStream.expand
        LogUtil.log(response, expand.toString())
        val os = expandOutputStream.os
        progressCallback?.let {
            response.writeTo(body, os, it)
        } ?: body.byteStream().writeTo(os)
        return expand
    }
}

@Throws(IOException::class)
private fun Response.writeTo(
    body: ResponseBody,
    os: OutputStream,
    callback: ProgressCallback,
) {
    val offsetSize = OkHttpCompat.request(this).tag(DownloadOffSize::class.java)?.offSize ?: 0
    var contentLength = OkHttpCompat.getContentLength(this)
    if (contentLength != -1L) contentLength += offsetSize
    if (contentLength == -1L) {
        LogUtil.log("Unable to calculate callback progress without `Content-Length` response header")
    }

    var lastProgress = 0
    var lastSize = 0L
    var lastRefreshTime = 0L

    body.byteStream().writeTo(os) {
        val currentSize = it + offsetSize
        lastSize = currentSize
        if (contentLength == -1L) {
            //响应头里取不到contentLength，仅回调已下载字节数
            val curTime = System.currentTimeMillis()
            if (curTime - lastRefreshTime > 500) {
                callback.onProgress(0, currentSize, contentLength)
                lastRefreshTime = curTime
            }
        } else {
            //当前进度 = 当前已读取的字节 * 100 / 总字节
            val currentProgress = ((currentSize * 100 / contentLength)).toInt()
            if (currentProgress > lastProgress) {
                lastProgress = currentProgress
                callback.onProgress(currentProgress, currentSize, contentLength)
            }
        }
    }
    if (contentLength == -1L) {
        //响应头里取不到contentLength时，保证下载完成事件能回调
        callback.onProgress(100, lastSize, contentLength)
    }
}