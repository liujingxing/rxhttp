@file:JvmName("Converter")
package rxhttp.wrapper.utils

import okhttp3.Response
import rxhttp.wrapper.OkHttpCompat
import rxhttp.wrapper.exception.ExceptionHelper
import java.io.IOException
import java.lang.reflect.Type

/**
 * User: ljx
 * Date: 2020/8/15
 * Time: 11:48
 */
@Throws(IOException::class)
fun <R> Response.convert(type: Type): R {
    val body = ExceptionHelper.throwIfFatal(this)
    val onResultDecoder = OkHttpCompat.isOnResultDecoder(this)
    LogUtil.log(this, onResultDecoder, null)
    val converter = OkHttpCompat.getConverter(this)
    return converter!!.convert(body, type, onResultDecoder)
}