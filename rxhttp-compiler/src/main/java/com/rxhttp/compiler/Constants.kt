package com.rxhttp.compiler

import com.squareup.javapoet.ArrayTypeName
import com.squareup.javapoet.ClassName
import com.squareup.javapoet.TypeName
import com.squareup.kotlinpoet.ARRAY
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy

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

val J_TYPE: TypeName = ClassName.bestGuess("java.lang.reflect.Type")
val J_ARRAY_TYPE: TypeName = ArrayTypeName.of(J_TYPE)

val K_TYPE = com.squareup.kotlinpoet.ClassName("java.lang.reflect", "Type")
val K_ARRAY_TYPE = ARRAY.parameterizedBy(K_TYPE)
