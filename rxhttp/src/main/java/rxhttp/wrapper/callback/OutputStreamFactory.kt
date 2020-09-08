package rxhttp.wrapper.callback

import android.content.Context
import android.net.Uri
import okhttp3.Response
import rxhttp.wrapper.OkHttpCompat
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream

/**
 * User: ljx
 * Date: 2020/9/8
 * Time: 22:12
 */
interface OutputStreamFactory {

    @Throws(IOException::class)
    fun getOutputStream(response: Response): OutputStream
}

class UriOutputStreamFactory(
    val context: Context,
    val uri: Uri
) : OutputStreamFactory {
    override fun getOutputStream(response: Response): OutputStream {
        val append = response.header("Content-Range") != null
        return context.contentResolver.openOutputStream(uri, if (append) "wa" else "w")
    }
}

class FileOutputStreamFactory(
    var localPath: String
) : OutputStreamFactory {

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