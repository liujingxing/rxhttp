package rxhttp.wrapper.entity

import java.io.File

/**
 * User: ljx
 * Date: 2018/12/21
 * Time: 09:21
 */
class UpFile(
    val key: String,
    private var filename: String?,
    val file: File
) {

    constructor(key: String, path: String) : this(key, null, File(path))
    constructor(key: String, file: File) : this(key, null, file)
    constructor(key: String, filename: String?, path: String) : this(key, filename, File(path))

    @Deprecated("", ReplaceWith("setFileName(fileName)"))
    fun setValue(filename: String?) {
        setFilename(filename)
    }

    fun setFilename(fileName: String?) {
        this.filename = fileName
    }

    fun getFilename(): String {
        return filename ?: file.name
    }
}