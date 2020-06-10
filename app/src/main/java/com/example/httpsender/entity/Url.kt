package com.example.httpsender.entity

import rxhttp.wrapper.annotation.DefaultDomain
import rxhttp.wrapper.annotation.Domain

/**
 * User: ljx
 * Date: 2020/2/27
 * Time: 23:55
 */
object Url {

    @JvmField
    @Domain(name = "Update", className = "Simple")
    var update = "http://update.9158.com"

    @JvmField
    @DefaultDomain //设置为默认域名
    var baseUrl = "https://www.wanandroid.com/"
}