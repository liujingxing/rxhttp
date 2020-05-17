package rxhttp.wrapper.cahce

import okhttp3.internal.http.StatusLine
import java.io.IOException

/**
 * 此类的作用在于兼用OkHttp版本  注意: 本类一定要用Kotlin语言编写，Java将无法兼容新老版本
 * User: ljx
 * Date: 2020/5/17
 * Time: 15:47
 */
object StatusLineUtil {
    @JvmStatic
    @Throws(IOException::class)
    fun parse(statusLine: String): StatusLine {
        return StatusLine.parse(statusLine)
    }
}

