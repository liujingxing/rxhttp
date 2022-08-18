@file:JvmName("KotlinSerializationConverter")

package rxhttp.wrapper.converter

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.StringFormat
import kotlinx.serialization.serializer
import okhttp3.MediaType
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody
import rxhttp.RxHttpPlugins
import rxhttp.wrapper.callback.JsonConverter
import rxhttp.wrapper.utils.JSONStringer
import java.lang.reflect.Type

@OptIn(ExperimentalSerializationApi::class)
class SerializationConverter(
    private val format: StringFormat,
    private val contentType: MediaType?,
) : JsonConverter {

    @Suppress("UNCHECKED_CAST")
    override fun <T> convert(
        body: ResponseBody,
        type: Type,
        needDecodeResult: Boolean
    ): T {
        var json = body.string()
        if (needDecodeResult) {
            json = RxHttpPlugins.onResultDecoder(json)
        }
        if (type == String::class.java) {
            return json as T
        }
        val serializer = format.serializersModule.serializer(type)
        return format.decodeFromString(serializer, json) as T
    }

    override fun <T : Any> convert(value: T): RequestBody {
        val json = when (value) {
            is Collection<*> -> {
                JSONStringer().setSerializeCallback {
                    val serializer = format.serializersModule.serializer(it.javaClass)
                    format.encodeToString(serializer, it)
                }.write(value).toString()
            }
            is Map<*, *> -> {
                JSONStringer().setSerializeCallback {
                    val serializer = format.serializersModule.serializer(it.javaClass)
                    format.encodeToString(serializer, it)
                }.write(value).toString()
            }
            else -> {
                val serializer = format.serializersModule.serializer(value::class.java)
                format.encodeToString(serializer, value)
            }
        }
        return json.toRequestBody(contentType)
    }
}

@JvmOverloads
@JvmName("create")
fun StringFormat.asConverter(contentType: MediaType? = JsonConverter.MEDIA_TYPE) =
    SerializationConverter(this, contentType)