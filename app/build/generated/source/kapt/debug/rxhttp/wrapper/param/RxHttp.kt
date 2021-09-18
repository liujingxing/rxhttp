package rxhttp.wrapper.`param`

import com.example.httpsender.parser.ResponseParser
import kotlin.Any
import kotlin.Unit
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import rxhttp.BodyParamFactory
import rxhttp.CallFactory
import rxhttp.onEachProgress
import rxhttp.toFlow
import rxhttp.toFlowProgress
import rxhttp.toParser
import rxhttp.wrapper.entity.Progress
import rxhttp.wrapper.parse.SimpleParser

public inline fun <reified T> RxHttp<*, *>.executeList() = executeClass<List<T>>()

public inline fun <reified T> RxHttp<*, *>.executeClass() = execute(object : SimpleParser<T>() {})

public inline fun <reified T> BaseRxHttp.asList() = asClass<List<T>>()

public inline fun <reified K, reified V> BaseRxHttp.asMap() = asClass<Map<K,V>>()

public inline fun <reified T> BaseRxHttp.asClass() = asParser(object : SimpleParser<T>() {})

public inline fun <reified T : Any> BaseRxHttp.asResponse() = asParser(object: ResponseParser<T>()
    {})

/**
 * 调用此方法监听上传进度                                                    
 * @param coroutine  CoroutineScope对象，用于开启协程回调进度，进度回调所在线程取决于协程所在线程
 * @param progress 进度回调  
 */
public fun <P : AbstractBodyParam<P>, R : RxHttpAbstractBodyParam<P, R>> RxHttpAbstractBodyParam<P,
    R>.upload(coroutine: CoroutineScope, progressCallback: suspend (Progress) -> Unit): R {
  param.setProgressCallback { progress, currentSize, totalSize ->
      coroutine.launch { progressCallback(Progress(progress, currentSize, totalSize)) }
  }
  @Suppress("UNCHECKED_CAST")
  return this as R
}

public inline fun <reified T : Any> CallFactory.toResponse() = toParser(object: ResponseParser<T>()
    {})

public inline fun <reified T : Any> CallFactory.toFlowResponse() = toFlow(toResponse<T>())

@ExperimentalCoroutinesApi
public inline fun <reified T : Any> BodyParamFactory.toFlowResponseProgress() =
    toFlowProgress(toResponse<T>())

@ExperimentalCoroutinesApi
public inline fun <reified T : Any> BodyParamFactory.toFlowResponse(noinline progress: suspend
    (Progress) -> Unit) = toFlowResponseProgress<T>().onEachProgress(progress)
