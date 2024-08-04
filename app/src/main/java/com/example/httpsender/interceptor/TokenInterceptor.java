package com.example.httpsender.interceptor;


import com.example.httpsender.entity.User;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import rxhttp.wrapper.param.RxHttp;

/**
 * token 失效，自动刷新token，然后再次发送请求，用户无感知
 * User: ljx
 * Date: 2019-12-04
 * Time: 11:56
 */
public class TokenInterceptor implements Interceptor {

    //保存刷新后的token
    private final AtomicReference<String> atomicToken = new AtomicReference<>();

    //token刷新时间
    @NotNull
    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        Response originalResponse = chain.proceed(request);
        String code = originalResponse.header("xxx"); //其中xxx，自己跟服务端协定
        if ("-1".equals(code)) { //token 失效 这里根据自己的业务需求写判断条件
            atomicToken.set(null);
            return handleTokenInvalid(chain, request);
        }
        return originalResponse;
    }

    //处理token失效问题, 同步刷新
    private Response handleTokenInvalid(Chain chain, Request request) throws IOException {
        boolean success = refreshToken();
        Request newRequest;
        if (success) { //刷新成功，重新添加token
            newRequest = request.newBuilder()
                .header("xxx", atomicToken.get())  //其中xxx，自己跟服务端协定
                .build();
        } else {
            newRequest = request;
        }
        return chain.proceed(newRequest);
    }

    //刷新token, 考虑到有并发情况，故这里需要加锁
    private boolean refreshToken() {
        //token不等于null，说明已经刷新
        if (atomicToken.get() != null) return true;
        synchronized (this) {
            //再次判断是否已经刷新
            if (atomicToken.get() != null) return true;
            try {
                //根据自己业务，同步刷新token, 注意这里千万不能异步
                String token = RxHttp.postForm("/refreshToken/...")
                    .executeString();
                atomicToken.set(token);
                User.get().setToken(token); //保存最新的token
                return true;
            } catch (IOException e) {
                return false;
            }
        }
    }
}
