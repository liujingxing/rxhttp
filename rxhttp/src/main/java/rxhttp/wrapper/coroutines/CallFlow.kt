package rxhttp.wrapper.coroutines

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.AbstractFlow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.mapNotNull
import rxhttp.toAwait
import rxhttp.wrapper.BodyParamFactory
import rxhttp.wrapper.CallFactory
import rxhttp.wrapper.callback.ProgressCallback
import rxhttp.wrapper.entity.OkResponse
import rxhttp.wrapper.entity.Progress
import rxhttp.wrapper.parse.OkResponseParser
import rxhttp.wrapper.parse.Parser
import rxhttp.wrapper.parse.StreamParser

/**
 * User: ljx
 * Date: 2023/5/4
 * Time: 15:49
 */
@OptIn(ExperimentalCoroutinesApi::class)
class CallFlow<T>(
    private val callFactory: CallFactory,
    private val parser: Parser<T>
) : AbstractFlow<T>() {

    override suspend fun collectSafely(collector: FlowCollector<T>) {
        val await = callFactory.toAwait(parser)
        collector.emit(await.await())
    }

    fun toFlowOkResponse(): CallFlow<OkResponse<T?>> =
        CallFlow(callFactory, OkResponseParser(parser))

    /**
     * @param capacity 进度回调队列容量,生产速度大于消费速度时,事件会堆积在队列里,当堆积数量超出队列容量时，会丢弃旧的事件
     * 如果想要更多的进度回调事件,可设置一个相对较大的容量
     * @param minPeriod 上传/下载进度回调最小周期，必须大于0，默认500毫秒
     * @param progress 进度回调
     */
    fun onProgress(capacity: Int = 2, minPeriod: Int = 500, progress: suspend (Progress<T>) -> Unit): Flow<T> =
        toFlowProgress(capacity, minPeriod)
            .mapNotNull {
                if (it.result == null) progress(it)
                it.result
            }

    fun toFlowProgress(capacity: Int = 2, minPeriod: Int = 500): Flow<Progress<T>> {
        require(capacity in 2..100) { "capacity must be in [2..100], but it was $capacity" }
        require(minPeriod > 0) { "minPeriod must be between 0 and Int.MAX_VALUE, but it was $minPeriod" }
        var streamParser: Parser<*> = parser
        while (streamParser is OkResponseParser<*>) {
            streamParser = streamParser.parser
        }
        if (streamParser !is StreamParser && callFactory !is BodyParamFactory) {
            throw UnsupportedOperationException("parser is " + streamParser.javaClass.name + ", callFactory is " + callFactory.javaClass.name)
        }
        return channelFlow {
            val progressCallback = ProgressCallback { currentSize, totalSize, speed ->
                trySend(Progress<T>(currentSize, totalSize, speed))
            }
            if (streamParser is StreamParser) {
                streamParser.setProgressCallback(minPeriod, progressCallback)
            } else if (callFactory is BodyParamFactory) {
                callFactory.param.setProgressCallback(minPeriod, progressCallback)
            }
            val t: T = callFactory.toAwait(parser).await()
            trySend(Progress(t))
        }.buffer(capacity, BufferOverflow.DROP_OLDEST)
    }
}