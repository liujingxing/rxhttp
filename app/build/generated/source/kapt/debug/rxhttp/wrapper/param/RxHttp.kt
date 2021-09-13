package rxhttp.wrapper.`param`

import com.example.httpsender.parser.ResponseParser
import kotlin.Any
import kotlin.Unit
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import rxhttp.IRxHttp
import rxhttp.onEachProgress
import rxhttp.toClass
import rxhttp.toParser
import rxhttp.wrapper.entity.Progress
import rxhttp.wrapper.entity.ProgressT
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
    R>.upload(coroutine: CoroutineScope, progress: suspend (Progress) -> Unit): R {
  param.setProgressCallback {
      coroutine.launch { progress(it) }
  }
  @Suppress("UNCHECKED_CAST")
  return this as R
}

@ExperimentalCoroutinesApi
public inline fun <reified T : Any> RxHttpAbstractBodyParam<*, *>.toFlow(noinline progress: suspend
    (Progress) -> Unit) = 
  channelFlow {                                                      
      getParam().setProgressCallback { trySend(ProgressT<T>(it)) }           
      toClass<T>().await().also { trySend(ProgressT<T>(it)) }           
  }.onEachProgress(progress)                                                      

public inline fun <reified T : Any> IRxHttp.toFlow() = flow<T> { emit(toClass<T>().await()) }       
                                          

public inline fun <reified T : Any> IRxHttp.toResponse() = toParser(object: ResponseParser<T>() {})

public inline fun <reified T : Any> IRxHttp.toFlowResponse() = 
  flow { emit(toResponse<T>().await()) }                                              

@ExperimentalCoroutinesApi
public inline fun <reified T : Any> RxHttpAbstractBodyParam<*, *>.toFlowResponse(noinline
    progress: suspend (Progress) -> Unit) = 
  channelFlow {                                                      
      getParam().setProgressCallback { trySend(ProgressT<T>(it)) }           
      toResponse<T>().await().also { trySend(ProgressT<T>(it)) }           
  }.onEachProgress(progress)                                                                        
                                          
