package rxhttp.wrapper.callback

import android.content.Context
import android.net.Uri
import okhttp3.Response
import rxhttp.wrapper.OkHttpCompat
import rxhttp.wrapper.entity.ExpandOutputStream
import rxhttp.wrapper.utils.isPartialContent
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

    // Range headers start index, equivalent to 'Range: bytes=offsetSize-'
    open fun offsetSize(): Long = 0

    @Throws(IOException::class)
    abstract fun openOutputStream(response: Response): ExpandOutputStream<T>
}

abstract class UriFactory(
    val context: Context
) : OutputStreamFactory<Uri>() {

    @Throws(IOException::class)
    abstract fun insert(response: Response): Uri

    open fun query(): Uri? = null

    override fun offsetSize() = query().length(context)

    final override fun openOutputStream(response: Response): ExpandOutputStream<Uri> {
        return ExpandOutputStream.open(context, insert(response), response.isPartialContent())
    }
}

class UriOutputStreamFactory(
    private val context: Context,
    private val uri: Uri
) : OutputStreamFactory<Uri>() {
    override fun offsetSize() = uri.length(context)

    override fun openOutputStream(response: Response): ExpandOutputStream<Uri> =
        ExpandOutputStream.open(context, uri, response.isPartialContent())
}

class FileOutputStreamFactory(
    private val localPath: String
) : OutputStreamFactory<String>() {
    override fun offsetSize() = File(localPath).length()

    override fun openOutputStream(response: Response): ExpandOutputStream<String> =
        File(localPath.replaceSuffix(response)).run {
            val parentFile = parentFile
            if (!parentFile.exists() && !parentFile.mkdirs()) {
                throw IOException("Directory $parentFile create fail")
            }
            ExpandOutputStream.open(this, response.isPartialContent())
        }

    private fun String.replaceSuffix(response: Response): String {
        return if (endsWith("/%s", true) || endsWith("/%1\$s", true)) {
            val filename = response.findFilename()
                ?: OkHttpCompat.pathSegments(response).last()
            format(filename)
        } else {
            this
        }
    }

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
                        var filename = keyValuePair[1]
                        //matches "test.apk" or 'test.apk'
                        if (filename.matches(Regex("^[\"'][\\s\\S]*[\"']\$"))) {
                            filename = filename.substring(1, filename.length - 1)
                        }
                        URLDecoder.decode(filename, "UTF-8")
                    }
                    "filename*" -> {
                        val filename = keyValuePair[1]
                        val firstIndex = filename.indexOf("'")
                        val lastIndex = filename.lastIndexOf("'")
                        if (firstIndex == -1 || lastIndex == -1 || firstIndex >= lastIndex) return null
                        val charset = filename.substring(0, firstIndex)
                        URLDecoder.decode(filename.substring(lastIndex + 1), charset)
                    }
                    else -> null
                }
            }
        }
        return null
    }
}