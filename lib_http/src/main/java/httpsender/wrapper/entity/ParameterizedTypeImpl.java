package httpsender.wrapper.entity;


import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * User: ljx
 * Date: 2018/10/23
 * Time: 09:36
 */
public class ParameterizedTypeImpl implements ParameterizedType {

    private final Type[] actualTypeArguments;
    private final Type   ownerType;
    private final Type   rawType;

    public ParameterizedTypeImpl(Type[] actualTypeArguments, Type ownerType, Type rawType) {
        this.actualTypeArguments = actualTypeArguments;
        this.ownerType = ownerType;
        this.rawType = rawType;
    }

    public Type[] getActualTypeArguments() {
        return actualTypeArguments;
    }

    public Type getOwnerType() {
        return ownerType;
    }

    public Type getRawType() {
        return rawType;
    }

}