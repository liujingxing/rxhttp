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
class StreamParser(
    private val outputStream: OutputStream,
) : IOParser<String>() {

    override fun onParse(response: Response): String {
        val offsetSize = OkHttpCompat.getDownloadOffSize(response)?.offSize ?: 0
        val body = ExceptionHelper.throwIfFatal(response)
        LogUtil.log(response, "")
        val contentLength = OkHttpCompat.getContentLength(response)
        write(body.byteStream(), outputStream, contentLength, offsetSize)
        return ""
    }
}