package rxhttp

import io.reactivex.Scheduler
import io.reactivex.functions.Consumer
import kotlinx.coroutines.withTimeout
import okhttp3.OkHttpClient
import rxhttp.wrapper.entity.Progress
import rxhttp.wrapper.parse.Parser

/**
 * 超时处理
 * User: ljx
 * Date: 2020/3/21
 * Time: 17:06
 */
class RxHttpTimeout(
    private val baseRxHttp: BaseRxHttp,
    private var timeoutMillis: Long = 0L
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
        return withTimeout(timeoutMillis) {
            baseRxHttp.await(client, parser)
        }
    }
}

/**
 * @param timeMillis 超时时长  注意: 要保证 timeMillis < OkHttp全局超时(连接+读+写)之和，否则无效
 */
fun BaseRxHttp.timeout(timeMillis: Long) = RxHttpTimeout(this, timeMillis)