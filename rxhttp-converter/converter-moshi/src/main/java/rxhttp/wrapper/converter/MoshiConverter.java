package rxhttp.wrapper.converter;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.JsonDataException;
import com.squareup.moshi.JsonReader;
import com.squareup.moshi.JsonWriter;
import com.squareup.moshi.Moshi;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import okio.Buffer;
import okio.BufferedSource;
import okio.ByteString;
import rxhttp.RxHttpPlugins;
import rxhttp.wrapper.callback.IConverter;

/**
 * User: ljx
 * Date: 2020/08/06
 * Time: 15:34
 */
public class MoshiConverter implements IConverter {

    private static final ByteString UTF8_BOM = ByteString.decodeHex("EFBBBF");
    private static final MediaType MEDIA_TYPE = MediaType.get("application/json; charset=UTF-8");

    private final Moshi moshi;
    private final boolean lenient;
    private final boolean failOnUnknown;
    private final boolean serializeNulls;

    private MoshiConverter(Moshi moshi, boolean lenient, boolean failOnUnknown, boolean serializeNulls) {
        this.moshi = moshi;
        this.lenient = lenient;
        this.failOnUnknown = failOnUnknown;
        this.serializeNulls = serializeNulls;
    }

    /**
     * Create an instance using a default {@link Moshi} instance for conversion.
     */
    public static MoshiConverter create() {
        return create(new Moshi.Builder().build());
    }

    /**
     * Create an instance using {@code moshi} for conversion.
     */
    public static MoshiConverter create(Moshi moshi) {
        if (moshi == null) throw new NullPointerException("moshi == null");
        return new MoshiConverter(moshi, false, false, false);
    }

    /**
     * Return a new factory which uses {@linkplain JsonAdapter#lenient() lenient} adapters.
     */
    public MoshiConverter asLenient() {
        return new MoshiConverter(moshi, true, failOnUnknown, serializeNulls);
    }

    /**
     * Return a new factory which uses {@link JsonAdapter#failOnUnknown()} adapters.
     */
    public MoshiConverter failOnUnknown() {
        return new MoshiConverter(moshi, lenient, true, serializeNulls);
    }

    /**
     * Return a new factory which includes null values into the serialized JSON.
     */
    public MoshiConverter withNullSerialization() {
        return new MoshiConverter(moshi, lenient, failOnUnknown, true);
    }

    @Override
    public <T> T convert(ResponseBody body, Type type, boolean needDecodeResult) throws IOException {
        JsonAdapter<T> adapter = moshi.adapter(type);
        if (lenient) {
            adapter = adapter.lenient();
        }
        if (failOnUnknown) {
            adapter = adapter.failOnUnknown();
        }
        if (serializeNulls) {
            adapter = adapter.serializeNulls();
        }
        BufferedSource source;
        if (needDecodeResult) {
            String decodeResult = RxHttpPlugins.onResultDecoder(body.string());
            source = new Buffer().writeUtf8(decodeResult);
        } else {
            source = body.source();
        }
        try {
            // Moshi has no document-level API so the responsibility of BOM skipping falls to whatever
            // is delegating to it. Since it's a UTF-8-only library as well we only honor the UTF-8 BOM.
            if (source.rangeEquals(0, UTF8_BOM)) {
                source.skip(UTF8_BOM.size());
            }
            JsonReader reader = JsonReader.of(source);
            T result = adapter.fromJson(reader);
            if (reader.peek() != JsonReader.Token.END_DOCUMENT) {
                throw new JsonDataException("JSON document was not fully consumed.");
            }
            return result;
        } finally {
            body.close();
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> RequestBody convert(T value) throws IOException {
        Class<T> clazz;
        if (value instanceof Map) {
            clazz = (Class<T>) Map.class;
        } else if (value instanceof List) {
            clazz = (Class<T>) List.class;
        } else {
            clazz = (Class<T>) value.getClass();
        }
        JsonAdapter<T> adapter = (JsonAdapter<T>) moshi.adapter(clazz);
        if (lenient) {
            adapter = adapter.lenient();
        }
        if (failOnUnknown) {
            adapter = adapter.failOnUnknown();
        }
        if (serializeNulls) {
            adapter = adapter.serializeNulls();
        }
        Buffer buffer = new Buffer();
        JsonWriter writer = JsonWriter.of(buffer);
        adapter.toJson(writer, value);
        return RequestBody.create(MEDIA_TYPE, buffer.readByteString());
    }
}