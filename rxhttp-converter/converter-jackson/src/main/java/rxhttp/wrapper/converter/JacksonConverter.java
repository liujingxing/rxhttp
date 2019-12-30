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
import rxhttp.wrapper.callback.IConverter;

/**
 * User: ljx
 * Date: 2019-11-24
 * Time: 15:34
 */
public class JacksonConverter implements IConverter {

    private static final MediaType MEDIA_TYPE = MediaType.get("application/json; charset=UTF-8");

    public static JacksonConverter create() {
        return create(new ObjectMapper());
    }

    @SuppressWarnings("ConstantConditions") // Guarding public API nullability.
    public static JacksonConverter create(ObjectMapper mapper) {
        if (mapper == null) throw new NullPointerException("mapper == null");
        return new JacksonConverter(mapper);
    }

    private final ObjectMapper mapper;

    private JacksonConverter(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public <T> T convert(ResponseBody body, Type type, boolean onResultDecoder) throws IOException {
        JavaType javaType = mapper.getTypeFactory().constructType(type);
        ObjectReader reader = mapper.readerFor(javaType);
        try {
            return reader.readValue(body.charStream());
        } finally {
            body.close();
        }
    }

    @Override
    public <T> RequestBody convert(T value) throws IOException {
        JavaType javaType = mapper.getTypeFactory().constructType(value.getClass());
        ObjectWriter writer = mapper.writerFor(javaType);
        byte[] bytes = writer.writeValueAsBytes(value);
        return RequestBody.create(MEDIA_TYPE, bytes);
    }
}
