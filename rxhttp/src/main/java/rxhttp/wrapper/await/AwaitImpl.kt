package rxhttp.wrapper.await

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import okhttp3.*
import rxhttp.HttpSender
import rxhttp.IAwait
import rxhttp.RxHttpPlugins
import rxhttp.wrapper.annotations.Nullable
import rxhttp.wrapper.cahce.CacheMode
import rxhttp.wrapper.cahce.CacheStrategy
import rxhttp.wrapper.cahce.InternalCache
import rxhttp.wrapper.exception.CacheReadFailedException
import rxhttp.wrapper.parse.Parser
import rxhttp.wrapper.utils.LogUtil
import java.io.IOException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * IAwait接口真正实现类
 * User: ljx
 * Date: 2020/3/21
 * Time: 17:06
 */
internal class AwaitImpl<T>(
    private val parser: Parser<T>,
    private val request: Request,
    private val client: OkHttpClient,
    private val cacheStrategy: CacheStrategy,
    private val cache: InternalCache = RxHttpPlugins.getCache()
) : IAwait<T> {

    override suspend fun await(): T {
        val cacheT = beforeReadCache()
        if (cacheT != null) return cacheT
        val newCall = HttpSender.newCall(client, request)
        return await(newCall)
    }

    private suspend fun beforeReadCache(): T? {
        return if (cacheModeIs(CacheMode.ONLY_CACHE, CacheMode.READ_CACHE_FAILED_REQUEST_NETWORK)) {
            withContext(Dispatchers.IO) {
                //读取缓存
                val cacheResponse = getCacheResponse(request, cacheStrategy.cacheValidTime)
                if (cacheResponse == null) {
                    if (cacheModeIs(CacheMode.ONLY_CACHE)) throw CacheReadFailedException("Cache read failed")
                    return@withContext null
                }
                parser.onParse(cacheResponse)
            }
        } else null
    }

    private fun cacheModeIs(vararg cacheModes: CacheMode): Boolean {
        val cacheMode = cacheStrategy.cacheMode
        cacheModes.forEach {
            if (it == cacheMode) return true
        }
        return false
    }

    @Nullable
    @Throws(IOException::class)
    private fun getCacheResponse(request: Request, validTime: Long): Response? {
        val cacheResponse = cache[request, cacheStrategy.cacheKey]
        if (cacheResponse != null) {
            val receivedTime = cacheResponse.receivedResponseAtMillis()
            return if (validTime != -1L && System.currentTimeMillis() - receivedTime > validTime) null else cacheResponse //缓存过期，返回null
        }
        return null
    }

    /**
     * 所有的awaitXxx方法,最终都会调用本方法
     */
    suspend fun await(call: Call): T {
        return suspendCancellableCoroutine { continuation ->
            continuation.invokeOnCancellation {
                call.cancel()  //当前线程同关闭协程时的线程 如：A线程关闭协程，这当前就在A线程调用
            }
            call.enqueue(object : Callback {

                override fun onResponse(call: Call, response: Response) {
                    val networkResponse = if (cacheStrategy.cacheMode != CacheMode.ONLY_NETWORK) {
                        //非ONLY_NETWORK模式下,请求成功，写入缓存
                        cache.put(response, cacheStrategy.cacheKey)
                    } else {
                        response
                    }
                    onSuccess(call, networkResponse)
                }

                override fun onFailure(call: Call, e: IOException) {
                    var networkResponse: Response? = null
                    if (cacheModeIs(CacheMode.REQUEST_NETWORK_FAILED_READ_CACHE)) {
                        //请求失败，读取缓存
                        networkResponse = getCacheResponse(request, cacheStrategy.cacheValidTime)
                    }
                    if (networkResponse == null) {
                        LogUtil.log(call.request().url().toString(), e)
                        continuation.resumeWithException(e)
                    } else {
                        onSuccess(call, networkResponse)
                    }
                }

                private fun onSuccess(call: Call, response: Response) {
                    try {
                        continuation.resume(parser.onParse(response))
                    } catch (e: Throwable) {
                        LogUtil.log(call.request().url().toString(), e)
                        continuation.resumeWithException(e)
                    }
                }
            })
        }
    }
}