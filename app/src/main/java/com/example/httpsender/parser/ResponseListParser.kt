package com.example.httpsender.parser

import com.example.httpsender.entity.Response
import rxhttp.wrapper.annotation.Parser
import rxhttp.wrapper.entity.ParameterizedTypeImpl
import rxhttp.wrapper.exception.ParseException
import rxhttp.wrapper.parse.AbstractParser
import java.io.IOException
import java.lang.reflect.Type

/**
 * 输入T，输出List<T>，并对code统一判断
 * User: ljx
 * Date: 2018/10/23
 * Time: 13:49
</T> */
@Parser(name = "ResponseList")
open class ResponseListParser<T> : AbstractParser<MutableList<T>> {
    /**
     * 此构造方法适用于任意Class对象，但更多用于带泛型的Class对象，如：List<Student>
     *
     * 用法:
     * Java: .asParser(new ResponseListParser<List<Student>>(){})
     * Kotlin: .asParser(object : ResponseListParser<List<Student>>() {})
     *
     * 注：此构造方法一定要用protected关键字修饰，否则调用此构造方法将拿不到泛型类型
    </Student></Student></Student> */
    protected constructor() : super()

    /**
     * 此构造方法仅适用于解析不带泛型的Class对象，如: Student.class
     *
     * 用法
     * Java: .asParser(new ResponseListParser<>(Student.class))   或者  .asResponseList(Student.class)
     * Kotlin: .asParser(ResponseListParser(Student::class.java)) 或者  .asResponseList(Student::class.java)
     */
    constructor(type: Class<T>) : super(type)

    @Throws(IOException::class)
    override fun onParse(response: okhttp3.Response): MutableList<T> {
        val type: Type = ParameterizedTypeImpl.get(Response::class.java, MutableList::class.java, mType) //获取泛型类型
        val data: Response<MutableList<T>> = convert(response, type)
        val list = data.data //获取data字段
        if (data.code != 0 || list == null) {  //code不等于0，说明数据不正确，抛出异常
            throw ParseException(data.code.toString(), data.msg, response)
        }
        return list
    }
}