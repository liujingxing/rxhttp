package com.example.httpsender.parser.java;

import com.example.httpsender.entity.PageList;
import com.example.httpsender.entity.Response;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.lang.reflect.Type;

import rxhttp.wrapper.entity.ParameterizedTypeImpl;
import rxhttp.wrapper.exception.ParseException;
import rxhttp.wrapper.parse.AbstractParser;

/**
 * 输入T，输出PageList<T>，并对code统一判断
 * User: ljx
 * Date: 2018/10/23
 * Time: 13:49
 */
//@Parser(name = "ResponsePageList")
public class ResponsePageListParser<T> extends AbstractParser<PageList<T>> {

    /**
     * 此构造方法适用于任意Class对象，但更多用于带泛型的Class对象，如：List<Student>
     * <p>
     * 用法:
     * Java: .asParser(new ResponsePageListParser<List<Student>>(){})
     * Kotlin: .asParser(object : ResponsePageListParser<List<Student>>() {})
     * <p>
     * 注：此构造方法一定要用protected关键字修饰，否则调用此构造方法将拿不到泛型类型
     */
    protected ResponsePageListParser() {
        super();
    }

    /**
     * 此构造方法仅适用于解析不带泛型的Class对象，如: Student.class
     * <p>
     * 用法
     * Java: .asParser(new ResponsePageListParser<>(Student.class))   或者  .asResponsePageList(Student.class)
     * Kotlin: .asParser(ResponsePageListParser(Student::class.java)) 或者  .asResponsePageList(Student::class.java)
     */
    public ResponsePageListParser(Class<T> type) {
        super(type);
    }

    @Override
    public PageList<T> onParse(@NotNull okhttp3.Response response) throws IOException {
        final Type type = ParameterizedTypeImpl.get(Response.class, PageList.class, mType); //获取泛型类型
        Response<PageList<T>> data = convert(response, type);
        PageList<T> pageList = data.getData(); //获取data字段
        if (data.getCode() != 0 || pageList == null) {  //code不等于0，说明数据不正确，抛出异常
            throw new ParseException(String.valueOf(data.getCode()), data.getMsg(), response);
        }
        return pageList;
    }
}
