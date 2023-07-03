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
fun List<ParameterSpec>.toParamNames(
    prefix: CharSequence = "",
    postfix: CharSequence = ""
): String = joinToString(", ", prefix, postfix) {
    if (it.isVararg()) "*${it.name}" else it.name
}

//获取泛型字符串 比如:<T> 、<K, V>等等
fun List<TypeVariableName>.getTypeVariableString(): String {
    val types = joinToString { it.name }
    return if (types.isEmpty()) "" else "<$types>"
}

//返回 javaTypeOf<T>, javaTypeOf<K>等
fun List<TypeVariableName>.getTypeOfString(): String =
    joinToString { "javaTypeOf<${it.name}>()" }


fun <T> Iterable<T>.joinToStringIndexed(
    separator: CharSequence = ", ",
    prefix: CharSequence = "",
    postfix: CharSequence = "",
    limit: Int = -1,
    truncated: CharSequence = "...",
    transform: ((Int, T) -> CharSequence)
): String {
    var index = 0
    return joinToString(separator, prefix, postfix, limit, truncated) {
        transform.invoke(index++, it)
    }
}