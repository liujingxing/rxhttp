package rxhttp.wrapper.entity

import rxhttp.wrapper.utils.javaObjectType
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

/**
 * User: ljx
 * Date: 2018/10/23
 * Time: 09:36
 */
class ParameterizedTypeImpl private constructor(
    private val ownerType: Type?,
    private val rawType: Type,
    private vararg val actualTypeArguments: Type
) : ParameterizedType {

    private constructor(rawType: Type, actualType: Type) : this(null, rawType, actualType)

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
         * get(List.class, List.class, String.class) 等价于 List<List<String>>
         *
         * @param rawType Type
         * @param types   Type数组
         * @return ParameterizedTypeImpl
         */
        @JvmStatic
        operator fun get(rawType: Type, vararg types: Type): ParameterizedTypeImpl {
            val size = types.size
            var lastType = types[size - 1].javaObjectType
            for (i in size - 2 downTo 0) { //The tail starts traversing
                lastType = ParameterizedTypeImpl(types[i], lastType)
            }
            return ParameterizedTypeImpl(rawType, lastType)
        }

        //适用于多个泛型参数的类
        //getParameterized(Map.Class, String.class, Integer.class)  等价于  Map<String, Integer>
        @JvmStatic
        fun getParameterized(rawType: Type, vararg actualTypeArguments: Type): Type {
            val types = actualTypeArguments.map { it.javaObjectType }
            return ParameterizedTypeImpl(null, rawType, *types.toTypedArray())
        }
    }
}