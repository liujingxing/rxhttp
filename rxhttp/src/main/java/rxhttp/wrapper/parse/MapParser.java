package rxhttp.wrapper.parse;


import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Map;

import okhttp3.Response;
import rxhttp.wrapper.entity.ParameterizedTypeImpl;
import rxhttp.wrapper.exception.ParseException;
import rxhttp.wrapper.utils.GsonUtil;
import rxhttp.wrapper.utils.TypeUtil;

/**
 * 将Response对象解析成泛型Map<K,V>对象
 * User: ljx
 * Date: 2018/10/23
 * Time: 13:49
 */
public class MapParser<K, V> implements Parser<Map<K, V>> {
    private Type kType, vType;

    protected MapParser() {
        kType = TypeUtil.getActualTypeParameter(this.getClass(), 0);
        kType = TypeUtil.getActualTypeParameter(this.getClass(), 1);
    }

    private MapParser(Type kType, Type vType) {
        this.kType = kType;
        this.vType = vType;
    }

    public static <K, V> MapParser<K, V> get(Class<K> kType, Class<V> vType) {
        return new MapParser<>(kType, vType);
    }

    @Override
    public Map<K, V> onParse(Response response) throws IOException {
        String content = getResult(response);
        final Type type = ParameterizedTypeImpl.getParameterized(Map.class, kType, vType);
        Map<K, V> t = GsonUtil.getObject(content, type);  //json 转 对象
        if (t == null)  //解析失败，抛出异常
            throw new ParseException("data parse error");
        return t;
    }
}
