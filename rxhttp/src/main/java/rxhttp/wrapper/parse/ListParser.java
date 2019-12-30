package rxhttp.wrapper.parse;


import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;

import okhttp3.Response;
import rxhttp.wrapper.entity.ParameterizedTypeImpl;

/**
 * 将Response对象解析成List对象
 * User: ljx
 * Date: 2018/10/23
 * Time: 13:49
 */
public class ListParser<T> extends AbstractParser<List<T>> {

    protected ListParser() {
        super();
    }

    private ListParser(Type type) {
        super(type);
    }

    public static <T> ListParser<T> get(Class<T> type) {
        return new ListParser<>(type);
    }

    @Override
    public List<T> onParse(Response response) throws IOException {
        final Type type = ParameterizedTypeImpl.get(List.class, mType); //拿到泛型类型
        return convert(response, type);
    }
}
