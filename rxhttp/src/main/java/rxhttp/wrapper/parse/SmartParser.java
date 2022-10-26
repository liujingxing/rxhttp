package rxhttp.wrapper.parse;

import java.io.IOException;
import java.lang.reflect.Type;

import okhttp3.Response;
import rxhttp.wrapper.utils.Converter;
import rxhttp.wrapper.utils.TypeUtil;

/**
 * Convert [Response] to [T]
 * User: ljx
 * Date: 2018/10/23
 * Time: 13:49
 */
public class SmartParser<T> extends TypeParser<T> {

    protected SmartParser() {
    }

    public SmartParser(Type type) {
        super(type);
    }

    @Override
    public T onParse(Response response) throws IOException {
        return Converter.convert(response, types[0]);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static <T> Parser<T> wrap(Type type) {
        Type actualType = TypeUtil.getActualType(type);
        if (actualType == null) actualType = type;
        SmartParser smartParser = new SmartParser(actualType);
        return actualType == type ? smartParser : new OkResponseParser(smartParser);
    }
}
