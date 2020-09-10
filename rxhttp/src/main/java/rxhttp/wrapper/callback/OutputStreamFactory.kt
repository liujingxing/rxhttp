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
abstract class OutputStreamFactory<T>(
    var data: T
) {
    @Throws(IOException::class)
    abstract fun getOutputStream(response: Response): OutputStream
}

internal class UriOutputStreamFactory(
    val context: Context,
    uri: Uri
) : OutputStreamFactory<Uri>(uri) {
    override fun getOutputStream(response: Response): OutputStream {
        val append = response.header("Content-Range") != null
        return context.contentResolver.openOutputStream(data, if (append) "wa" else "w")
    }
}

internal class FileOutputStreamFactory(
    localPath: String
) : OutputStreamFactory<String>(localPath) {

    override fun getOutputStream(response: Response): OutputStream {
        data = data.replaceSuffix(response)
        //创建文件
        val dstFile = File(data).apply {
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