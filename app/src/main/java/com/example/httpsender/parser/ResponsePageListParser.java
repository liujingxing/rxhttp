package com.example.httpsender.parser;


import com.example.httpsender.entity.PageList;
import com.example.httpsender.entity.Response;

import java.io.IOException;
import java.lang.reflect.Type;

import rxhttp.wrapper.annotation.Parser;
import rxhttp.wrapper.entity.ParameterizedTypeImpl;
import rxhttp.wrapper.exception.ParseException;
import rxhttp.wrapper.parse.AbstractParser;

/**
 * Response<PageList<T>> 数据解析器,解析完成对Response对象做判断,如果ok,返回数据 PageList<T>
 * User: ljx
 * Date: 2018/10/23
 * Time: 13:49
 */
@Parser(name = "ResponsePageList")
public class ResponsePageListParser<T> extends AbstractParser<PageList<T>> {

    protected ResponsePageListParser() {
        super();
    }

    public ResponsePageListParser(Class<T> type) {
        super(type);
    }

    @Override
    public PageList<T> onParse(okhttp3.Response response) throws IOException {
        final Type type = ParameterizedTypeImpl.get(Response.class, PageList.class, mType); //获取泛型类型
        Response<PageList<T>> data = convert(response, type);
        PageList<T> pageList = data.getData(); //获取data字段
        if (data.getCode() != 0 || pageList == null) {  //code不等于0，说明数据不正确，抛出异常
            throw new ParseException(String.valueOf(data.getCode()), data.getMsg(), response);
        }
        return pageList;
    }
}
