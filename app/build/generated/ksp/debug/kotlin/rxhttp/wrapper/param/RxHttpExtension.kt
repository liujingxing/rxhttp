package rxhttp.wrapper.`param`

import kotlin.collections.List
import rxhttp.CallFlow
import rxhttp.toAwait
import rxhttp.toFlow
import rxhttp.wrapper.CallFactory
import rxhttp.wrapper.coroutines.Await
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

public inline fun <reified T> CallFactory.toFlowResponse(): CallFlow<T> =
    toFlow(BaseRxHttp.wrapResponseParser<T>(javaTypeOf<T>()))
