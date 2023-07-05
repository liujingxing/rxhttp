package com.example.httpsender.parser.java;

import com.example.httpsender.entity.Response;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.lang.reflect.Type;

import rxhttp.wrapper.exception.ParseException;
import rxhttp.wrapper.parse.TypeParser;
import rxhttp.wrapper.utils.Converter;

/**
 * 输入T,输出T,并对code统一判断
 * User: ljx
 * Date: 2018/10/23
 * Time: 13:49
 */
//@Parser(name = "Response", wrappers = {PageList.class})
public class ResponseParser<T> extends TypeParser<T> {

    /**
     * 此构造方法可适用任意Class对象，但更多用于带泛型的Class对象，如：List<List<Student>>>
     * <p>
     * 如Java环境中调用
     * toObservable(new ResponseParser<List<List<Student>>>(){})
     * 等价与kotlin环境下的
     * toObservableResponse<List<List<Student>>>()
     * <p>
     * 注：此构造方法一定要用protected关键字修饰，否则调用此构造方法将拿不到泛型类型
     */
    protected ResponseParser() {
        super();
    }

    /**
     * 该解析器会生成以下系列方法，前3个kotlin环境调用，后4个Java环境调用，所有方法内部均会调用本构造方法
     * toFlowResponse<T>()
     * toAwaitResponse<T>()
     * toObservableResponse<T>()
     * toObservableResponse(Type)
     * toObservableResponse(Class<T>)
     * toObservableResponseList(Class<T>)
     * toObservableResponsePageList(Class<T>)
     * <p>
     * Flow/Await下 toXxxResponse<PageList<T>> 等价与 toObservableResponsePageList(Class<T>)
     */
    public ResponseParser(Type type) {
        super(type);
    }

    @SuppressWarnings("unchecked")
    @Override
    public T onParse(@NotNull okhttp3.Response response) throws IOException {
        Response<T> data = Converter.convertTo(response, Response.class, types);
        T t = data.getData(); //获取data字段
        if (t == null && types[0] == String.class) {
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
