@file:JvmName("KotlinExtensions")

package rxhttp.wrapper.utils

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import kotlinx.coroutines.suspendCancellableCoroutine
import okhttp3.*
import rxhttp.wrapper.entity.UriRequestBody
import rxhttp.wrapper.parse.Parser
import java.io.FileNotFoundException
import java.io.IOException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * User: ljx
 * Date: 2020/9/13
 * Time: 22:41
 */

@JvmOverloads
fun Uri.asRequestBody(
    context: Context,
    contentType: MediaType? = null
): RequestBody = UriRequestBody(context, this, contentType)

@JvmOverloads
fun Uri.asPart(
    context: Context,
    key: String,
    filename: String? = null,
    contentType: MediaType? = null
): MultipartBody.Part {
    val newFilename = filename ?: displayName(context)
    return MultipartBody.Part.createFormData(key, newFilename, asRequestBody(context, contentType))
}

//return The size of the media item, return -1 if does not exist
fun Uri?.length(context: Context): Long {
    if (this == null) return -1L
    val fileDescriptor = try {
        context.contentResolver.openFileDescriptor(this, "r")
    } catch (e: FileNotFoundException) {
        null
    }
    return fileDescriptor?.statSize ?: -1L
}

internal fun Uri.displayName(context: Context): String? {
    return getColumnValue(context.contentResolver, MediaStore.MediaColumns.DISPLAY_NAME)
}

//Return the value of the specified column，return null if does not exist
internal fun Uri.getColumnValue(contentResolver: ContentResolver, columnName: String): String? {
    return contentResolver.query(this, arrayOf(columnName),
        null, null, null)?.use {
        if (it.moveToFirst()) it.getString(0) else null
    }
}

internal suspend fun Call.await(): Response {
    return suspendCancellableCoroutine { continuation ->
        continuation.invokeOnCancellation {
            cancel()
        }
        enqueue(object : Callback {

            override fun onResponse(call: Call, response: Response) {
                continuation.resume(response)
            }

            override fun onFailure(call: Call, e: IOException) {
                continuation.resumeWithException(e)
            }
        })
    }
}

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