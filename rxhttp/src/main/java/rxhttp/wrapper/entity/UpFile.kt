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
    var filename: String? = file.name,
    val skipSize: Long = 0,
) {

    constructor(key: String, path: String) : this(key, File(path))
}