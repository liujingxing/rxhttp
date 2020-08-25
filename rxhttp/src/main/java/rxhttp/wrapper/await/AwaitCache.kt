package rxhttp.wrapper.await

import okhttp3.Request
import okhttp3.Response
import rxhttp.IAwait
import rxhttp.IRxHttp
import rxhttp.RxHttpPlugins
import rxhttp.wrapper.OkHttpCompat
import rxhttp.wrapper.annotations.Nullable
import rxhttp.wrapper.cahce.CacheMode
import rxhttp.wrapper.cahce.CacheStrategy
import rxhttp.wrapper.cahce.InternalCache
import rxhttp.wrapper.exception.CacheReadFailedException
import java.io.IOException

/**
 * User: ljx
 * Date: 2020/3/21
 * Time: 17:06
 */
internal class AwaitCache(
    private val iAwait: AwaitOkResponse,
    private val iRxHttp: IRxHttp
) : IAwait<Response> {

    internal val request: Request by lazy { iAwait.originalCall.request() }
    private val cache: InternalCache by lazy { RxHttpPlugins.getCache() }  //缓存读取
    private val cacheStrategy: CacheStrategy by lazy { iRxHttp.getCacheStrategy() }  //缓存策略

    @Suppress("BlockingMethodInNonBlockingContext")
    override suspend fun await(): Response {
        val cacheResponse = beforeReadCache(request)
        if (cacheResponse != null) return cacheResponse  //缓存有效，直接返回
        try {
            //发起请求
            val response = iAwait.await()
            return if (!cacheModeIs(CacheMode.ONLY_NETWORK)) {
                //非ONLY_NETWORK模式下,请求成功，写入缓存
                cache.put(response, cacheStrategy.cacheKey)
            } else {
                response
            }
        } catch (e: Throwable) {
            var networkResponse: Response? = null
            if (cacheModeIs(CacheMode.REQUEST_NETWORK_FAILED_READ_CACHE)) {
                //请求失败，读取缓存
                networkResponse = getCacheResponse(request, cacheStrategy.cacheValidTime)
            }
            return networkResponse ?: throw e
        }
    }

    private fun beforeReadCache(request: Request): Response? {
        return if (cacheModeIs(CacheMode.ONLY_CACHE, CacheMode.READ_CACHE_FAILED_REQUEST_NETWORK)) {
            //读取缓存
            val cacheResponse = getCacheResponse(request, cacheStrategy.cacheValidTime)
            if (cacheResponse == null) {
                if (cacheModeIs(CacheMode.ONLY_CACHE)) throw CacheReadFailedException("Cache read failed")
                return null
            }
            cacheResponse
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
            val receivedTime = OkHttpCompat.receivedResponseAtMillis(cacheResponse)
            return if (validTime != -1L && System.currentTimeMillis() - receivedTime > validTime) null else cacheResponse //缓存过期，返回null
        }
        return null
    }
}