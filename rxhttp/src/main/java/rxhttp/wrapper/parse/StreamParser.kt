package rxhttp.wrapper.parse

import okhttp3.Response
import rxhttp.wrapper.OkHttpCompat
import rxhttp.wrapper.exception.ExceptionHelper
import rxhttp.wrapper.utils.LogUtil
import java.io.OutputStream

/**
 * User: ljx
 * Date: 2020/9/4
 * Time: 21:39
 */
open class StreamParser(
    private val outputStream: OutputStream? = null
) : IOParser<String>() {

    override fun onParse(response: Response): String {
        val body = ExceptionHelper.throwIfFatal(response)
        LogUtil.log(response, "")
        val contentLength = OkHttpCompat.getContentLength(response)
        val offsetSize = OkHttpCompat.getDownloadOffSize(response)?.offSize ?: 0
        write(body.byteStream(), getOutputStream(response), contentLength, offsetSize)
        return ""
    }

    protected open fun getOutputStream(response: Response) = outputStream
}