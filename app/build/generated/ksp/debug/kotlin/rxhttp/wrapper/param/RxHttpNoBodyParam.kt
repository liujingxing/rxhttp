package rxhttp.wrapper.param

import rxhttp.wrapper.param.NoBodyParam

/**
 * Github
 * https://github.com/liujingxing/rxhttp
 * https://github.com/liujingxing/rxlife
 * https://github.com/liujingxing/rxhttp/wiki/FAQ
 * https://github.com/liujingxing/rxhttp/wiki/更新日志
 */
open class RxHttpNoBodyParam(param: NoBodyParam) : RxHttp<NoBodyParam, RxHttpNoBodyParam>(param) {

    @JvmOverloads
    fun add(key: String, value: Any?, isAdd: Boolean = true): RxHttpNoBodyParam {
        if (isAdd) addQuery(key, value)
        return this
    }

    fun addAll(map: Map<String, *>) = addAllQuery(map)

    fun addEncoded(key: String, value: Any?) = addEncodedQuery(key, value)

    fun addAllEncoded(map: Map<String, *>) = addAllEncodedQuery(map)
}
