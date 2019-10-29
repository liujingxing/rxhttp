package rxhttp.wrapper.parse;


import java.io.IOException;
import java.lang.reflect.Type;

import okhttp3.Response;
import rxhttp.wrapper.utils.GsonUtil;

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
        return GsonUtil.fromJson(content, type); //json 转 对象
    }
}
