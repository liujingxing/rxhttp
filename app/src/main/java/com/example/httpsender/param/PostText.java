package com.example.httpsender.param;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import rxhttp.wrapper.annotation.Param;
import rxhttp.wrapper.param.JsonParam;
import rxhttp.wrapper.param.Method;

/**
 * 发送任意文本
 * User: ljx
 * Date: 2019-09-12
 * Time: 17:25
 */
@Param(methodName = "postText")
public class PostText extends JsonParam {

    private static final MediaType MEDIA_TYPE = MediaType.get("application/json; charset=UTF-8");

    private String text;

    public PostText(String url) {
        super(url, Method.POST);
    }

    public PostText setText(String text) {
        this.text = text;
        return this;
    }

    @Override
    public RequestBody getRequestBody() {
        return RequestBody.create(MEDIA_TYPE, text);
    }
}
