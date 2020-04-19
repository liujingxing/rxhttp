package com.rxhttp.compiler

import com.squareup.javapoet.ClassName

/**
 * RxJava 版本管理
 * User: ljx
 * Date: 2020/4/12
 * Time: 15:33
 */
var rxJavaVersion: String? = null

private val rxJavaClassList = LinkedHashMap<String, String>()

fun getClassName(simpleName: String): ClassName =
    ClassName.get(rxJavaClassList[simpleName], simpleName)

fun getClassPath(simpleName: String) = rxJavaClassList[simpleName] + ".$simpleName"

fun getKClassName(simpleName: String) =
    com.squareup.kotlinpoet.ClassName(rxJavaClassList[simpleName]!!, simpleName)

fun isDependenceRxJava() = rxJavaVersion == "rxjava2" || rxJavaVersion == "rxjava3"


fun initRxJavaVersion(version: String?) {
    rxJavaVersion = version

    if (version == "rxjava2") {
        rxJavaClassList["Scheduler"] = "io.reactivex"
        rxJavaClassList["Observable"] = "io.reactivex"
        rxJavaClassList["Consumer"] = "io.reactivex.functions"
        rxJavaClassList["Schedulers"] = "io.reactivex.schedulers"
        rxJavaClassList["RxJavaPlugins"] = "io.reactivex.plugins"
        rxJavaClassList["Observer"] = "io.reactivex"
        rxJavaClassList["Exceptions"] = "io.reactivex.exceptions"
        rxJavaClassList["DeferredScalarDisposable"] = "io.reactivex.internal.observers"
        rxJavaClassList["ObservableEmitter"] = "io.reactivex"
        rxJavaClassList["Disposable"] = "io.reactivex.disposables"
        rxJavaClassList["Cancellable"] = "io.reactivex.functions"
        rxJavaClassList["CancellableDisposable"] = "io.reactivex.internal.disposables"
        rxJavaClassList["DisposableHelper"] = "io.reactivex.internal.disposables"
        rxJavaClassList["SimpleQueue"] = "io.reactivex.internal.fuseable"
        rxJavaClassList["SpscLinkedArrayQueue"] = "io.reactivex.internal.queue"
        rxJavaClassList["AtomicThrowable"] = "io.reactivex.internal.util"
        rxJavaClassList["ExceptionHelper"] = "io.reactivex.internal.util"
        rxJavaClassList["Disposable"] = "io.reactivex.disposables"

    } else if (version == "rxjava3") {
        rxJavaClassList["Scheduler"] = "io.reactivex.rxjava3.core"
        rxJavaClassList["Observable"] = "io.reactivex.rxjava3.core"
        rxJavaClassList["Consumer"] = "io.reactivex.rxjava3.functions"
        rxJavaClassList["Schedulers"] = "io.reactivex.rxjava3.schedulers"
        rxJavaClassList["RxJavaPlugins"] = "io.reactivex.rxjava3.plugins"
        rxJavaClassList["Observer"] = "io.reactivex.rxjava3.core"
        rxJavaClassList["Exceptions"] = "io.reactivex.rxjava3.exceptions"
        rxJavaClassList["DeferredScalarDisposable"] = "io.reactivex.rxjava3.internal.observers"
        rxJavaClassList["ObservableEmitter"] = "io.reactivex.rxjava3.core"
        rxJavaClassList["Disposable"] = "io.reactivex.rxjava3.disposables"
        rxJavaClassList["Cancellable"] = "io.reactivex.rxjava3.functions"
        rxJavaClassList["CancellableDisposable"] = "io.reactivex.rxjava3.internal.disposables"
        rxJavaClassList["DisposableHelper"] = "io.reactivex.rxjava3.internal.disposables"
        rxJavaClassList["SimpleQueue"] = "io.reactivex.rxjava3.internal.fuseable"
        rxJavaClassList["SpscLinkedArrayQueue"] = "io.reactivex.rxjava3.internal.queue"
        rxJavaClassList["AtomicThrowable"] = "io.reactivex.rxjava3.internal.util"
        rxJavaClassList["ExceptionHelper"] = "io.reactivex.rxjava3.internal.util"
        rxJavaClassList["Disposable"] = "io.reactivex.rxjava3.disposables"
    }

}