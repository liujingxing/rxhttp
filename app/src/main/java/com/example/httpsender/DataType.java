package com.example.httpsender;



import java.lang.reflect.Type;

import httpsender.wrapper.entity.ParameterizedTypeImpl;

/**
 * User: ljx
 * Date: 2018/10/23
 * Time: 09:36
 */
public class DataType extends ParameterizedTypeImpl {

    private DataType(Type actualType) {
        super(new Type[]{actualType}, null, Data.class);
    }

    public static DataType get(Type actualType) {
        return new DataType(actualType);
    }

}