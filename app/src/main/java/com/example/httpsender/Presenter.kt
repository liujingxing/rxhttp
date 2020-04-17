package com.example.httpsender

import android.util.Log
import androidx.lifecycle.LifecycleOwner
import com.example.httpsender.entity.Article
import com.example.httpsender.entity.PageList
import com.rxjava.rxlife.BaseScope
import com.rxjava.rxlife.RxLifeScope
import com.rxjava.rxlife.life
import io.reactivex.Observable
import kotlinx.coroutines.TimeoutCancellationException
import rxhttp.*
import rxhttp.wrapper.cahce.CacheMode
import rxhttp.wrapper.param.RxHttp
import rxhttp.wrapper.param.toResponse
import java.util.concurrent.TimeUnit

/**
 * User: ljx
 * Date: 2019-05-26
 * Time: 15:20
 */
class Presenter(owner: LifecycleOwner) : BaseScope(owner) {

    fun test() {
        Observable.interval(1, 1, TimeUnit.SECONDS)
            .life(this) //这里的this 为Scope接口对象
            .subscribe { Log.e("LJX", "accept aLong=$it") }
    }

    fun testRetry() = RxLifeScope().launch({
        val pageList = RxHttp.get("/article/list/0/json")
            .setCacheMode(CacheMode.ONLY_NETWORK)
            .toResponse<PageList<Article>>()
            .delay(100)
            .startDelay(100)
            .onErrorReturnItem(PageList())
            .timeout(1000)
            .retry(2, 1000) {
                it is TimeoutCancellationException
            }
            .async()
            .tryAwait()

        Log.e("RxHttp", "pageList=$pageList")
    }, {
        Log.e("RxHttp", "it=$it")
    })
}