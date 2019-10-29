package rxhttp.wrapper.parse;


import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;

import okhttp3.Response;
import rxhttp.wrapper.entity.ParameterizedTypeImpl;
import rxhttp.wrapper.utils.GsonUtil;

/**
 * 将Response对象解析成List<T>对象
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
        String content = getResult(response);//Response转String
        final Type type = ParameterizedTypeImpl.get(List.class, mType); //拿到泛型类型
        return GsonUtil.fromJson(content, type); //将content自动解析成调用者传入的泛型对象
    }
}
