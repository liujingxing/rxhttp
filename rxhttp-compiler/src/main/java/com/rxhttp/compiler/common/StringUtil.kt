package com.rxhttp.compiler.common

import com.rxhttp.compiler.ksp.isVararg
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.TypeVariableName

/**
 * User: ljx
 * Date: 2022/8/10
 * Time: 11:28
 */
//返回参数名列表, 多个参数用逗号隔开, 如： a, b, c
fun List<ParameterSpec>.toParamNames(): String {
    val paramNames = StringBuilder()
    forEachIndexed { index, parameterSpec ->
        if (index > 0) paramNames.append(", ")
        if (parameterSpec.isVararg()) paramNames.append("*")
        paramNames.append(parameterSpec.name)
    }
    return paramNames.toString()
}

//获取泛型字符串 比如:<T> 、<K, V>等等
fun List<TypeVariableName>.getTypeVariableString(): String {
    val type = StringBuilder()
    forEachIndexed { i, typeVariableName ->
        if (i > 0) type.append(", ")
        type.append(typeVariableName.name)
    }
    return if (type.isEmpty()) "" else "<$type>"
}

//返回 javaTypeOf<T>, javaTypeOf<K>等
fun List<TypeVariableName>.getTypeOfString(): String {
    val type = StringBuilder()
    forEachIndexed { i, typeVariableName ->
        if (i > 0) type.append(", ")
        type.append("javaTypeOf<${typeVariableName.name}>()")
    }
    return type.toString()
}