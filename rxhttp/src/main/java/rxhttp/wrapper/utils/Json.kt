@file:JvmName("JsonUtil")

package rxhttp.wrapper.utils

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
import java.util.*

/**
 * User: ljx
 * Date: 2020/8/1
 * Time: 17:27
 */
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

fun JsonElement.toAny(): Any? {
    return when (this) {
        is JsonObject -> toMap()
        is JsonArray -> toList()
        is JsonPrimitive -> toAny()
        else -> null
    }
}

fun JsonPrimitive.toAny(): Any {
    return when {
        isNumber -> asNumber.toAny()
        isBoolean -> asBoolean
        else -> asString
    }
}

fun Number.toAny(): Any {
    val d = toDouble()
    val i = d.toLong()
    return if (d == i.toDouble() && i.toString().length == toString().length) i else d
}