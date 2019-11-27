package rxhttp.wrapper.parse;


import java.io.IOException;
import java.lang.reflect.Type;

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


    @Override
    public T onParse(Response response) throws IOException {
        return convert(response, mType);
    }
}
