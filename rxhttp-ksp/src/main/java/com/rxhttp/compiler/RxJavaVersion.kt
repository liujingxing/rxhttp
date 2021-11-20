package com.rxhttp.compiler

import com.squareup.javapoet.ClassName

/**
 * RxJava 版本管理
 * User: ljx
 * Date: 2020/4/12
 * Time: 15:33
 */
private var rxJavaVersion: String? = null

private val rxJavaClassList = LinkedHashMap<String, String>()

fun getClassName(simpleName: String): ClassName =
    ClassName.get(rxJavaClassList[simpleName], simpleName)

fun getClassPath(simpleName: String) = rxJavaClassList[simpleName] + ".$simpleName"

fun getKClassName(simpleName: String) =
    com.squareup.kotlinpoet.ClassName(rxJavaClassList[simpleName]!!, simpleName)

fun isDependenceRxJava() = rxJavaVersion != null


fun initRxJavaVersion(version: String?) {
    val realVersion = when {
        version.equals("RxJava2", true) -> {
            "2.0.0"
        }
        version.equals("RxJava3", true) -> {
            "3.0.0"
        }
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
        rxJavaClassList["SpscArrayQueue"] = if (realVersion.versionCompare("3.1.1") >= 0) {
            "io.reactivex.rxjava3.operators"
        } else {
            "io.reactivex.rxjava3.internal.queue"
        }
        rxJavaClassList["Disposable"] = "io.reactivex.rxjava3.disposables"
        rxJavaClassList["ObservableSource"] = "io.reactivex.rxjava3.core"
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
        rxJavaClassList["SpscArrayQueue"] = "io.reactivex.internal.queue"
        rxJavaClassList["Disposable"] = "io.reactivex.disposables"
        rxJavaClassList["ObservableSource"] = "io.reactivex"
    }
}

private fun String.versionCompare(version: String): Int {
    val versionArr1 = split(".")
    val versionArr2 = version.split(".")
    val minLen = versionArr1.size.coerceAtMost(versionArr2.size)
    var diff = 0
    for (i in 0 until minLen) {
        val v1 = versionArr1[i]
        val v2 = versionArr2[i]
        diff = v1.length - v2.length
        if (diff == 0) {
            diff = v1.compareTo(v2)
        }
        if (diff != 0) {
            break
        }
    }
    return if (diff != 0) diff else versionArr1.size - versionArr2.size
}