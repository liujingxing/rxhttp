package com.example.httpsender;



import java.lang.reflect.Type;

import httpsender.wrapper.entity.ParameterizedTypeImpl;

/**
 * User: ljx
 * Date: 2018/10/23
 * Time: 09:36
 */
public class ResponseType extends ParameterizedTypeImpl {

    private ResponseType(Type actualType) {
        super(new Type[]{actualType}, null, Response.class);
    }

    public static ResponseType get(Type actualType) {
        return new ResponseType(actualType);
    }

}