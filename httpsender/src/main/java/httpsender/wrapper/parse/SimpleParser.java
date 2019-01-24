package httpsender.wrapper.parse;


import java.io.IOException;
import java.lang.reflect.Type;

import httpsender.wrapper.exception.ParseException;
import httpsender.wrapper.utils.GsonUtil;
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

    @SuppressWarnings("unchecked")
    @Override
    public T onParse(Response response) throws IOException {
        String content = getResult(response);
        Type type = getActualTypeParameter();
        if (type == String.class) return (T) content;
        T t = GsonUtil.getObject(content, type);
        if (t == null)
            throw new ParseException("data parse error");
        return t;
    }
}
