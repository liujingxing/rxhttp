@file:JvmName("TypeUtil")

package rxhttp.wrapper.utils

import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type
import kotlin.reflect.javaType
import kotlin.reflect.typeOf


/**
 * 获取泛型类型
 *
 * @param clazz 类类型
 * @param index 第几个泛型
 * @return Type
 */
fun getActualTypeParameter(clazz: Class<*>, index: Int): Type {
    return getActualTypeParameters(clazz)[index]
}

/**
 * 获取泛型类型数组
 *
 * @param clazz 类类型
 * @return Array<Type>
 */
fun getActualTypeParameters(clazz: Class<*>): Array<Type> {
    val superclass = clazz.genericSuperclass as? ParameterizedType
        ?: throw RuntimeException("Missing type parameter.")
    return superclass.actualTypeArguments
}

@OptIn(ExperimentalStdlibApi::class)
inline fun <reified T> javaTypeOf(): Type = typeOf<T>().javaType

internal val Type.javaObjectType: Type
    get() {
        val type = this
        if (type !is Class<*> || !type.isPrimitive) return type
        return when (type.name) {
            "boolean" -> Boolean::class.java
            "char" -> Character::class.java
            "byte" -> Byte::class.java
            "short" -> Short::class.java
            "int" -> Integer::class.java
            "float" -> Float::class.java
            "long" -> Long::class.java
            "double" -> Double::class.java
            "void" -> Void::class.java
            else -> type
        }
    }