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
import rxhttp.wrapper.entity.ProgressT
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

    fun onProgress(capacity: Int = 2, progress: suspend (Progress) -> Unit): Flow<T> =
        toFlowProgress(capacity)
            .mapNotNull {
                if (it.result == null) progress(it)
                it.result
            }

    fun toFlowProgress(capacity: Int = 2): Flow<ProgressT<T>> {
        require(capacity in 2..100) { "capacity must be in [2..100], but it was $capacity" }
        var streamParser: Parser<*> = parser
        while (streamParser is OkResponseParser<*>) {
            streamParser = streamParser.parser
        }
        if (streamParser !is StreamParser && callFactory !is BodyParamFactory) {
            throw UnsupportedOperationException("parser is " + streamParser.javaClass.name + ", callFactory is " + callFactory.javaClass.name)
        }
        return channelFlow {
            val progressCallback = ProgressCallback { progress, currentSize, totalSize ->
                trySend(ProgressT<T>(progress, currentSize, totalSize))
            }
            if (streamParser is StreamParser) {
                streamParser.progressCallback = progressCallback
            } else if (callFactory is BodyParamFactory) {
                callFactory.param.setProgressCallback(progressCallback)
            }
            val t: T = callFactory.toAwait(parser).await()
            trySend(ProgressT(t))
        }.buffer(capacity, BufferOverflow.DROP_OLDEST)
    }
}