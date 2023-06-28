package rxhttp.wrapper.`param`

import kotlin.Int
import kotlin.Unit
import kotlin.collections.List
import kotlinx.coroutines.flow.Flow
import rxhttp.toAwait
import rxhttp.toFlow
import rxhttp.toFlowProgress
import rxhttp.wrapper.BodyParamFactory
import rxhttp.wrapper.CallFactory
import rxhttp.wrapper.coroutines.Await
import rxhttp.wrapper.entity.Progress
import rxhttp.wrapper.entity.ProgressT
import rxhttp.wrapper.utils.javaTypeOf

public inline fun <reified T> BaseRxHttp.executeList(): List<T> = executeClass<List<T>>()

public inline fun <reified T> BaseRxHttp.executeClass(): T = executeClass<T>(javaTypeOf<T>())

public inline fun <reified T> BaseRxHttp.toObservableList(): ObservableCall<List<T>> =
    toObservable<List<T>>()

public inline fun <reified V> BaseRxHttp.toObservableMapString(): ObservableCall<Map<String,V>> =
    toObservable<Map<String, V>>()

public inline fun <reified T> BaseRxHttp.toObservable(): ObservableCall<T> =
    toObservable<T>(javaTypeOf<T>())

public inline fun <reified T> BaseRxHttp.toObservableResponse(): ObservableCall<T> =
    toObservableResponse<T>(javaTypeOf<T>())

public inline fun <reified T> CallFactory.toAwaitResponse(): Await<T> =
    toAwait(BaseRxHttp.wrapResponseParser<T>(javaTypeOf<T>()))

public inline fun <reified T> CallFactory.toFlowResponse(): Flow<T> = toFlow(toAwaitResponse<T>())

public inline fun <reified T> BodyParamFactory.toFlowResponse(capacity: Int = 2, noinline
    progress: suspend (Progress) -> Unit): Flow<T> = toFlow(toAwaitResponse<T>(), capacity,
    progress)

public inline fun <reified T> BodyParamFactory.toFlowResponseProgress(capacity: Int = 2):
    Flow<ProgressT<T>> = toFlowProgress(toAwaitResponse<T>(), capacity)
