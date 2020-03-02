package com.example.httpsender.parser;


import com.example.httpsender.entity.Response;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;

import rxhttp.wrapper.annotation.Parser;
import rxhttp.wrapper.entity.ParameterizedTypeImpl;
import rxhttp.wrapper.exception.ParseException;
import rxhttp.wrapper.parse.AbstractParser;

/**
 * Response<List<T>> 数据解析器,解析完成对Response对象做判断,如果ok,返回数据 List<T>
 * User: ljx
 * Date: 2018/10/23
 * Time: 13:49
 */
@Parser(name = "ResponseList")
public class ResponseListParser<T> extends AbstractParser<List<T>> {

    //注意：此构造方法一定要用protected关键字修饰，否则调用此构造方法将拿不到泛型类型
    protected ResponseListParser() {
        super();
    }

    public ResponseListParser(Class<T> type) {
        super(type);
    }

    @Override
    public List<T> onParse(okhttp3.Response response) throws IOException {
        final Type type = ParameterizedTypeImpl.get(Response.class, List.class, mType); //获取泛型类型
        Response<List<T>> data = convert(response, type);
        List<T> list = data.getData(); //获取data字段
        if (data.getCode() != 0 || list == null) {  //code不等于0，说明数据不正确，抛出异常
            throw new ParseException(String.valueOf(data.getCode()), data.getMsg(), response);
        }
        return list;
    }
}
