package com.rxhttp.compiler

import rxhttp.wrapper.parse.TypeParser

/**
 * User: ljx
 * Date: 2023/8/28
 * Time: 11:12
 */
open class TestParser1<T, F, S> : TypeParser<F> {

    protected constructor() : super()
    override fun onParse(response: okhttp3.Response): F {
        TODO("Not yet implemented")
    }
}