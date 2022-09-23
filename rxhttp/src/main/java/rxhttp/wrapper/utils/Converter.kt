@file:JvmName("Converter")

package rxhttp.wrapper.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import okhttp3.Response
import okhttp3.ResponseBody
import rxhttp.Platform
import rxhttp.wrapper.OkHttpCompat
import rxhttp.wrapper.callback.Converter
import rxhttp.wrapper.entity.ParameterizedTypeImpl
import java.io.IOException
import java.lang.reflect.Type
import kotlin.reflect.KClass

/**
 * User: ljx
 * Date: 2020/8/15
 * Time: 11:48
 */

@Throws(IOException::class)
fun <T> Response.convertTo(rawType: KClass<*>, vararg types: Type): T {
    return convertTo(rawType.java, *types)
}

@Throws(IOException::class)
fun <T> Response.convertTo(rawType: Type, vararg types: Type): T {
    return convert(ParameterizedTypeImpl.get(rawType, *types))
}

@Throws(IOException::class)
fun <T> Response.convertToParameterized(rawType: KClass<*>, vararg actualTypeArguments: Type): T {
    return convertToParameterized(rawType.java, *actualTypeArguments)
}

@Throws(IOException::class)
fun <T> Response.convertToParameterized(rawType: Type, vararg actualTypeArguments: Type): T {
    return convert(ParameterizedTypeImpl.getParameterized(rawType, *actualTypeArguments))
}

@Suppress("UNCHECKED_CAST")
@Throws(IOException::class)
fun <T> Response.convert(type: Type): T {
    val body = OkHttpCompat.throwIfFail(this)
    LogUtil.log(this, null)
    return if (type === ResponseBody::class.java) {
        body.use { OkHttpCompat.buffer(it) as T }
    } else if (Platform.get().isAndroid && type === Bitmap::class.java) {
        BitmapFactory.decodeStream(body.byteStream()) as T
    } else {
        val needDecodeResult = OkHttpCompat.needDecodeResult(this)
        val converter = OkHttpCompat.request(this).tag(Converter::class.java)
        converter?.convert(body, type, needDecodeResult)
            ?: throw IllegalStateException("Converter Could not deserialize body as $type")
    }
}