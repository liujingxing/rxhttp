package rxhttp.wrapper.parse

import okhttp3.Response
import rxhttp.wrapper.callback.FileOutputStreamFactory
import rxhttp.wrapper.callback.OutputStreamFactory
import rxhttp.wrapper.callback.UriOutputStreamFactory
import rxhttp.wrapper.exception.ExceptionHelper
import rxhttp.wrapper.utils.LogUtil

/**
 * User: ljx
 * Date: 2020/9/4
 * Time: 21:39
 */
class StreamParser(
    private val osFactory: OutputStreamFactory
) : IOParser() {

    override fun onParse(response: Response): String {
        val body = ExceptionHelper.throwIfFatal(response)
        val os = osFactory.getOutputStream(response);
        val msg = when (osFactory) {
            is FileOutputStreamFactory -> osFactory.localPath
            is UriOutputStreamFactory -> osFactory.uri.toString()
            else -> ""
        }
        LogUtil.log(response, msg)
        response.writeTo(body, os, callback)
        return msg
    }
}