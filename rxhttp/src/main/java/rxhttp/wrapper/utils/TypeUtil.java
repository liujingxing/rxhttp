package rxhttp.wrapper.utils;

import org.jetbrains.annotations.Nullable;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import rxhttp.wrapper.entity.OkResponse;

/**
 * User: ljx
 * Date: 2022/10/23
 * Time: 19:20
 */
public class TypeUtil {

    public static Type[] getActualTypeParameters(Class<?> clazz) {
        Type superclass = clazz.getGenericSuperclass();
        if (superclass instanceof ParameterizedType) {
            ParameterizedType parameterized = (ParameterizedType) superclass;
            return parameterized.getActualTypeArguments();
        }
        throw new RuntimeException("Missing type parameter.");
    }

    @Nullable
    public static Type getActualType(Type type) {
        if (type instanceof ParameterizedType) {
            ParameterizedType parameterized = (ParameterizedType) type;
            if (parameterized.getRawType() == OkResponse.class) {
                return parameterized.getActualTypeArguments()[0];
            }
        }
        return null;
    }
}
