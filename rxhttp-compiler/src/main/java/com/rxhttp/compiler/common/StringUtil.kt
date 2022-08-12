package com.rxhttp.compiler.common

import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.TypeVariableName

/**
 * User: ljx
 * Date: 2022/8/10
 * Time: 11:28
 */
//返回参数名列表, 多个参数用逗号隔开, 如： a, b, c
fun getParamsName(parameterSpecs: List<ParameterSpec>): String {
    val paramsName = StringBuilder()
    parameterSpecs.forEachIndexed { index, parameterSpec ->
        if (index > 0) paramsName.append(", ")
        if (KModifier.VARARG in parameterSpec.modifiers) paramsName.append("*")
        paramsName.append(parameterSpec.name)
    }
    return paramsName.toString()
}

//获取泛型字符串 比如:<T> 、<K, V>等等
fun getTypeVariableString(typeVariableNames: List<TypeVariableName>): String {
    val type = StringBuilder()
    val size = typeVariableNames.size
    typeVariableNames.forEachIndexed { i, typeVariableName ->
        if (i == 0) type.append("<")
        type.append(typeVariableName.name)
        type.append(if (i < size - 1) ", " else ">")
    }
    return type.toString()
}

//返回 javaTypeOf<T>, javaTypeOf<K>等
fun getTypeOfString(typeVariableNames: List<TypeVariableName>): String {
    val type = StringBuilder()
    typeVariableNames.forEachIndexed { i, typeVariableName ->
        if (i > 0) type.append(", ")
        type.append("javaTypeOf<${typeVariableName.name}>()")
    }
    return type.toString()
}