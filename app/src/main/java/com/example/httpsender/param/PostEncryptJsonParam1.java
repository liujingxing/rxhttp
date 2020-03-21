package com.example.httpsender.param;

import okhttp3.Request;
import okhttp3.RequestBody;
import rxhttp.wrapper.annotation.Param;
import rxhttp.wrapper.param.AbstractParam;
import rxhttp.wrapper.param.Method;

/**
 * User: ljx
 * Date: 2019-09-11
 * Time: 11:52
 */
@Param(methodName = "postEncryptJson1")
public class PostEncryptJsonParam1 extends AbstractParam<PostEncryptJsonParam1> {

    public PostEncryptJsonParam1(String url) {
        super(url, Method.POST);
    }

    @Override
    public RequestBody getRequestBody() {
        return null;
    }

    public void test(){

    }

    @Override
    public PostEncryptJsonParam1 add(String key, Object value) {
        return null;
    }
}
