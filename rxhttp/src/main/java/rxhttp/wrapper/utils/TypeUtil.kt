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
            "boolean" -> java.lang.Boolean::class.java
            "char" -> java.lang.Character::class.java
            "byte" -> java.lang.Byte::class.java
            "short" -> java.lang.Short::class.java
            "int" -> java.lang.Integer::class.java
            "float" -> java.lang.Float::class.java
            "long" -> java.lang.Long::class.java
            "double" -> java.lang.Double::class.java
            "void" -> Void::class.java
            else -> type
        }
    }