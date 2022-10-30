package rxhttp.wrapper.utils

import okhttp3.Response
import java.io.IOException
import java.lang.reflect.Type
import kotlin.reflect.KClass

/**
 * User: ljx
 * Date: 2020/8/15
 * Time: 11:48
 */
@Throws(IOException::class)
fun <T> Response.convertTo(rawType: KClass<*>, vararg types: Type): T =
    convertTo(rawType.java, *types)

@Throws(IOException::class)
fun <T> Response.convertTo(rawType: Type, vararg types: Type): T =
    Converter.convertTo(this, rawType, *types)

@Throws(IOException::class)
fun <T> Response.convertToParameterized(rawType: KClass<*>, vararg actualTypes: Type): T =
    convertToParameterized(rawType.java, *actualTypes)

@Throws(IOException::class)
fun <T> Response.convertToParameterized(rawType: Type, vararg actualTypes: Type): T =
    Converter.convertToParameterized(this, rawType, *actualTypes)

@Throws(IOException::class)
fun <T> Response.convert(type: Type): T = Converter.convert(this, type)