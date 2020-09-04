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
class DownloadParser @JvmOverloads constructor(
    private val localPath: String,
    var callback: ProgressCallback? = null,
) : Parser<String> {

    private var lastProgress = 0

    /**
     * When the download is complete, return to the local file path
     */
    @Throws(IOException::class)
    override fun onParse(response: Response): String {
        val offsetSize = OkHttpCompat.getDownloadOffSize(response)?.offSize ?: 0
        val localPath = localPath.replaceSuffix(response)
        val body = ExceptionHelper.throwIfFatal(response)
        LogUtil.log(response, localPath)
        val append = OkHttpCompat.header(response, "Content-Range") != null
        val contentLength = OkHttpCompat.getContentLength(response) + offsetSize
        //将输入流写出到文件
        IOUtil.write(body.byteStream(), localPath, append) {
            callback?.apply {
                val currentSize = it + offsetSize
                //当前进度 = 当前已读取的字节 / 总字节
                val currentProgress = ((currentSize * 100f / contentLength)).toInt()
                if (currentProgress > lastProgress) {
                    lastProgress = currentProgress
                    onProgress(currentProgress, currentSize, contentLength)
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
}