package com.example.httpsender.interceptor;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * 处理重定向的拦截器，非必须
 * User: ljx
 * Date: 2019-12-17
 * Time: 23:24
 */
public class RedirectInterceptor implements Interceptor {

    @Override
    public Response intercept(Chain chain) throws IOException {
        okhttp3.Request request = chain.request();
        Response response = chain.proceed(request);
        int code = response.code();
        if (code == 308) {
            //获取重定向的地址
            String location = response.headers().get("Location");
            //重新构建请求
            Request newRequest = request.newBuilder().url(location).build();
            response.close();
            response = chain.proceed(newRequest);
        }
        return response;
    }
}
