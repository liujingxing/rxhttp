package com.example.httpsender.param;

import rxhttp.wrapper.annotation.Param;
import rxhttp.wrapper.param.FormParam;
import rxhttp.wrapper.param.Method;

/**
 * User: ljx
 * Date: 2019-09-11
 * Time: 11:52
 */
@Param(methodName = "postEncryptForm")
public class PostEncryptFormParam extends FormParam {

    public PostEncryptFormParam(String url) {
        super(url, Method.POST);
    }

}
