package com.example.httpsender

import android.app.Application
import android.util.Log
import androidx.lifecycle.viewModelScope
import com.example.httpsender.entity.Article
import com.example.httpsender.entity.PageList
import com.rxjava.rxlife.ScopeViewModel
import com.rxjava.rxlife.lifeOnMain
import io.reactivex.rxjava3.core.Observable
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.launch
import rxhttp.awaitResult
import rxhttp.retry
import rxhttp.timeout
import rxhttp.wrapper.param.RxHttp
import rxhttp.wrapper.param.toResponse
import java.util.concurrent.TimeUnit

/**
 * User: ljx
 * Date: 2019-05-31
 * Time: 21:50
 */
class MyViewModel(application: Application) : ScopeViewModel(application) {

    fun startInterval() {
        Observable.interval(1, 1, TimeUnit.SECONDS)
            .lifeOnMain(this)
            .subscribe { Log.e("LJX", "MyViewModel aLong=$it") }
    }

    fun testRetry() = viewModelScope.launch {
        RxHttp.get("/article/list/0/json")
            .toResponse<PageList<Article>>()
            .timeout(100)
            .retry(2, 1000) {
                it is TimeoutCancellationException
            }
            .awaitResult {
                Log.e("LJX", "pageList=$it")
            }
            .onFailure {
                Log.e("LJX", "it=$it")
            }

    }
}