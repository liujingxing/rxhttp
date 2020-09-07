package rxhttp.wrapper.parse

import okhttp3.Response
import rxhttp.wrapper.OkHttpCompat
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream

/**
 * File downloader
 * User: ljx
 * Date: 2018/10/23
 * Time: 13:49
 */
class DownloadParser(
    private var localPath: String,
) : StreamParser() {

    override fun onParse(response: Response): String {
        super.onParse(response)
        return localPath
    }

    override fun getOutputStream(response: Response): OutputStream {
        localPath = localPath.replaceSuffix(response)
        //创建文件
        val dstFile = File(localPath).apply {
            val parentFile = parentFile
            if (!parentFile.exists() && !parentFile.mkdirs()) {
                throw IOException("Directory $parentFile create fail")
            }
        }
        val append = OkHttpCompat.header(response, "Content-Range") != null
        return FileOutputStream(dstFile, append)
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