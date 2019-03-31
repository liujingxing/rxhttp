package httpsender.wrapper.entity;


import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;

/**
 * User: ljx
 * Date: 2018/10/23
 * Time: 09:36
 */
public class ParameterizedTypeImpl implements ParameterizedType {

    private final Type   rawType;
    private final Type   ownerType;
    private final Type[] actualTypeArguments;

    public ParameterizedTypeImpl(Type rawType, Type actualType) {
        this(rawType, actualType, null);
    }

    public ParameterizedTypeImpl(Type rawType, Type actualType, Type ownerType) {
        this.rawType = rawType;
        this.ownerType = ownerType;
        this.actualTypeArguments = new Type[]{actualType};
    }

    /**
     * 根据types数组，确定具体的泛型类型
     * List<List<String>>  对应  get(List.class, List.class, String.class)
     *
     * @param types Type数组
     * @return ParameterizedTypeImpl
     */
    public static ParameterizedTypeImpl get(Type... types) {
        final int length = types.length;
        if (length > 2) {
            Type parameterizedType = new ParameterizedTypeImpl(types[length - 1], types[length - 2]);
            Type[] newTypes = Arrays.copyOf(types, length - 1);
            newTypes[newTypes.length - 1] = parameterizedType;
            return get(newTypes);
        }
        return new ParameterizedTypeImpl(types[0], types[1]);
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