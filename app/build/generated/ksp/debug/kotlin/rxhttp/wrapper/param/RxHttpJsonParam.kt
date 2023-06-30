package rxhttp.wrapper.param

import com.google.gson.JsonObject

import rxhttp.wrapper.param.JsonParam
/**
 * Github
 * https://github.com/liujingxing/rxhttp
 * https://github.com/liujingxing/rxlife
 * https://github.com/liujingxing/rxhttp/wiki/FAQ
 * https://github.com/liujingxing/rxhttp/wiki/更新日志
 */
open class RxHttpJsonParam(param: JsonParam) : RxHttpAbstractBodyParam<JsonParam, RxHttpJsonParam>(param) {

    @JvmOverloads
    fun add(key: String, value: Any?, add: Boolean = true) = apply {
        if (add) param.add(key, value)
    }

    fun addAll(map: Map<String, *>) = apply { param.addAll(map) }

    /**
     * 将Json对象里面的key-value逐一取出，添加到另一个Json对象中，
     * 输入非Json对象将抛出[IllegalStateException]异常
     */
    fun addAll(jsonObject: String) = apply { param.addAll(jsonObject) }

    /**
     * 将Json对象里面的key-value逐一取出，添加到另一个Json对象中
     */
    fun addAll(jsonObject: JsonObject) = apply { param.addAll(jsonObject) }

    /**
     * 添加一个JsonElement对象(Json对象、json数组等)
     */
    fun addJsonElement(key: String, jsonElement: String) = apply {
        param.addJsonElement(key, jsonElement)
    }
}
