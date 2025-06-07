package com.rxhttp.compiler

import com.rxhttp.compiler.common.versionCompare

/**
 * RxJava 版本管理
 * User: ljx
 * Date: 2020/4/12
 * Time: 15:33
 */
private var rxJavaVersion: String? = null

private val rxJavaClassList = LinkedHashMap<String, String>()

fun getClassPath(simpleName: String): String = rxJavaClassList[simpleName] + ".$simpleName"

fun isDependenceRxJava() = rxJavaVersion != null

fun initRxJavaVersion(version: String?) {
    val realVersion = when {
        version.equals("RxJava2", true) -> "2.0.0"
        version.equals("RxJava3", true) -> "3.0.0"
        else -> version
    } ?: return
    rxJavaVersion = realVersion
    if (realVersion.versionCompare("3.0.0") >= 0) {
        rxJavaClassList["Scheduler"] = "io.reactivex.rxjava3.core"
        rxJavaClassList["Observable"] = "io.reactivex.rxjava3.core"
        rxJavaClassList["Consumer"] = "io.reactivex.rxjava3.functions"
        rxJavaClassList["Schedulers"] = "io.reactivex.rxjava3.schedulers"
        rxJavaClassList["RxJavaPlugins"] = "io.reactivex.rxjava3.plugins"
        rxJavaClassList["Observer"] = "io.reactivex.rxjava3.core"
        rxJavaClassList["Exceptions"] = "io.reactivex.rxjava3.exceptions"
        rxJavaClassList["Disposable"] = "io.reactivex.rxjava3.disposables"
        rxJavaClassList["DisposableHelper"] = "io.reactivex.rxjava3.internal.disposables"
        rxJavaClassList["Disposable"] = "io.reactivex.rxjava3.disposables"
        rxJavaClassList["ObservableSource"] = "io.reactivex.rxjava3.core"
        rxJavaClassList["TrampolineScheduler"] = "io.reactivex.rxjava3.internal.schedulers"
        rxJavaClassList["AndroidSchedulers"] = "io.reactivex.rxjava3.android.schedulers"
    } else {
        rxJavaClassList["Scheduler"] = "io.reactivex"
        rxJavaClassList["Observable"] = "io.reactivex"
        rxJavaClassList["Consumer"] = "io.reactivex.functions"
        rxJavaClassList["Schedulers"] = "io.reactivex.schedulers"
        rxJavaClassList["RxJavaPlugins"] = "io.reactivex.plugins"
        rxJavaClassList["Observer"] = "io.reactivex"
        rxJavaClassList["Exceptions"] = "io.reactivex.exceptions"
        rxJavaClassList["Disposable"] = "io.reactivex.disposables"
        rxJavaClassList["DisposableHelper"] = "io.reactivex.internal.disposables"
        rxJavaClassList["Disposable"] = "io.reactivex.disposables"
        rxJavaClassList["ObservableSource"] = "io.reactivex"
        rxJavaClassList["TrampolineScheduler"] = "io.reactivex.internal.schedulers"
        rxJavaClassList["AndroidSchedulers"] = "io.reactivex.android.schedulers"
    }
}