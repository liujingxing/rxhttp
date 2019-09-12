package com.example.httpsender.param;

import rxhttp.wrapper.annotation.Param;
import rxhttp.wrapper.param.Method;
import rxhttp.wrapper.param.NoBodyParam;

/**
 * User: ljx
 * Date: 2019-09-12
 * Time: 17:25
 */
@Param(methodName = "getEncrypt")
public class GetEncryptParam extends NoBodyParam {

    public GetEncryptParam(String url) {
        super(url, Method.GET);
    }

    public void test(){

    }
}
