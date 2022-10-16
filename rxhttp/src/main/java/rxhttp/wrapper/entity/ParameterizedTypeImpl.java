package rxhttp.wrapper.entity;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import rxhttp.wrapper.utils.Utils;


/**
 * User: ljx
 * Date: 2022/9/27
 * Time: 17:28
 */
public class ParameterizedTypeImpl implements ParameterizedType {

    private final Type rawType;
    private final Type ownerType;
    private final Type[] actualTypeArguments;

    public ParameterizedTypeImpl(Type rawType, Type actualType) {
        this(null, rawType, actualType);
    }

    public ParameterizedTypeImpl(@Nullable Type ownerType, Type rawType, Type... actualTypeArguments) {
        this.rawType = rawType;
        this.ownerType = ownerType;
        this.actualTypeArguments = actualTypeArguments;
    }

    // get(List.class, List.class, String.class) equivalent to List<List<String>>
    public static ParameterizedType get(@NotNull Type rawType, @NotNull Type... types) {
        final int length = types.length;
        Type lastType = Utils.getWrapType(types[length - 1]);
        for (int i = length - 2; i >= 0; i--) {
            lastType = new ParameterizedTypeImpl(types[i], lastType);
        }
        return new ParameterizedTypeImpl(rawType, lastType);
    }

    // getParameterized(List.Class, String.class) equivalent to List<String>
    // getParameterized(Map.Class, String.class, Integer.class) equivalent to Map<String, Integer>
    public static ParameterizedType getParameterized(@NotNull Type rawType, @NotNull Type... types) {
        final int length = types.length;
        Type[] newTypes = new Type[length];
        for (int i = 0; i < length; i++) {
            newTypes[i] = Utils.getWrapType(types[i]);
        }
        return new ParameterizedTypeImpl(null, rawType, newTypes);
    }

    @Override
    public final Type[] getActualTypeArguments() {
        return actualTypeArguments;
    }

    @Override
    public final Type getOwnerType() {
        return ownerType;
    }

    @Override
    public final Type getRawType() {
        return rawType;
    }
}
