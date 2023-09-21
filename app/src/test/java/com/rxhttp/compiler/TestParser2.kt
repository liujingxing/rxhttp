package com.rxhttp.compiler

import com.example.httpsender.entity.Response
import com.example.httpsender.entity.User
import rxhttp.wrapper.annotation.Parser
import rxhttp.wrapper.callback.Consumer
import java.lang.reflect.Type

/**
 * User: ljx
 * Date: 2023/8/28
 * Time: 11:12
 */
@Parser(name = "test")
class TestParser2<A, B, C>: TestParser1<A, Response<B?>?, C>,
    rxhttp.wrapper.parse.Parser<Response<B?>?> {

    protected constructor() : super()

    constructor(typeT: Type, typeF: Type, typeS: Type)

    override fun onParse(response: okhttp3.Response): Response<B?> {
        TODO("Not yet implemented")
//        return null
//        return super.onParse(response)
    }

//    override fun accept(t: User) {
//        TODO("Not yet implemented")
//    }

}