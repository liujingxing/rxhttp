package rxhttp

import io.reactivex.Scheduler
import io.reactivex.functions.Consumer
import rxhttp.wrapper.entity.Progress
import rxhttp.wrapper.parse.Parser

/**
 * RxHttp代理类，需要扩展功能时，可以继承本类，并重写 [await] 方法 ,如：失败重试[RxHttpRetry] 超时 [RxHttpTimeout]
 * User: ljx
 * Date: 2020/3/21
 * Time: 23:06
 */
open class RxHttpProxy(
    protected val baseRxHttp: BaseRxHttp
) : BaseRxHttp() {

    override val breakDownloadOffSize: Long
        get() = baseRxHttp.breakDownloadOffSize

    override fun buildRequest() = baseRxHttp.buildRequest()

    override fun <T> asParser(parser: Parser<T>) = baseRxHttp.asParser(parser)

    override fun asDownload(
        destPath: String,
        progressConsumer: Consumer<Progress<String>>,
        observeOnScheduler: Scheduler?
    ) = baseRxHttp.asDownload(destPath, progressConsumer, observeOnScheduler)
}

/**
 * 失败重试，该方法仅在使用协程时才有效
 * @param times  重试次数, 默认Int.MAX_VALUE 代表不断重试
 * @param period 重试周期, 默认为0, 单位: milliseconds
 * @param test   重试条件, 默认为空，即无条件重试
 */
fun BaseRxHttp.retry(
    times: Int = Int.MAX_VALUE,
    period: Long = 0,
    test: ((Throwable) -> Boolean)? = null
) = RxHttpRetry(this, times, period, test)

/**
 * 为单个请求设置超时时长，该方法仅在使用协程时才有效
 * @param timeMillis 时长 单位: milliseconds
 * 注意: 要保证 timeMillis < OkHttp全局超时(连接+读+写)之和，否则无效
 */
fun BaseRxHttp.timeout(timeMillis: Long) = RxHttpTimeout(this, timeMillis)

