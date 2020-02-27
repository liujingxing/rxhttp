package com.example.httpsender.entity;

import rxhttp.wrapper.annotation.DefaultDomain;
import rxhttp.wrapper.annotation.Domain;

/**
 * User: ljx
 * Date: 2020/2/27
 * Time: 23:55
 */
public class Url {

    @Domain(name = "Update")
    public static String update = "http://update.9158.com";

    @DefaultDomain //设置为默认域名
    public static String baseUrl = "https://www.wanandroid.com/";
}
