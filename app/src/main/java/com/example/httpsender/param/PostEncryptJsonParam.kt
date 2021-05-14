package com.example.httpsender.param


import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody
import rxhttp.wrapper.annotation.Param
import rxhttp.wrapper.param.JsonParam
import rxhttp.wrapper.param.Method
import rxhttp.wrapper.utils.GsonUtil

/**
 * User: ljx
 * Date: 2019/1/25
 * Time: 19:32
 */
@Param(methodName = "postEncryptJson")
class PostEncryptJsonParam(url: String) : JsonParam(url, Method.POST) {

    private var MEDIA_TYPE_JSON: MediaType = "application/json; charset=utf-8".toMediaType()
    /**
     * @return 根据自己的业务需求返回对应的RequestBody
     */
    override fun getRequestBody(): RequestBody {
        //我们要发送Post请求，参数以加密后的json形式发出
        //第一步，将参数转换为Json字符串
        val json = if (bodyParam == null) "" else GsonUtil.toJson(bodyParam)
        //第二步，加密
        val encryptByte = encrypt(json, "RxHttp")
        //第三部，创建RequestBody并返回
        return RequestBody.create(MEDIA_TYPE_JSON, encryptByte!!)
    }

    /**
     * @param content  要加密的字符串
     * @param password 密码
     * @return 加密后的字节数组
     */
    private fun encrypt(content: String, password: String): ByteArray? {
        //加码代码省略
        return null
    }
}
