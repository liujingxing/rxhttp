package com.example.httpsender.parser.kotlin


import com.example.httpsender.entity.PageList
import com.example.httpsender.entity.Response
import rxhttp.wrapper.entity.ParameterizedTypeImpl
import rxhttp.wrapper.exception.ParseException
import rxhttp.wrapper.parse.AbstractParser
import java.io.IOException

/**
 * Response<PageList<T>> 数据解析器,解析完成对Response对象做判断,如果ok,返回数据 PageList<T>
 * User: ljx
 * Date: 2018/10/23
 * Time: 13:49
 */
//@Parser(name = "ResponsePageList")
class ResponsePageListParser<T> : AbstractParser<PageList<T>> {

    protected constructor() : super()

    constructor(type: Class<T>) : super(type)

    @Throws(IOException::class)
    override fun onParse(response: okhttp3.Response): PageList<T> {
        val type = ParameterizedTypeImpl.get(Response::class.java, PageList::class.java, mType) //获取泛型类型
        val data = convert<Response<PageList<T>>>(response, type)
        val pageList = data.data //获取data字段
        if (data.code != 0 || pageList == null) {  //code不等于0，说明数据不正确，抛出异常
            throw ParseException(data.code.toString(), data.msg, response)
        }
        return pageList
    }
}
