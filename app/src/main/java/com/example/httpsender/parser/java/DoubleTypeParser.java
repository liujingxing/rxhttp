package com.example.httpsender.parser.java;

import android.util.Pair;

import com.example.httpsender.entity.Response;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.lang.reflect.Type;

import rxhttp.wrapper.entity.ParameterizedTypeImpl;
import rxhttp.wrapper.exception.ParseException;
import rxhttp.wrapper.parse.TypeParser;
import rxhttp.wrapper.utils.Converter;

/**
 * 大于等于2个泛型的解析器，可以参考此类
 * User: ljx
 * Date: 2018/10/23
 * Time: 13:49
 */
//@Parser(name = "DoubleType")
public class DoubleTypeParser<F, S> extends TypeParser<Pair<F, S>> {

    protected DoubleTypeParser() {
        super();
    }

    public DoubleTypeParser(Type fType, Type sType) {
        super(fType, sType);
    }

    @Override
    public Pair<F, S> onParse(@NotNull okhttp3.Response response) throws IOException {
        Type pairType = ParameterizedTypeImpl.getParameterized(Pair.class, types);
        Response<Pair<F, S>> data = Converter.convertTo(response, Response.class, pairType);
        Pair<F, S> t = data.getData(); //获取data字段
        if (data.getCode() != 0 || t == null) {//code不等于0，说明数据不正确，抛出异常
            throw new ParseException(String.valueOf(data.getCode()), data.getMsg(), response);
        }
        return t;
    }
}
