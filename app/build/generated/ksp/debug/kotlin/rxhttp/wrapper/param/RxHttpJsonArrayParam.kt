package rxhttp.wrapper.param

import com.google.gson.JsonArray
import com.google.gson.JsonObject

import rxhttp.wrapper.param.JsonArrayParam

/**
 * Github
 * https://github.com/liujingxing/rxhttp
 * https://github.com/liujingxing/rxlife
 * https://github.com/liujingxing/rxhttp/wiki/FAQ
 * https://github.com/liujingxing/rxhttp/wiki/更新日志
 */
open class RxHttpJsonArrayParam(param: JsonArrayParam) : RxHttpAbstractBodyParam<JsonArrayParam, RxHttpJsonArrayParam>(param) {

    @JvmOverloads
    fun add(key: String, value: Any?, isAdd: Boolean = true) = apply {
        if (isAdd) param.add(key, value)
    }

    fun addAll(map: Map<String, *>) = apply { param.addAll(map) }

    fun add(any: Any) = apply { param.add(any) }

    fun addAll(list: List<*>) = apply {  param.addAll(list) }

    /**
     * 添加多个对象，将字符串转JsonElement对象,并根据不同类型,执行不同操作,可输入任意非空字符串
     */
    fun addAll(jsonElement: String) = apply { param.addAll(jsonElement) }

    fun addAll(jsonArray: JsonArray) = apply { param.addAll(jsonArray) }

    /**
     * 将Json对象里面的key-value逐一取出，添加到Json数组中，成为单独的对象
     */
    fun addAll(jsonObject: JsonObject) = apply { param.addAll(jsonObject) }

    fun addJsonElement(jsonElement: String) = apply { param.addJsonElement(jsonElement) }

    /**
     * 添加一个JsonElement对象(Json对象、json数组等)
     */
    fun addJsonElement(key: String, jsonElement: String) = apply {
        param.addJsonElement(key, jsonElement)
    }
}
