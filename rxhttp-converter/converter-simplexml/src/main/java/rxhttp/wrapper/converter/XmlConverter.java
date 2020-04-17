package rxhttp.wrapper.converter;


import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

import java.io.IOException;
import java.lang.reflect.Type;

import okhttp3.ResponseBody;
import rxhttp.wrapper.annotations.NonNull;
import rxhttp.wrapper.callback.IConverter;

/**
 * User: ljx
 * Date: 2019-11-21
 * Time: 22:19
 */
public class XmlConverter implements IConverter {

    private final Serializer serializer;
    private final boolean strict;

    public static XmlConverter create() {
        return create(new Persister());
    }

    public static XmlConverter create(Serializer serializer) {
        return new XmlConverter(serializer, true);
    }

    public static XmlConverter createNonStrict() {
        return createNonStrict(new Persister());
    }

    @SuppressWarnings("ConstantConditions") // Guarding public API nullability.
    public static XmlConverter createNonStrict(Serializer serializer) {
        if (serializer == null) throw new NullPointerException("serializer == null");
        return new XmlConverter(serializer, false);
    }

    private XmlConverter(Serializer serializer, boolean strict) {
        this.serializer = serializer;
        this.strict = strict;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T convert(@NonNull ResponseBody body, @NonNull Type type, boolean onResultDecoder) throws IOException {
        if (!(type instanceof Class)) return null;
        Class<T> cls = (Class<T>) type;
        try {
            T read = serializer.read(cls, body.charStream(), strict);
            if (read == null) {
                throw new IllegalStateException("Could not deserialize body as " + cls);
            }
            return read;
        } catch (RuntimeException | IOException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            body.close();
        }
    }
}
