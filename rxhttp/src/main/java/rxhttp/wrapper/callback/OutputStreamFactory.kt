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

/**
 * User: ljx
 * Date: 2020/9/8
 * Time: 22:12
 */
abstract class OutputStreamFactory<T> {

    @Throws(IOException::class)
    abstract fun getOutputStream(response: Response): OutputStreamWrapper<T>
}

abstract class UriFactory(
    val context: Context
) : OutputStreamFactory<Uri>() {

    @Throws(IOException::class)
    abstract fun insert(response: Response): Uri

    open fun query(): Uri? = null

    final override fun getOutputStream(response: Response): OutputStreamWrapper<Uri> {
        val append = response.header("Content-Range") != null
        return insert(response).toWrapper(context, append)
    }
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
    uri.toWrapper(context, append)
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
        val filename = response.findFilename()
            ?: OkHttpCompat.pathSegments(response).last()
        format(filename)
    } else {
        this
    }
}

private fun Response.findFilename(): String? {
    val header = header("Content-Disposition") ?: return null
    header.split(";").forEach {
        val keyValuePair = it.split("=")
        if (keyValuePair.size > 1 && keyValuePair[0] == " filename") {
            val filename = keyValuePair[1]
            return filename.substring(1, filename.length - 1)
        }
    }
    return null
}