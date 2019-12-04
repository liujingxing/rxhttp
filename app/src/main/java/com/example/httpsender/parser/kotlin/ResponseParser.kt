package com.example.httpsender.parser.kotlin


import com.example.httpsender.entity.Response
import rxhttp.wrapper.entity.ParameterizedTypeImpl
import rxhttp.wrapper.exception.ParseException
import rxhttp.wrapper.parse.AbstractParser
import java.io.IOException

/**
 * Response<T> 数据解析器,解析完成对Response对象做判断,如果ok,返回数据 T
 * User: ljx
 * Date: 2018/10/23
 * Time: 13:49
 */
//@Parser(name = "Response")
class ResponseParser<T> : AbstractParser<T> {

    protected constructor() : super()

    constructor(type: Class<T>) : super(type)

    @Throws(IOException::class)
    override fun onParse(response: okhttp3.Response): T {
        val type = ParameterizedTypeImpl.get(Response::class.java, mType) //获取泛型类型
        val data = convert<Response<T>>(response, type)
        var t: T? = data.data //获取data字段
        if (t == null && mType === String::class.java) {
            /*
             * 考虑到有些时候服务端会返回：{"errorCode":0,"errorMsg":"关注成功"}  类似没有data的数据
             * 此时code正确，但是data字段为空，直接返回data的话，会报空指针错误，
             * 所以，判断泛型为String类型时，重新赋值，并确保赋值不为null
             */
            t = data.msg as T
        }
        if (data.code != 0 || t == null) {//code不等于0，说明数据不正确，抛出异常
            throw ParseException(data.code.toString(), data.msg, response)
        }
        return t
    }
}
