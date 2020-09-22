package rxhttp.wrapper.callback

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import okhttp3.Response
import rxhttp.wrapper.OkHttpCompat
import rxhttp.wrapper.entity.AppendUri
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
    abstract fun getUri(response: Response): Uri

    open fun getAppendUri(): AppendUri? = null

    final override fun getOutputStream(response: Response): OutputStreamWrapper<Uri> {
        val append = response.header("Content-Range") != null
        return getUri(response).toWrapper(context, append)
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
        format(OkHttpCompat.pathSegments(response).last())
    } else {
        this
    }
}

//find the Uri by filename and relativePath, return null if find fail
fun Uri.findUriByFileName(context: Context, filename: String?, relativePath: String?): AppendUri? {
    if (filename.isNullOrEmpty() || relativePath.isNullOrEmpty()) return null
    val realRelativePath = relativePath.run {
        //Remove the prefix slash if it exists
        if (startsWith("/")) substring(1) else this
    }.run {
        //Suffix adds a slash if it does not exist
        if (endsWith("/")) this else "$this/"
    }
    val columnNames = arrayOf(
        MediaStore.MediaColumns._ID,
        MediaStore.MediaColumns.SIZE,
    )
    return context.contentResolver.query(this, columnNames,
        "relative_path=? AND _display_name=?", arrayOf(realRelativePath, filename), null).use {
        if (it.moveToFirst()) {
            val uriId = it.getLong(0)
            val newUri = ContentUris.withAppendedId(this, uriId)
            AppendUri(newUri, it.getLong(1))
        } else null
    }
}