package rxhttp.wrapper.entity

import java.io.File

/**
 * User: ljx
 * Date: 2018/12/21
 * Time: 09:21
 */
class UpFile @JvmOverloads constructor(
    val key: String,
    val file: File,
    private var filename: String? = null,
    val skipSize: Long = 0,
) {

    constructor(key: String, path: String) : this(key, File(path))

    @Deprecated("", ReplaceWith("setFileName(fileName)"))
    fun setValue(filename: String?) {
        setFilename(filename)
    }

    @Deprecated("", ReplaceWith("getFilename()"))
    fun getValue(): String {
        return getFilename()
    }

    fun setFilename(fileName: String?) {
        this.filename = fileName
    }

    fun getFilename(): String {
        return filename ?: file.name
    }
}