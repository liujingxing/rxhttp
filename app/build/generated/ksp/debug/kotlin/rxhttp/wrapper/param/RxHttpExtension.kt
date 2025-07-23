package rxhttp.wrapper.`param`

import kotlin.collections.List
import rxhttp.toAwait
import rxhttp.toFlow
import rxhttp.wrapper.CallFactory
import rxhttp.wrapper.coroutines.CallAwait
import rxhttp.wrapper.coroutines.CallFlow
import rxhttp.wrapper.utils.javaTypeOf

public inline fun <reified T> BaseRxHttp.executeList(): List<T> = executeClass<List<T>>()

public inline fun <reified T> BaseRxHttp.executeClass(): T = executeClass<T>(javaTypeOf<T>())

public inline fun <reified T> BaseRxHttp.toObservableList(): ObservableCall<List<T>> = toObservable<List<T>>()

public inline fun <reified T> BaseRxHttp.toObservable(): ObservableCall<T> = toObservable<T>(javaTypeOf<T>())

public inline fun <reified T> BaseRxHttp.toObservableResponse(): ObservableCall<T> = toObservableResponse<T>(javaTypeOf<T>())

public inline fun <reified T> CallFactory.toAwaitResponse(): CallAwait<T> = toAwait(BaseRxHttp.wrapResponseParser(javaTypeOf<T>()))

public inline fun <reified T> CallFactory.toFlowResponse(): CallFlow<T> = toFlow(BaseRxHttp.wrapResponseParser(javaTypeOf<T>()))
