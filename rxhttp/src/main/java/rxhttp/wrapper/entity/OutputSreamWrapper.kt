package rxhttp.wrapper.entity

import android.content.Context
import android.net.Uri
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

/**
 * @param result When the download is complete, the object is returned
 * @param os Download OutputStream
 */
data class OutputStreamWrapper<out T>(
    val result: T,
    val os: OutputStream
) {
    override fun toString(): String = "($result, $os)"
}

fun File.toWrapper(append: Boolean = false) =
    OutputStreamWrapper(absolutePath, FileOutputStream(this, append))

fun Uri.toWrapper(
    context: Context,
    append: Boolean = false
): OutputStreamWrapper<Uri> {
    val os = context.contentResolver.openOutputStream(this, if (append) "wa" else "w")
    return OutputStreamWrapper(this, os)
}