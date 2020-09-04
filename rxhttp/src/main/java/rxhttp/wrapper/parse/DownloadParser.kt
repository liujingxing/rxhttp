package rxhttp.wrapper.parse

import okhttp3.Response
import rxhttp.wrapper.OkHttpCompat
import rxhttp.wrapper.exception.ExceptionHelper
import rxhttp.wrapper.utils.LogUtil
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

/**
 * File downloader
 * User: ljx
 * Date: 2018/10/23
 * Time: 13:49
 */
class DownloadParser(
    private val localPath: String,
) : IOParser<String>() {

    /**
     * When the download is complete, return to the local file path
     */
    @Throws(IOException::class)
    override fun onParse(response: Response): String {
        val localPath = localPath.replaceSuffix(response)
        val body = ExceptionHelper.throwIfFatal(response)
        LogUtil.log(response, localPath)

        val contentLength = OkHttpCompat.getContentLength(response)
        val offsetSize = OkHttpCompat.getDownloadOffSize(response)?.offSize ?: 0
        val append = OkHttpCompat.header(response, "Content-Range") != null

        //创建文件
        val dstFile = File(localPath).apply {
            val parentFile = parentFile
            if (!parentFile.exists() && !parentFile.mkdirs()) {
                throw IOException("Directory $parentFile create fail")
            }
        }

       //将输入流写出到文件
        write(body.byteStream(), FileOutputStream(dstFile, append), contentLength, offsetSize)
        return localPath
    }
}


private fun String.replaceSuffix(response: Response): String {
    return if (endsWith("/%s", true)
        || endsWith("/%1\$s", true)) {
        format(OkHttpCompat.pathSegments(response).last())
    } else {
        this
    }
}