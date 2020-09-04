package rxhttp.wrapper.parse

import okhttp3.Response
import rxhttp.wrapper.exception.ExceptionHelper
import rxhttp.wrapper.utils.LogUtil
import java.io.IOException

/**
 * 通过此解析器，可拿到[okhttp3.Response]对象
 * User: ljx
 * Date: 2020-01-19
 * Time: 10:14
 */
class OkResponseParser : Parser<Response> {

    @Throws(IOException::class)
    override fun onParse(response: Response): Response {
        ExceptionHelper.throwIfFatal(response)
        LogUtil.log(response, null)
        return response
    }
}