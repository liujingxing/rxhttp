package com.example.httpsender.param;

import okhttp3.Request;
import okhttp3.RequestBody;
import rxhttp.wrapper.annotation.Param;
import rxhttp.wrapper.param.BodyParam;
import rxhttp.wrapper.param.Method;

/**
 * User: ljx
 * Date: 2019-09-11
 * Time: 11:52
 */
@Param(methodName = "postEncryptJson1")
public class PostEncryptJsonParam1 extends BodyParam<PostEncryptJsonParam1> {

    public PostEncryptJsonParam1(String url) {
        super(url, Method.POST);
    }

    @Override
    public Request buildRequest() {
        return null;
    }

    @Override
    public RequestBody getRequestBody() {
        return null;
    }
}
