package rxhttp.wrapper.utils;

import com.google.gson.internal.$Gson$Types;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * User: ljx
 * Date: 2019-06-16
 * Time: 13:36
 */
public class TypeUtil {

    /**
     * 获取泛型类型
     *
     * @param clazz 类类型
     * @param index 第几个泛型
     * @return Type
     */
    public static Type getActualTypeParameter(Class clazz, int index) {
        Type superclass = clazz.getGenericSuperclass();
        if (!(superclass instanceof ParameterizedType)) {
            throw new RuntimeException("Missing type parameter.");
        }
        ParameterizedType parameter = (ParameterizedType) superclass;
        return $Gson$Types.canonicalize(parameter.getActualTypeArguments()[index]);
    }
}
