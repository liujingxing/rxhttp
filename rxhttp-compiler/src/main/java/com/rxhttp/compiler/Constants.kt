package com.rxhttp.compiler

import com.squareup.javapoet.ClassName
import com.squareup.javapoet.TypeVariableName

/**
 * User: ljx
 * Date: 2021/11/23
 * Time: 16:10
 */

const val RXHttp = "RxHttp"

const val defaultPackageName = "rxhttp.wrapper.param"
const val rxhttp_rxjava = "rxhttp_rxjava"
const val rxhttp_package = "rxhttp_package"
const val rxhttp_incremental = "rxhttp_incremental"
const val rxhttp_debug = "rxhttp_debug"

val rxhttpClassName: ClassName = ClassName.get(rxHttpPackage, RXHttp)
val rxhttpKClassName = com.squareup.kotlinpoet.ClassName(rxHttpPackage, RXHttp)
val paramClassName: ClassName = ClassName.get("rxhttp.wrapper.param", "Param")
val p: TypeVariableName = TypeVariableName.get("P", paramClassName)      //泛型P
val r: TypeVariableName = TypeVariableName.get("R", rxhttpClassName)     //泛型R