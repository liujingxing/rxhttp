package rxhttp.wrapper.parse

import okhttp3.Response
import java.io.IOException

/**
 *[okhttp3.Response] to T
 * User: ljx
 * Date: 2018/10/23
 * Time: 13:49
 */
interface Parser<T> {

    @Throws(IOException::class)
    fun onParse(response: Response): T
}