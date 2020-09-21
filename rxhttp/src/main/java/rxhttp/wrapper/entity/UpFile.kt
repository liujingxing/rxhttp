package rxhttp.wrapper.entity

import java.io.File

/**
 * User: ljx
 * Date: 2018/12/21
 * Time: 09:21
 */
class UpFile(
    val key: String,
    private var fileName: String?,
    val file: File
) {

    constructor(key: String, path: String) : this(key, null, File(path))
    constructor(key: String, file: File) : this(key, null, file)
    constructor(key: String, fileName: String?, path: String) : this(key, fileName, File(path))

    @Deprecated("", ReplaceWith("setFileName(fileName)"))
    fun setValue(fileName: String?) {
        setFileName(fileName)
    }

    fun setFileName(fileName: String?) {
        this.fileName = fileName
    }

    fun getFileName(): String {
        return fileName ?: file.name
    }
}