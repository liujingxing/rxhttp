package rxhttp.wrapper.converter;


import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.charset.Charset;

import io.reactivex.annotations.NonNull;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import okio.Buffer;
import rxhttp.RxHttpPlugins;
import rxhttp.wrapper.callback.IConverter;
import rxhttp.wrapper.utils.GsonUtil;

/**
 * RxHttp 默认的转换器，通过Gson转换数据
 * User: ljx
 * Date: 2019-11-21
 * Time: 22:19
 */
public class GsonConverter implements IConverter {

    private static final MediaType MEDIA_TYPE = MediaType.get("application/json; charset=UTF-8");
    private static final Charset UTF_8 = Charset.forName("UTF-8");

    private final Gson gson;

    /**
     * Create an instance using a default {@link Gson} instance for conversion. Encoding to JSON and
     * decoding from JSON (when no charset is specified by a header) will use UTF-8.
     */
    public static GsonConverter create() {
        return create(GsonUtil.buildGson());
    }

    /**
     * Create an instance using {@code gson} for conversion. Encoding to JSON and
     * decoding from JSON (when no charset is specified by a header) will use UTF-8.
     */
    // Guarding public API nullability.
    public static GsonConverter create(Gson gson) {
        if (gson == null) throw new NullPointerException("gson == null");
        return new GsonConverter(gson);
    }

    private GsonConverter(Gson gson) {
        this.gson = gson;
    }

    @NonNull
    @Override
    public <T> T convert(ResponseBody body, @NonNull Type type, boolean onResultAssembly) throws IOException {
        try {
            String result = body.string();
            if (onResultAssembly) {
                result = RxHttpPlugins.onResultDecoder(result);
            }
            return gson.fromJson(result, type);
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
        Writer writer = new OutputStreamWriter(buffer.outputStream(), UTF_8);
        JsonWriter jsonWriter = gson.newJsonWriter(writer);
        adapter.write(jsonWriter, value);
        jsonWriter.close();
        return RequestBody.create(MEDIA_TYPE, buffer.readByteString());
    }
}
