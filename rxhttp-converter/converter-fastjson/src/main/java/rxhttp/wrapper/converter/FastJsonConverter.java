package rxhttp.wrapper.converter;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.parser.ParserConfig;
import com.alibaba.fastjson.serializer.SerializeConfig;

import java.io.IOException;
import java.lang.reflect.Type;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import rxhttp.wrapper.annotations.NonNull;
import rxhttp.wrapper.callback.IConverter;

/**
 * User: ljx
 * Date: 2019-11-24
 * Time: 15:34
 */
public class FastJsonConverter implements IConverter {

    private static final MediaType MEDIA_TYPE = MediaType.get("application/json; charset=UTF-8");

    private SerializeConfig serializeConfig;
    private ParserConfig parserConfig;

    public static FastJsonConverter create() {
        return create(ParserConfig.getGlobalInstance(), SerializeConfig.getGlobalInstance());
    }

    public static FastJsonConverter create(@NonNull ParserConfig parserConfig) {
        return create(parserConfig, SerializeConfig.getGlobalInstance());
    }

    public static FastJsonConverter create(@NonNull SerializeConfig serializeConfig) {
        return create(ParserConfig.getGlobalInstance(), serializeConfig);
    }

    public static FastJsonConverter create(@NonNull ParserConfig parserConfig, @NonNull SerializeConfig serializeConfig) {
        if (parserConfig == null) throw new NullPointerException("parserConfig == null");
        if (serializeConfig == null) throw new NullPointerException("serializeConfig == null");
        return new FastJsonConverter(parserConfig, serializeConfig);
    }

    private FastJsonConverter(ParserConfig parserConfig, SerializeConfig serializeConfig) {
        this.parserConfig = parserConfig;
        this.serializeConfig = serializeConfig;
    }

    @Override
    public <T> T convert(ResponseBody body, Type type,boolean onResultDecoder) throws IOException {
        try {
            return JSON.parseObject(body.string(), type, parserConfig);
        } finally {
            body.close();
        }
    }

    @Override
    public <T> RequestBody convert(T value) throws IOException {
        return RequestBody.create(MEDIA_TYPE, JSON.toJSONBytes(value, serializeConfig));
    }
}
