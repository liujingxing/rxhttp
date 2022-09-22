package rxhttp.wrapper.utils

import kotlinx.coroutines.suspendCancellableCoroutine
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import rxhttp.wrapper.parse.Parser
import java.io.Closeable
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.reflect.javaType
import kotlin.reflect.typeOf

/**
 * User: ljx
 * Date: 2022/9/22
 * Time: 23:47
 */
internal suspend fun <T> Call.await(parser: Parser<T>): T {
    return suspendCancellableCoroutine { continuation ->
        continuation.invokeOnCancellation {
            cancel()
        }
        enqueue(object : Callback {

            override fun onResponse(call: Call, response: Response) {
                try {
                    continuation.resume(parser.onParse(response))
                } catch (t: Throwable) {
                    continuation.resumeWithException(t)
                }
            }

            override fun onFailure(call: Call, e: IOException) {
                continuation.resumeWithException(e)
            }
        })
    }
}

private const val LENGTH_BYTE = 8 * 1024

@Throws(IOException::class)
internal fun InputStream.writeTo(
    outStream: OutputStream,
    progress: ((Long) -> Unit)? = null
): Boolean {
    return try {
        val bytes = ByteArray(LENGTH_BYTE)
        var totalReadLength: Long = 0
        var readLength: Int
        while (read(bytes, 0, bytes.size).also { readLength = it } != -1) {
            outStream.write(bytes, 0, readLength)
            progress?.apply {
                totalReadLength += readLength
                invoke(totalReadLength)
            }
        }
        true
    } finally {
        close(this, outStream)
    }
}

internal fun close(vararg closeables: Closeable?) {
    for (closeable in closeables) {
        if (closeable == null) continue
        try {
            closeable.close()
        } catch (ignored: IOException) {
        }
    }
}

fun getActualTypeParameters(clazz: Class<*>): Array<Type> {
    return (clazz.genericSuperclass as? ParameterizedType)?.actualTypeArguments
        ?: throw RuntimeException("Missing type parameter.")
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