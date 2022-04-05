@file:JvmName("Converter")

package rxhttp.wrapper.utils

import okhttp3.Response
import rxhttp.wrapper.OkHttpCompat
import rxhttp.wrapper.entity.ParameterizedTypeImpl
import rxhttp.wrapper.exception.ExceptionHelper
import java.io.IOException
import java.lang.reflect.Type
import kotlin.reflect.KClass

/**
 * User: ljx
 * Date: 2020/8/15
 * Time: 11:48
 */

@Throws(IOException::class)
fun <R> Response.convertTo(rawType: KClass<*>, vararg types: Type): R {
    return convertTo(rawType.java, *types)
}

@Throws(IOException::class)
fun <R> Response.convertTo(rawType: Type, vararg types: Type): R {
    return convert(ParameterizedTypeImpl.get(rawType, *types))
}

@Throws(IOException::class)
fun <R> Response.convertToParameterized(rawType: KClass<*>, vararg actualTypeArguments: Type): R {
    return convertToParameterized(rawType.java, *actualTypeArguments)
}

@Throws(IOException::class)
fun <R> Response.convertToParameterized(rawType: Type, vararg actualTypeArguments: Type): R {
    return convert(ParameterizedTypeImpl.getParameterized(rawType, *actualTypeArguments))
}

@Throws(IOException::class)
fun <R> Response.convert(type: Type): R {
    val body = ExceptionHelper.throwIfFatal(this)
    val needDecodeResult = OkHttpCompat.needDecodeResult(this)
    LogUtil.log(this, null)
    val converter = OkHttpCompat.getConverter(this)
    return converter?.convert(body, type, needDecodeResult)
        ?: throw IllegalStateException("Converter Could not deserialize body as $type")
}