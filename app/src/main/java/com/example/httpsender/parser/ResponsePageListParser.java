package com.example.httpsender.parser;


import com.example.httpsender.entity.PageList;
import com.example.httpsender.entity.Response;

import java.io.IOException;
import java.lang.reflect.Type;

import rxhttp.wrapper.annotation.Parser;
import rxhttp.wrapper.entity.ParameterizedTypeImpl;
import rxhttp.wrapper.exception.ParseException;
import rxhttp.wrapper.parse.AbstractParser;
import rxhttp.wrapper.utils.GsonUtil;

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

    public ResponsePageListParser(Type type) {
        super(type);
    }

    @Override
    public PageList<T> onParse(okhttp3.Response response) throws IOException {
        String content = getResult(response); //从Response中取出Http执行结果
        final Type type = ParameterizedTypeImpl.get(Response.class, PageList.class, mType); //获取泛型类型
        Response<PageList<T>> data = GsonUtil.fromJson(content, type);
        //跟服务端协议好，code等于0，才代表数据正确,否则，抛出异常
        if (data.getCode() != 0) {
            throw new ParseException(String.valueOf(data.getCode()), data.getMsg(), response);
        }
        PageList<T> t = data.getData(); //获取data字段
        if (t == null) {  //为空，有可能是参数错误或者其他原因，导致服务器不能正确给我们data字段数据
            throw new ParseException(String.valueOf(data.getCode()), data.getMsg(), response);
        }
        return t;
    }
}
