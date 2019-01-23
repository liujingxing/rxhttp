package com.example.httpsender;


/**
 * User: ljx
 * Date: 2018/10/21
 * Time: 13:16
 */
public class Response<T> {

    private int    code;
    private String msg;
    private T      data;

    public int getCode() {
        return code;
    }

    public String getMsg() {
        return msg == null ? "" : msg;
    }

    public T getData() {
        return data;
    }

    public boolean isSuccess() {
        return true;
    }
}
