package rxhttp.wrapper.callback

import android.content.Context
import android.net.Uri
import okhttp3.Response
import rxhttp.wrapper.OkHttpCompat
import rxhttp.wrapper.entity.OutputStreamWrapper
import rxhttp.wrapper.entity.toWrapper
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream

/**
 * User: ljx
 * Date: 2020/9/8
 * Time: 22:12
 */
abstract class OutputStreamFactory<T> {

    @Throws(IOException::class)
    abstract fun getOutputStream(response: Response): OutputStreamWrapper<T>
}

internal class UriOutputStreamFactory(
    private val context: Context,
    private val uri: Uri
) : OutputStreamFactory<Uri>() {
    override fun getOutputStream(response: Response): OutputStreamWrapper<Uri> {
        val append = response.header("Content-Range") != null
        val os: OutputStream = context.contentResolver.openOutputStream(uri, if (append) "wa" else "w")
        return os.toWrapper(uri)
    }
}

internal class FileOutputStreamFactory(
    private val localPath: String
) : OutputStreamFactory<String>() {

    override fun getOutputStream(response: Response): OutputStreamWrapper<String> {
        val localPath = localPath.replaceSuffix(response)
        //创建文件
        val dstFile = File(localPath).apply {
            val parentFile = parentFile
            if (!parentFile.exists() && !parentFile.mkdirs()) {
                throw IOException("Directory $parentFile create fail")
            }
        }
        val append = OkHttpCompat.header(response, "Content-Range") != null
        return FileOutputStream(dstFile, append).toWrapper(localPath)
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