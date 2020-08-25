package rxhttp.wrapper.parse

import okhttp3.Response
import rxhttp.wrapper.OkHttpCompat
import rxhttp.wrapper.callback.ProgressCallback
import rxhttp.wrapper.exception.ExceptionHelper
import rxhttp.wrapper.utils.IOUtil
import rxhttp.wrapper.utils.LogUtil
import java.io.IOException

/**
 * File downloader
 * User: ljx
 * Date: 2018/10/23
 * Time: 13:49
 */
class DownloadParser(
    private val localPath: String,
    private val callback: ProgressCallback?
) : Parser<String> {

    var lastProgress = 0

    /**
     * When the download is complete, return to the local file path
     */
    @Throws(IOException::class)
    override fun onParse(response: Response): String {
        val localPath = localPath.replaceSuffix(response)
        val body = ExceptionHelper.throwIfFatal(response)
        LogUtil.log(response, false, localPath)
        val append = OkHttpCompat.header(response, "Content-Range") != null
        val contentLength = getContentLength(response)
        //将输入流写出到文件
        IOUtil.write(body.byteStream(), localPath, append) {
            callback?.apply {
                //当前进度 = 当前已读取的字节 / 总字节
                val currentProgress = ((it * 100f / contentLength)).toInt()
                if (currentProgress > lastProgress) {
                    lastProgress = currentProgress
                    callback.onProgress(currentProgress, it, contentLength)
                }
            }
        }
        return localPath
    }

    private fun String.replaceSuffix(response: Response): String {
        return if (endsWith("/%s", true)
            || endsWith("/%1\$s", true)) {
            format(OkHttpCompat.pathSegments(response).last())
        } else {
            this
        }
    }


    //从响应头 Content-Range 中，取 contentLength
    private fun getContentLength(response: Response): Long {
        var contentLength: Long = response.body?.contentLength() ?: -1
        if (contentLength != -1L) {
            return contentLength
        }
        val headerValue = response.header("Content-Range")
        if (headerValue != null) {
            //响应头Content-Range格式 : bytes 100001-20000000/20000001
            try {
                val divideIndex = headerValue.indexOf("/") //斜杠下标
                val blankIndex = headerValue.indexOf(" ")
                val fromToValue = headerValue.substring(blankIndex + 1, divideIndex)
                val split = fromToValue.split("-".toRegex()).toTypedArray()
                val start = split[0].toLong() //开始下载位置
                val end = split[1].toLong() //结束下载位置
                contentLength = end - start + 1 //要下载的总长度
            } catch (ignore: Exception) {
            }
        }
        return contentLength
    }
}