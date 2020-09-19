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

inline fun <T> newOutputStreamFactory(
    crossinline uriFactory: (Response) -> OutputStreamWrapper<T>
): OutputStreamFactory<T> = object : OutputStreamFactory<T>() {
    override fun getOutputStream(response: Response): OutputStreamWrapper<T> {
        return uriFactory(response)
    }
}

internal fun newOutputStreamFactory(
    context: Context,
    uri: Uri
): OutputStreamFactory<Uri> = newOutputStreamFactory {
    val append = it.header("Content-Range") != null
    val os: OutputStream = context.contentResolver.openOutputStream(uri, if (append) "wa" else "w")
    os.toWrapper(uri)
}

internal fun newOutputStreamFactory(
    localPath: String
): OutputStreamFactory<String> = newOutputStreamFactory {
    val destPath = localPath.replaceSuffix(it)
    //创建文件
    val dstFile = File(destPath).apply {
        val parentFile = parentFile
        if (!parentFile.exists() && !parentFile.mkdirs()) {
            throw IOException("Directory $parentFile create fail")
        }
    }
    val append = OkHttpCompat.header(it, "Content-Range") != null
    FileOutputStream(dstFile, append).toWrapper(destPath)
}

private fun String.replaceSuffix(response: Response): String {
    return if (endsWith("/%s", true)
        || endsWith("/%1\$s", true)) {
        format(OkHttpCompat.pathSegments(response).last())
    } else {
        this
    }
}