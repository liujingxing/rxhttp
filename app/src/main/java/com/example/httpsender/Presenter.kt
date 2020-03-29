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
import rxhttp.retry
import rxhttp.timeout
import rxhttp.wrapper.param.RxHttp
import rxhttp.wrapper.param.awaitResponse
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

    fun testRetry() = RxLifeScope().launch {
        val pageList = RxHttp.get("/article/list/0/json")
            .timeout(100)
            .retry(2, 1000) {
                it is TimeoutCancellationException
            }
            .awaitResponse<PageList<Article>>()
    }
}