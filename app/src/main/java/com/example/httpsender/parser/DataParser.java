package com.example.httpsender.parser;


import com.example.httpsender.entity.Data;

import java.io.IOException;
import java.lang.reflect.Type;

import rxhttp.wrapper.annotation.Parser;
import rxhttp.wrapper.entity.ParameterizedTypeImpl;
import rxhttp.wrapper.exception.ParseException;
import rxhttp.wrapper.parse.AbstractParser;
import rxhttp.wrapper.utils.GsonUtil;

/**
 * Data<T> 数据解析器 ,解析完成对Data对象做判断,如果ok,返回数据 T
 * User: ljx
 * Date: 2018/10/23
 * Time: 13:49
 */
@Parser(name = "Data")
public class DataParser<T> extends AbstractParser<T> {

    protected DataParser() {
        super();
    }

    private DataParser(Type type) {
        super(type);
    }

    public static <T> DataParser<T> get(Class<T> type) {
        return new DataParser<>(type);
    }

    /**
     * @param response Http执行结果
     * @return 开发者传入的泛型类型
     * @throws IOException 网络异常、数据异常等，RxJava的观察者会捕获此异常
     */
    @Override
    public T onParse(okhttp3.Response response) throws IOException {
        String content = getResult(response); //从Response中取出Http执行结果
        final Type type = ParameterizedTypeImpl.get(Data.class, mType); //获取泛型类型
        //通过Gson自动解析成Data<T>对象
        Data<T> data = GsonUtil.getObject(content, type);
        if (data == null) //为空 ，表明数据不正确
            throw new ParseException("data parse error");
        //跟服务端协议好，code等于100，才代表数据正确,否则，抛出异常
        if (data.getCode() != 100) {
            throw new ParseException(String.valueOf(data.getCode()), data.getMsg());
        }
        T t = data.getData(); //获取data字段
        if (t == null) {  //为空，有可能是参数错误或者其他原因，导致服务器不能正确给我们data字段数据
            throw new ParseException(String.valueOf(data.getCode()), data.getMsg());
        }
        return t;
    }
}
