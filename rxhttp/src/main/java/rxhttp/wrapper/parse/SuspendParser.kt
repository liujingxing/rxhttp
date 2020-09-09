package rxhttp.wrapper.parse

import okhttp3.Response
import java.io.IOException

/**
 *[okhttp3.Response] to T
 * User: ljx
 * Date: 2018/10/23
 * Time: 13:49
 */
abstract class SuspendParser<T> : Parser<T> {

    override fun onParse(response: Response): T {
        throw UnsupportedOperationException("Should be call onSuspendParse fun")
    }

    @Throws(IOException::class)
    abstract suspend fun onSuspendParse(response: Response): T
}