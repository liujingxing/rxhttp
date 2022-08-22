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
    @DefaultDomain //设置为默认域名
    var baseUrl = "https://www.wanandroid.com/"

    const val UPLOAD_URL = "http://t.xinhuo.com/index.php/Api/Pic/uploadPic"

    const val DOWNLOAD_URL = "https://apk-ssl.tancdn.com/3.5.3_276/%E6%8E%A2%E6%8E%A2.apk"
}