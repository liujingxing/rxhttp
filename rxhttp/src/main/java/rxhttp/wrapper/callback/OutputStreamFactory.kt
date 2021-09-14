package rxhttp.wrapper.callback

import android.content.Context
import android.net.Uri
import okhttp3.Response
import rxhttp.wrapper.OkHttpCompat
import rxhttp.wrapper.entity.ExpandOutputStream
import rxhttp.wrapper.entity.toOutputStream
import rxhttp.wrapper.utils.length
import java.io.File
import java.io.IOException
import java.net.URLDecoder

/**
 * User: ljx
 * Date: 2020/9/8
 * Time: 22:12
 */
abstract class OutputStreamFactory<T> {

    //断点下载时的偏移量
    open fun offsetSize(): Long = 0

    @Throws(IOException::class)
    abstract fun getOutputStream(response: Response): ExpandOutputStream<T>
}

abstract class UriFactory(
    val context: Context
) : OutputStreamFactory<Uri>() {

    @Throws(IOException::class)
    abstract fun insert(response: Response): Uri

    open fun query(): Uri? = null

    override fun offsetSize() = query().length(context)

    final override fun getOutputStream(response: Response): ExpandOutputStream<Uri> {
        return insert(response).toOutputStream(context, response.append)
    }
}

class UriOutputStreamFactory(
    private val context: Context,
    private val uri: Uri
) : OutputStreamFactory<Uri>() {
    override fun offsetSize() = uri.length(context)

    override fun getOutputStream(response: Response): ExpandOutputStream<Uri> =
        uri.toOutputStream(context, response.append)
}

class FileOutputStreamFactory(
    private val localPath: String
) : OutputStreamFactory<String>() {
    override fun offsetSize() = File(localPath).length()

    override fun getOutputStream(response: Response): ExpandOutputStream<String> =
        File(localPath.replaceSuffix(response)).run {
            val parentFile = parentFile
            if (!parentFile.exists() && !parentFile.mkdirs()) {
                throw IOException("Directory $parentFile create fail")
            }
            toOutputStream(response.append)
        }
}

private fun String.replaceSuffix(response: Response): String {
    return if (endsWith("/%s", true)
        || endsWith("/%1\$s", true)
    ) {
        val filename = response.findFilename()
            ?: OkHttpCompat.pathSegments(response).last()
        format(filename)
    } else {
        this
    }
}

private val Response.append
    get() = OkHttpCompat.header(this, "Content-Range") != null

/**
 * find filename form Content-Disposition response headers
 * For example:
 * Content-Disposition: attachment; filename=test.apk
 * Content-Disposition: attachment; filename='test.apk'
 * Content-Disposition: attachment; filename="test.apk"
 * Content-Disposition: attachment;filename*=UTF-8'zh_cn'%E6%B5%8B%E8%AF%95.apk
 */
private fun Response.findFilename(): String? {
    val header = OkHttpCompat.header(this, "Content-Disposition") ?: return null
    header.split(";").forEach {
        val keyValuePair = it.split("=")
        if (keyValuePair.size > 1) {
            return when (keyValuePair[0].trim()) {
                "filename" -> {
                    keyValuePair[1].run {
                        //matches "test.apk" or 'test.apk'
                        if (matches(Regex("^[\"'][\\s\\S]*[\"']\$"))) {
                            substring(1, length - 1)
                        } else {
                            this
                        }
                    }
                }
                "filename*" -> {
                    keyValuePair[1].run {
                        val firstIndex = indexOf("'")
                        val lastIndex = lastIndexOf("'")
                        if (firstIndex == -1 || lastIndex == -1 || firstIndex >= lastIndex) return null
                        URLDecoder.decode(substring(lastIndex + 1), substring(0, firstIndex))
                    }
                }
                else -> null
            }
        }
    }
    return null
}