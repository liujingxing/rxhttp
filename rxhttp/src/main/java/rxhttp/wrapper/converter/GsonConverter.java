package rxhttp.wrapper.converter;


import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.lang.reflect.Type;

import kotlin.text.Charsets;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import okio.Buffer;
import rxhttp.RxHttpPlugins;
import rxhttp.wrapper.annotations.NonNull;
import rxhttp.wrapper.callback.JsonConverter;
import rxhttp.wrapper.utils.GsonUtil;

/**
 * RxHttp 默认的转换器，通过Gson转换数据
 * User: ljx
 * Date: 2019-11-21
 * Time: 22:19
 */
public class GsonConverter implements JsonConverter {

    private final Gson gson;
    private final MediaType contentType;

    private GsonConverter(Gson gson, MediaType contentType) {
        this.gson = gson;
        this.contentType = contentType;
    }

    public static GsonConverter create() {
        return create(GsonUtil.buildGson());
    }

    public static GsonConverter create(Gson gson) {
        return create(gson, JsonConverter.MEDIA_TYPE);
    }

    public static GsonConverter create(Gson gson, MediaType contentType) {
        if (gson == null) throw new NullPointerException("gson == null");
        return new GsonConverter(gson, contentType);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T convert(ResponseBody body, @NonNull Type type, boolean needDecodeResult) throws IOException {
        try {
            String result = body.string();
            if (needDecodeResult) {
                result = RxHttpPlugins.onResultDecoder(result);
            }
            if (type == String.class) return (T) result;
            T t = gson.fromJson(result, type);
            if (t == null) {
                throw new IllegalStateException("GsonConverter Could not deserialize body as " + type);
            }
            return t;
        } finally {
            body.close();
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> RequestBody convert(T value) throws IOException {
        TypeToken<T> typeToken = (TypeToken<T>) TypeToken.get(value.getClass());
        TypeAdapter<T> adapter = this.gson.getAdapter(typeToken);
        Buffer buffer = new Buffer();
        Writer writer = new OutputStreamWriter(buffer.outputStream(), Charsets.UTF_8);
        JsonWriter jsonWriter = gson.newJsonWriter(writer);
        adapter.write(jsonWriter, value);
        jsonWriter.close();
        return RequestBody.create(contentType, buffer.readByteString());
    }
}
