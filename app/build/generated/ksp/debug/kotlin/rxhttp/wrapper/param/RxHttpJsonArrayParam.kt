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
class RxHttpJsonArrayParam(param: JsonArrayParam) : RxHttpAbstractBodyParam<JsonArrayParam, RxHttpJsonArrayParam>(param) {

    @JvmOverloads
    fun add(key: String, value: Any?, isAdd: Boolean = true): RxHttpJsonArrayParam {
        if (isAdd) param.add(key, value)
        return this
    }

    fun addAll(map: Map<String, *>): RxHttpJsonArrayParam {
        param.addAll(map)
        return this
    }

    fun add(any: Any): RxHttpJsonArrayParam {
        param.add(any)
        return this
    }

    fun addAll(list: List<*>): RxHttpJsonArrayParam {
        param.addAll(list)
        return this
    }

    /**
     * 添加多个对象，将字符串转JsonElement对象,并根据不同类型,执行不同操作,可输入任意非空字符串
     */
    fun addAll(jsonElement: String): RxHttpJsonArrayParam {
        param.addAll(jsonElement)
        return this
    }

    fun addAll(jsonArray: JsonArray): RxHttpJsonArrayParam {
        param.addAll(jsonArray)
        return this
    }

    /**
     * 将Json对象里面的key-value逐一取出，添加到Json数组中，成为单独的对象
     */
    fun addAll(jsonObject: JsonObject): RxHttpJsonArrayParam {
        param.addAll(jsonObject)
        return this
    }

    fun addJsonElement(jsonElement: String): RxHttpJsonArrayParam {
        param.addJsonElement(jsonElement)
        return this
    }

    /**
     * 添加一个JsonElement对象(Json对象、json数组等)
     */
    fun addJsonElement(key: String, jsonElement: String): RxHttpJsonArrayParam {
        param.addJsonElement(key, jsonElement)
        return this
    }
}
