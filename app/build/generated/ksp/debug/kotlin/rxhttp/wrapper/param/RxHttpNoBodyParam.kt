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
    fun add(key: String, value: Any?, add: Boolean = true): RxHttpNoBodyParam = apply {
        if (add) addQuery(key, value)
    }

    fun addAll(map: Map<String, *>): RxHttpNoBodyParam = addAllQuery(map)

    fun addEncoded(key: String, value: Any?): RxHttpNoBodyParam = addEncodedQuery(key, value)

    fun addAllEncoded(map: Map<String, *>): RxHttpNoBodyParam = addAllEncodedQuery(map)
}
