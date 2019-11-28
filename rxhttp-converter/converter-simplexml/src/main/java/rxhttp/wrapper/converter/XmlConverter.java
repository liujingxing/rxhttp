package rxhttp.wrapper.converter;


import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

import java.io.IOException;
import java.lang.reflect.Type;

import io.reactivex.annotations.NonNull;
import okhttp3.ResponseBody;
import rxhttp.wrapper.callback.IConverter;

/**
 * User: ljx
 * Date: 2019-11-21
 * Time: 22:19
 */
public class XmlConverter implements IConverter {

    private final Serializer serializer;
    private final boolean strict;

    /**
     * Create an instance using a default {@link Persister} instance for conversion.
     */
    public static XmlConverter create() {
        return create(new Persister());
    }

    /**
     * Create an instance using {@code serializer} for conversion.
     */
    public static XmlConverter create(Serializer serializer) {
        return new XmlConverter(serializer, true);
    }

    /**
     * Create an instance using a default {@link Persister} instance for non-strict conversion.
     */
    public static XmlConverter createNonStrict() {
        return createNonStrict(new Persister());
    }

    /**
     * Create an instance using {@code serializer} for non-strict conversion.
     */
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
