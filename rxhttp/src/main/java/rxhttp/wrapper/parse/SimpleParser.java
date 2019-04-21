package rxhttp.wrapper.parse;


import java.io.IOException;
import java.lang.reflect.Type;

import rxhttp.wrapper.exception.ParseException;
import rxhttp.wrapper.utils.GsonUtil;
import okhttp3.Response;

/**
 * 将Response对象解析成泛型T对象
 * User: ljx
 * Date: 2018/10/23
 * Time: 13:49
 */
public class SimpleParser<T> extends AbstractParser<T> {

    protected SimpleParser() {
        super();
    }

    private SimpleParser(Type type) {
        super(type);
    }

    public static <T> SimpleParser<T> get(Class<T> type) {
        return new SimpleParser<>(type);
    }

    @SuppressWarnings("unchecked")
    @Override
    public T onParse(Response response) throws IOException {
        String content = getResult(response);//Response转String
        final Type type = mType; //拿到泛型类型
        if (type == String.class) return (T) content; //泛型是String类型，直接返回
        T t = GsonUtil.getObject(content, type); //将content自动解析成调用者传入的泛型对象
        if (t == null)//为空，说明数据不正确，直接抛出异常，RxJava的观察者会捕获此异常
            throw new ParseException("data parse error");
        return t;
    }
}
