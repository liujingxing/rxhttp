package rxhttp.wrapper.converter;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;

import java.io.IOException;
import java.lang.reflect.Type;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import rxhttp.RxHttpPlugins;
import rxhttp.wrapper.callback.JsonConverter;

/**
 * User: ljx
 * Date: 2019-11-24
 * Time: 15:34
 */
public class JacksonConverter implements JsonConverter {

    private final ObjectMapper mapper;
    private final MediaType contentType;

    private JacksonConverter(ObjectMapper mapper, MediaType contentType) {
        this.mapper = mapper;
        this.contentType = contentType;
    }

    public static JacksonConverter create() {
        return create(new ObjectMapper());
    }

    public static JacksonConverter create(ObjectMapper mapper) {
        return create(mapper, JsonConverter.MEDIA_TYPE);
    }

    public static JacksonConverter create(ObjectMapper mapper, MediaType contentType) {
        if (mapper == null) throw new NullPointerException("mapper == null");
        return new JacksonConverter(mapper, contentType);
    }

    @Override
    public <T> T convert(ResponseBody body, Type type, boolean needDecodeResult) throws IOException {
        JavaType javaType = mapper.getTypeFactory().constructType(type);
        ObjectReader reader = mapper.readerFor(javaType);
        try {
            String result = body.string();
            if (needDecodeResult) {
                result = RxHttpPlugins.onResultDecoder(result);
            }
            T t = reader.readValue(result);
            if (t == null) {
                throw new IllegalStateException("JacksonConverter Could not deserialize body as " + type);
            }
            return t;
        } finally {
            body.close();
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    public <T> RequestBody convert(T value) throws IOException {
        JavaType javaType = mapper.getTypeFactory().constructType(value.getClass());
        ObjectWriter writer = mapper.writerFor(javaType);
        byte[] bytes = writer.writeValueAsBytes(value);
        return RequestBody.create(contentType, bytes);
    }
}
