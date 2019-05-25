package com.example.httpsender.entity;


/**
 * User: ljx
 * Date: 2018/10/21
 * Time: 13:16
 */
public class Data<T> {

    private int    code;
    private String msg;
    private T      data;

    public int getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }

    public T getData() {
        return data;
    }
}
