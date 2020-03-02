package com.example.httpsender.parser;


import com.example.httpsender.entity.Response;

import java.io.IOException;
import java.lang.reflect.Type;

import rxhttp.wrapper.annotation.Parser;
import rxhttp.wrapper.entity.ParameterizedTypeImpl;
import rxhttp.wrapper.exception.ParseException;
import rxhttp.wrapper.parse.AbstractParser;

/**
 * 输入T,输出T,并对code统一判断
 * User: ljx
 * Date: 2018/10/23
 * Time: 13:49
 */
@Parser(name = "Response")
public class ResponseParser<T> extends AbstractParser<T> {

    //注意：此构造方法一定要用protected关键字修饰，否则调用此构造方法将拿不到泛型类型
    protected ResponseParser() {
        super();
    }

    public ResponseParser(Class<T> type) {
        super(type);
    }

    @SuppressWarnings("unchecked")
    @Override
    public T onParse(okhttp3.Response response) throws IOException {
        final Type type = ParameterizedTypeImpl.get(Response.class, mType); //获取泛型类型
        Response<T> data = convert(response, type);
        T t = data.getData(); //获取data字段
        if (t == null && mType == String.class) {
            /*
             * 考虑到有些时候服务端会返回：{"errorCode":0,"errorMsg":"关注成功"}  类似没有data的数据
             * 此时code正确，但是data字段为空，直接返回data的话，会报空指针错误，
             * 所以，判断泛型为String类型时，重新赋值，并确保赋值不为null
             */
            t = (T) data.getMsg();
        }
        if (data.getCode() != 0 || t == null) {//code不等于0，说明数据不正确，抛出异常
            throw new ParseException(String.valueOf(data.getCode()), data.getMsg(), response);
        }
        return t;
    }
}
