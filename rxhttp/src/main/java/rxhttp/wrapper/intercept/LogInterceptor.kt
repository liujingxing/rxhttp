package rxhttp.wrapper.intercept

import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import rxhttp.wrapper.OkHttpCompat
import rxhttp.wrapper.utils.LogTime
import rxhttp.wrapper.utils.LogUtil


/**
 * Only the request start log is printed
 *
 * User: ljx
 * Date: 2021/10/17
 * Time: 16:42
 */
class LogInterceptor(private val okClient: OkHttpClient) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        var request = chain.request()
        // Prints the request start log
        LogUtil.log(request, OkHttpCompat.cookieJar(okClient))
        // Record the request start time
        request = request.newBuilder()
            .tag(LogTime::class.java, LogTime())
            .build()
        return chain.proceed(request)
    }
}