package com.example.httpsender.param;

import rxhttp.wrapper.annotation.Param;
import rxhttp.wrapper.param.FormParam;
import rxhttp.wrapper.param.Method;

/**
 * 此类中自己声明的所有public方法(构造方法除外)都会在RxHttp$PostEncryptFormParam类中一一生成，
 * 并一一对应调用。如: RxHttp$PostEncryptFormParam.test(int,int)方法内部会调用本类的test(int,int)方法
 * User: ljx
 * Date: 2019-09-11
 * Time: 11:52
 */
@Param(methodName = "postEncryptForm")
public class PostEncryptFormParam extends FormParam {

    public PostEncryptFormParam(String url) {
        super(url, Method.POST);
    }

    public PostEncryptFormParam(String url, Method method) {
        super(url, method);
    }

    public PostEncryptFormParam test1(String s) {
        return this;
    }

    //此方法会在
    public PostEncryptFormParam test2(long a, float b) {
        return this;
    }

    public int add(int a, int b) {
        return a + b;
    }
}
