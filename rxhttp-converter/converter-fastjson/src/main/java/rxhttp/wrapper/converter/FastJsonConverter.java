package rxhttp.wrapper.converter;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.parser.ParserConfig;
import com.alibaba.fastjson.serializer.SerializeConfig;

import org.jetbrains.annotations.NotNull;

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
public class FastJsonConverter implements JsonConverter {

    private final SerializeConfig serializeConfig;
    private final ParserConfig parserConfig;
    private final MediaType contentType;

    private FastJsonConverter(ParserConfig parserConfig, SerializeConfig serializeConfig, MediaType contentType) {
        this.parserConfig = parserConfig;
        this.serializeConfig = serializeConfig;
        this.contentType = contentType;
    }

    public static FastJsonConverter create() {
        return create(ParserConfig.getGlobalInstance(), SerializeConfig.getGlobalInstance());
    }

    public static FastJsonConverter create(ParserConfig parserConfig) {
        return create(parserConfig, SerializeConfig.getGlobalInstance());
    }

    public static FastJsonConverter create(SerializeConfig serializeConfig) {
        return create(ParserConfig.getGlobalInstance(), serializeConfig);
    }

    public static FastJsonConverter create(ParserConfig parserConfig, SerializeConfig serializeConfig) {
        return create(parserConfig, serializeConfig, JsonConverter.MEDIA_TYPE);
    }

    public static FastJsonConverter create(ParserConfig parserConfig, SerializeConfig serializeConfig, MediaType contentType) {
        if (parserConfig == null) throw new NullPointerException("parserConfig == null");
        if (serializeConfig == null) throw new NullPointerException("serializeConfig == null");
        return new FastJsonConverter(parserConfig, serializeConfig, contentType);
    }

    @NotNull
    @SuppressWarnings("unchecked")
    @Override
    public <T> T convert(@NotNull ResponseBody body, @NotNull Type type, boolean needDecodeResult) throws IOException {
        try {
            String result = body.string();
            if (needDecodeResult) {
                result = RxHttpPlugins.onResultDecoder(result);
            }
            if (type == String.class) {
                return (T) result;
            }
            T t = JSON.parseObject(result, type, parserConfig);
            if (t == null) {
                throw new IllegalStateException("FastJsonConverter Could not deserialize body as " + type);
            }
            return t;
        } finally {
            body.close();
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    public <T> RequestBody convert(T value) throws IOException {
        return RequestBody.create(contentType, JSON.toJSONBytes(value, serializeConfig));
    }
}
