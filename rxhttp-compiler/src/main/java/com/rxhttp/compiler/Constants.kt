package com.rxhttp.compiler

import com.squareup.javapoet.ClassName

/**
 * User: ljx
 * Date: 2021/11/23
 * Time: 16:10
 */

const val RxHttp = "RxHttp"

const val defaultPackageName = "rxhttp.wrapper.param"
const val rxhttp_rxjava = "rxhttp_rxjava"
const val rxhttp_package = "rxhttp_package"
const val rxhttp_incremental = "rxhttp_incremental"
const val rxhttp_debug = "rxhttp_debug"
const val rxhttp_android_platform = "rxhttp_android_platform"

val rxhttpKClass = com.squareup.kotlinpoet.ClassName(rxHttpPackage, RxHttp)
val rxhttpClass: ClassName = ClassName.get(rxHttpPackage, RxHttp)
