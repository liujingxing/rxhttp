package rxhttp.wrapper.intercept

import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import rxhttp.wrapper.OkHttpCompat
import rxhttp.wrapper.utils.LogTime
import rxhttp.wrapper.utils.LogUtil


/**
 * Print the request start/end log
 *
 * User: ljx
 * Date: 2021/10/17
 * Time: 16:42
 */
class LogInterceptor(private val okClient: OkHttpClient) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        // Prints the request start log
        LogUtil.log(request, OkHttpCompat.cookieJar(okClient))
        val logTime = LogTime()
        val response = chain.proceed(request)
        // Prints the request end log
        LogUtil.log(response, logTime)
        return response
    }
}