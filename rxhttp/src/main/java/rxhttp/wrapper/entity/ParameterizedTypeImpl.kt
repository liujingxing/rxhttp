package rxhttp.wrapper.entity

import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

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
            val size = types.size
            var lastType = types[size - 1]
            for (i in size - 2 downTo 0) { //The tail starts traversing
                lastType = ParameterizedTypeImpl(types[i], lastType)
            }
            return ParameterizedTypeImpl(rawType, lastType)
        }

        //适用于多个泛型参数的类
        @JvmStatic
        fun getParameterized(rawType: Type, vararg actualTypeArguments: Type) =
            ParameterizedTypeImpl(null, rawType, *actualTypeArguments)
    }
}