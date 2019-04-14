package httpsender.wrapper.entity;


import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;

import io.reactivex.annotations.NonNull;

/**
 * User: ljx
 * Date: 2018/10/23
 * Time: 09:36
 */
public class ParameterizedTypeImpl implements ParameterizedType {

    private final Type   rawType;
    private final Type   ownerType;
    private final Type[] actualTypeArguments;

    //适用于单个泛型参数的类
    public ParameterizedTypeImpl(Type rawType, Type actualType) {
        this(null, rawType, actualType);
    }

    //适用于多个泛型参数的类
    public ParameterizedTypeImpl(Type ownerType, Type rawType, Type... actualTypeArguments) {
        this.rawType = rawType;
        this.ownerType = ownerType;
        this.actualTypeArguments = actualTypeArguments;
    }

    /**
     * 本方法仅使用于单个泛型参数的类
     * 根据types数组，确定具体的泛型类型
     * List<List<String>>  对应  get(List.class, List.class, String.class)
     *
     * @param types Type数组
     * @return ParameterizedTypeImpl
     */
    public static ParameterizedTypeImpl get(@NonNull Type rawType, @NonNull Type... types) {
        final int length = types.length;
        if (length > 1) {
            Type parameterizedType = new ParameterizedTypeImpl(types[length - 2], types[length - 1]);
            Type[] newTypes = Arrays.copyOf(types, length - 1);
            newTypes[newTypes.length - 1] = parameterizedType;
            return get(rawType, newTypes);
        }
        return new ParameterizedTypeImpl(rawType, types[0]);
    }

    //适用于多个泛型参数的类
    public static ParameterizedTypeImpl getParameterized(@NonNull Type rawType, @NonNull Type... actualTypeArguments) {
        return new ParameterizedTypeImpl(null, rawType, actualTypeArguments);
    }

    public final Type[] getActualTypeArguments() {
        return actualTypeArguments;
    }

    public final Type getOwnerType() {
        return ownerType;
    }

    public final Type getRawType() {
        return rawType;
    }

}