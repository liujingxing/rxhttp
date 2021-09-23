package rxhttp.wrapper.entity

import android.content.Context
import android.net.Uri
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

/**
 * @param expand When the download is complete, the object is returned
 * @param os Download OutputStream
 */
class ExpandOutputStream<out T>(
    val expand: T,
    val os: OutputStream
) {
    override fun toString(): String = "($expand, $os)"
}

internal fun File.toOutputStream(append: Boolean = false) =
    ExpandOutputStream(absolutePath, FileOutputStream(this, append))

internal fun Uri.toOutputStream(
    context: Context,
    append: Boolean = false
): ExpandOutputStream<Uri> {
    val os = context.contentResolver.openOutputStream(this, if (append) "wa" else "w")
    return ExpandOutputStream(this, os)
}