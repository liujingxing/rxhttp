package com.example.httpsender;



import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;

import httpsender.wrapper.annotation.Parser;
import httpsender.wrapper.entity.ParameterizedTypeImpl;
import httpsender.wrapper.exception.ParseException;
import httpsender.wrapper.parse.AbstractParser;
import httpsender.wrapper.utils.GsonUtil;

/**
 * Data<List<T>> 数据解析器,解析完成对Data对象做判断,如果ok,返回数据 List<T>
 * User: ljx
 * Date: 2018/10/23
 * Time: 13:49
 */
@Parser(name = "DataListParser")
public class DataListParser<T> extends AbstractParser<List<T>> {

    protected DataListParser() {
        super();
    }

    private DataListParser(Type type) {
        super(type);
    }

    public static <T> DataListParser<T> get(Class<T> type) {
        return new DataListParser<>(type);
    }

    @Override
    public List<T> onParse(okhttp3.Response response) throws IOException {
        String content = getResult(response); //从Response中取出Http执行结果
        final Type type = ParameterizedTypeImpl.get(Data.class, List.class, mType); //获取泛型类型
        Data<List<T>> data = GsonUtil.getObject(content, type);
        if (data == null) //为空 ，表明数据不正确
            throw new ParseException("data parse error");
        //跟服务端协议好，code等于100，才代表数据正确,否则，抛出异常
        if (data.getCode() != 100) {
            throw new ParseException(String.valueOf(data.getCode()), data.getMsg());
        }
        List<T> t = data.getData(); //获取data字段
        if (t == null) {  //为空，有可能是参数错误或者其他原因，导致服务器不能正确给我们data字段数据
            throw new ParseException(String.valueOf(data.getCode()), data.getMsg());
        }
        return t;
    }
}
