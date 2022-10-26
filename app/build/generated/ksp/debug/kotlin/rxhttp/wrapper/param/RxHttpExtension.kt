package rxhttp.wrapper.`param`

import kotlin.Int
import kotlin.Unit
import rxhttp.toAwait
import rxhttp.toFlow
import rxhttp.toFlowProgress
import rxhttp.wrapper.BodyParamFactory
import rxhttp.wrapper.CallFactory
import rxhttp.wrapper.entity.Progress
import rxhttp.wrapper.utils.javaTypeOf

public inline fun <reified T> BaseRxHttp.executeList() = executeClass<List<T>>()

public inline fun <reified T> BaseRxHttp.executeClass() = executeClass<T>(javaTypeOf<T>())

public inline fun <reified T> BaseRxHttp.toObservableList() = toObservable<List<T>>()

public inline fun <reified V> BaseRxHttp.toObservableMapString() = toObservable<Map<String, V>>()

public inline fun <reified T> BaseRxHttp.toObservable() = toObservable<T>(javaTypeOf<T>())

public inline fun <reified T> BaseRxHttp.toObservableResponse() =
    toObservableResponse<T>(javaTypeOf<T>())

public inline fun <reified T> CallFactory.toAwaitResponse() =
    toAwait(BaseRxHttp.wrapResponseParser<T>(javaTypeOf<T>()))

public inline fun <reified T> CallFactory.toFlowResponse() = toFlow(toAwaitResponse<T>())

public inline fun <reified T> BodyParamFactory.toFlowResponse(capacity: Int = 1, noinline
    progress: suspend (Progress) -> Unit) = toFlow(toAwaitResponse<T>(), capacity, progress)

public inline fun <reified T> BodyParamFactory.toFlowResponseProgress(capacity: Int = 1) =
    toFlowProgress(toAwaitResponse<T>(), capacity)
