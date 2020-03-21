package rxhttp

import io.reactivex.Scheduler
import io.reactivex.functions.Consumer
import kotlinx.coroutines.delay
import okhttp3.OkHttpClient
import rxhttp.wrapper.entity.Progress
import rxhttp.wrapper.parse.Parser

/**
 * 失败重试
 * User: ljx
 * Date: 2020/3/21
 * Time: 17:06
 */
class RxHttpRetry(
    private val baseRxHttp: BaseRxHttp,
    private var times: Int = 0,
    private val period: Long = 0L,
    private val test: ((Throwable) -> Boolean)? = null
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

    override suspend fun <T> await(client: OkHttpClient, parser: Parser<T>): T {
        return try {
            baseRxHttp.await(client, parser)
        } catch (e: Throwable) {
            val remaining = times  //剩余次数
            if (remaining != Int.MAX_VALUE) {
                times = remaining - 1
            }
            val pass = test?.invoke(e) ?: true
            if (remaining > 0 && pass) {
                delay(period)
                await(client, parser) //递归，直到剩余次数为0
            } else throw e
        }
    }
}

/**
 * @param times  重试次数, 默认Int.MAX_VALUE 代表不断重试
 * @param period 重试周期, 默认为0, 单位: milliseconds
 * @param test   重试条件, 默认为空，即无条件重试
 */
fun BaseRxHttp.retry(
    times: Int = Int.MAX_VALUE,
    period: Long = 0,
    test: ((Throwable) -> Boolean)? = null
) = RxHttpRetry(this, times, period, test)

