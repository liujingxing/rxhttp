package com.example.httpsender.entity;


/**
 * User: ljx
 * Date: 2018/10/21
 * Time: 13:16
 */
public class Response<T> {

    private int    errorCode;
    private String errorMsg;
    private T      data;

    public int getCode() {
        return errorCode;
    }

    public String getMsg() {
        return errorMsg;
    }

    public T getData() {
        return data;
    }
}
