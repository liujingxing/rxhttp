package com.example.httpsender.parser


import com.example.httpsender.entity.Response
import rxhttp.wrapper.annotation.Parser
import rxhttp.wrapper.entity.ParameterizedTypeImpl
import rxhttp.wrapper.exception.ParseException
import rxhttp.wrapper.parse.AbstractParser
import rxhttp.wrapper.utils.GsonUtil
import java.io.IOException
import java.lang.reflect.Type

/**
 * Response<List></List><T>> 数据解析器,解析完成对Response对象做判断,如果ok,返回数据 List<T>
 * User: ljx
 * Date: 2018/10/23
 * Time: 13:49
</T></T> */
@Parser(name = "ResponseList")
open class ResponseListParser<T> : AbstractParser<List<T>> {

    protected constructor() : super()

    constructor(type: Type) : super(type)

    @Throws(IOException::class)
    override fun onParse(response: okhttp3.Response): List<T> {
        val content = getResult(response) //从Response中取出Http执行结果
        val type = ParameterizedTypeImpl.get(Response::class.java, List::class.java, mType) //获取泛型类型
        val data = GsonUtil.fromJson<Response<List<T>>>(content, type)
        //跟服务端协议好，code等于100，才代表数据正确,否则，抛出异常
        if (data.code != 100) {
            throw ParseException(data.code.toString(), data.msg, response)
        }
        return data.data
            ?: //为空，有可能是参数错误或者其他原因，导致服务器不能正确给我们data字段数据
            throw ParseException(data.code.toString(), data.msg, response)
    }
}
