package com.rxhttp.compiler

import com.rxhttp.compiler.ksp.parameterizedBy
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

val rxhttpKClassName = com.squareup.kotlinpoet.ClassName(rxHttpPackage, RXHttp)
val paramKClassName = com.squareup.kotlinpoet.ClassName("rxhttp.wrapper.param", "Param")
val kP = com.squareup.kotlinpoet.TypeVariableName("P", paramKClassName.parameterizedBy("P"))      //泛型P
val kR = com.squareup.kotlinpoet.TypeVariableName("R", rxhttpKClassName.parameterizedBy("P","R"))     //泛型R


val rxhttpClassName: ClassName = ClassName.get(rxHttpPackage, RXHttp)
val paramClassName: ClassName = ClassName.get("rxhttp.wrapper.param", "Param")
val p: TypeVariableName = TypeVariableName.get("P", paramClassName)      //泛型P
val r: TypeVariableName = TypeVariableName.get("R", rxhttpClassName)     //泛型R