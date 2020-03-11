package rxhttp.wrapper.entity

import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type
import java.util.*

/**
 * User: ljx
 * Date: 2018/10/23
 * Time: 09:36
 */
class ParameterizedTypeImpl(
    private val ownerType: Type?,
    private val rawType: Type,
    private vararg val actualTypeArguments: Type
) : ParameterizedType {

    //适用于单个泛型参数的类
    constructor(rawType: Type, actualType: Type) : this(null, rawType, actualType)

    override fun getActualTypeArguments(): Array<out Type> {
        return actualTypeArguments
    }

    override fun getOwnerType(): Type? {
        return ownerType
    }

    override fun getRawType(): Type {
        return rawType
    }

    companion object {
        /**
         * 本方法仅使用于单个泛型参数的类
         * 根据types数组，确定具体的泛型类型
         * List里面是List  对应  get(List.class, List.class, String.class)
         *
         * @param rawType Type
         * @param types   Type数组
         * @return ParameterizedTypeImpl
         */
        @JvmStatic
        operator fun get(rawType: Type, vararg types: Type): ParameterizedTypeImpl {
            val length = types.size
            if (length > 1) {
                val parameterizedType: Type = ParameterizedTypeImpl(types[length - 2], types[length - 1])
                val newTypes = Arrays.copyOf(types, length - 1)
                newTypes[newTypes.size - 1] = parameterizedType
                return get(rawType, *newTypes)
            }
            return ParameterizedTypeImpl(rawType, types[0])
        }

        //适用于多个泛型参数的类
        @JvmStatic
        fun getParameterized(rawType: Type, vararg actualTypeArguments: Type): ParameterizedTypeImpl {
            return ParameterizedTypeImpl(null, rawType, *actualTypeArguments)
        }
    }
}