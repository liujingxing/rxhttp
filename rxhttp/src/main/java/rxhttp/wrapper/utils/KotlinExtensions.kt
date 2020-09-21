@file:JvmName("KotlinExtensions")

package rxhttp.wrapper.utils

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import android.webkit.MimeTypeMap
import kotlinx.coroutines.suspendCancellableCoroutine
import okhttp3.*
import rxhttp.wrapper.entity.UriRequestBody
import rxhttp.wrapper.parse.Parser
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
    name: String,
    contentType: MediaType? = null
): MultipartBody.Part {
    val contentResolver = context.contentResolver
    var fileName = getColumnValue(contentResolver, MediaStore.MediaColumns.DISPLAY_NAME)
    if (fileName == null) {
        val currentTime = System.currentTimeMillis().toString()
        val mimeType = contentResolver.getType(this)
        val fileSuffix = MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType)
        fileName = "$currentTime.$fileSuffix"
    }
    return MultipartBody.Part.createFormData(name, fileName, asRequestBody(context, contentType))
}

//return The size of the media item, return -1 if does not exist
internal fun Uri.length(context: Context): Long {
    return context.contentResolver.query(this, arrayOf(MediaStore.MediaColumns.SIZE),
        null, null, null).use {
        if (it.moveToFirst()) it.getLong(0) else -1L
    }
}

//Return the value of the specified column，return null if does not exist
internal fun Uri.getColumnValue(contentResolver: ContentResolver, columnName: String): String? {
    return contentResolver.query(this, arrayOf(columnName),
        null, null, null).use {
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