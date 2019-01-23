package com.example.httpsender;


import httpsender.wrapper.exception.*;
import httpsender.wrapper.parse.*;

import java.io.IOException;
import java.lang.reflect.Type;

import httpsender.wrapper.utils.GsonUtil;

/**
 * 数据解析器
 * User: ljx
 * Date: 2018/10/23
 * Time: 13:49
 */
public class DataParser<T> extends AbstractParser<T> {

    protected DataParser() {
        super();
    }

    @SuppressWarnings("unchecked")
    @Override
    public T onParse(okhttp3.Response response) throws IOException {
        String content = getResult(response);
        final Type type = getActualTypeParameter();
        Response<T> hr = GsonUtil.getObject(content, ResponseType.get(type));
        throwIfFatal(hr);
        T t = hr.getData();
        if (t == null) {
            if (type == String.class) return (T) hr.getMsg();
            throw new EmptyObjectException(String.valueOf(hr.getCode()), hr.getMsg());
        }
        return t;
    }

    private void throwIfFatal(Response response) throws IOException {
        if (response == null)
            throw new ParseException("data parse error");
        if (!response.isSuccess())
            throw new FailException(String.valueOf(response.getCode()), response.getMsg());
    }
}
