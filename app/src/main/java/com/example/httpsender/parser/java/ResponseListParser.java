package com.example.httpsender.parser.java;

import com.example.httpsender.entity.Response;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;

import rxhttp.wrapper.annotation.Parser;
import rxhttp.wrapper.entity.ParameterizedTypeImpl;
import rxhttp.wrapper.exception.ParseException;
import rxhttp.wrapper.parse.AbstractParser;

/**
 * 输入T，输出List<T>，并对code统一判断
 * User: ljx
 * Date: 2018/10/23
 * Time: 13:49
 */
//@Parser(name = "ResponseList")
public class ResponseListParser<T> extends AbstractParser<List<T>> {

    /**
     * 此构造方法适用于任意Class对象，但更多用于带泛型的Class对象，如：List<Student>
     *
     * 用法:
     * Java: .asParser(new ResponseListParser<List<Student>>(){})
     * Kotlin: .asParser(object : ResponseListParser<List<Student>>() {})
     *
     * 注：此构造方法一定要用protected关键字修饰，否则调用此构造方法将拿不到泛型类型
     */
    protected ResponseListParser() {
        super();
    }

    /**
     * 此构造方法仅适用于解析不带泛型的Class对象，如: Student.class
     *
     * 用法
     * Java: .asParser(new ResponseListParser<>(Student.class))   或者  .asResponseList(Student.class)
     * Kotlin: .asParser(ResponseListParser(Student::class.java)) 或者  .asResponseList(Student::class.java)
     */
    public ResponseListParser(Class<T> type) {
        super(type);
    }

    @Override
    public List<T> onParse(@NotNull okhttp3.Response response) throws IOException {
        final Type type = ParameterizedTypeImpl.get(Response.class, List.class, mType); //获取泛型类型
        Response<List<T>> data = convert(response, type);
        List<T> list = data.getData(); //获取data字段
        if (data.getCode() != 0 || list == null) {  //code不等于0，说明数据不正确，抛出异常
            throw new ParseException(String.valueOf(data.getCode()), data.getMsg(), response);
        }
        return list;
    }
}
