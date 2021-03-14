@file:JvmName("JsonUtil")

package rxhttp.wrapper.utils

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
import com.google.gson.internal.LazilyParsedNumber
import java.util.*

/**
 * User: ljx
 * Date: 2020/8/1
 * Time: 17:27
 */

fun JsonElement.toAny(): Any? {
    return when (this) {
        is JsonObject -> toMap()
        is JsonArray -> toList()
        is JsonPrimitive -> toAny()
        else -> null
    }
}

fun JsonObject.toMap(): Map<String, Any?> {
    val map: MutableMap<String, Any?> = LinkedHashMap()
    for ((key, value) in entrySet()) {
        map[key] = value.toAny()
    }
    return map
}

fun JsonArray.toList(): List<Any?> {
    val list: MutableList<Any?> = ArrayList()
    forEach { list.add(it.toAny()) }
    return list
}

fun JsonPrimitive.toAny(): Any {
    return when {
        isNumber -> asNumber.toNumber()
        isBoolean -> asBoolean
        else -> asString
    }
}

/**
 * 因fastJson、moshi等第三方数据解析工具正常情况下无法对LazilyParsedNumber类型数据，做出正确的序列化操作
 * 故对于正常数据，转为基本类型，对于超大整型、浮点型数据，转为BigInteger、BigDecimal (moshi 需要额外配置，才能对这两个数据类型正常序列化)
 */
fun Number.toNumber(): Number {
    return if (this is LazilyParsedNumber) {
        val number = toString()
        if (number.contains(".")) {
            val double = toDouble()
            if (double.toString() == number) {
                double
            } else {
                number.toBigDecimal()
            }
        } else {
            val long = toLong()
            if (long.toString() == number) {
                long
            } else {
                number.toBigInteger()
            }
        }
    } else this
}