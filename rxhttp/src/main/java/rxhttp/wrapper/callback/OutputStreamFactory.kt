package rxhttp.wrapper.callback

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
    val context: Context,
    val queryUri: Uri? = null,
    val fileName: String? = null,
) : OutputStreamFactory<Uri>() {

    @Throws(IOException::class)
    abstract fun getUri(response: Response): Uri

    final override fun getOutputStream(response: Response): OutputStreamWrapper<Uri> {
        val append = response.header("Content-Range") != null
        return getUri(response).toWrapper(context, append)
    }

    fun getAppendUri(): AppendUri? {
        return queryUri?.findUriByFileName(context, fileName)
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

//find the Uri by filename, return null if find fail
private fun Uri.findUriByFileName(context: Context, fileName: String?): AppendUri? {
    if (fileName.isNullOrEmpty()) return null
    val columnNames = arrayOf(
        MediaStore.MediaColumns._ID,
        MediaStore.MediaColumns.SIZE,
    )
    return context.contentResolver.query(this, columnNames,
        MediaStore.MediaColumns.DISPLAY_NAME + "=?", arrayOf(fileName), null).use {
        if (it.moveToFirst()) {
            val uriId = it.getString(0)
            val newUri = this.buildUpon().appendPath(uriId).build()
            AppendUri(newUri, it.getLong(1))
        } else null
    }
}