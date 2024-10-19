package rxhttp.wrapper.converter;

import org.jetbrains.annotations.NotNull;
import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.lang.reflect.Type;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import okio.Buffer;
import rxhttp.RxHttpPlugins;
import rxhttp.wrapper.callback.IConverter;

/**
 * User: ljx
 * Date: 2019-11-21
 * Time: 22:19
 */
public class XmlConverter implements IConverter {
    private static final MediaType MEDIA_TYPE = MediaType.get("application/xml; charset=UTF-8");
    private static final String CHARSET = "UTF-8";

    private final Serializer serializer;
    private final boolean strict;
    private final MediaType contentType;

    private XmlConverter(Serializer serializer, boolean strict, MediaType contentType) {
        this.serializer = serializer;
        this.strict = strict;
        this.contentType = contentType;
    }

    public static XmlConverter create() {
        return create(new Persister());
    }

    public static XmlConverter create(Serializer serializer) {
        return create(serializer, MEDIA_TYPE);
    }

    public static XmlConverter create(Serializer serializer, MediaType contentType) {
        return new XmlConverter(serializer, true, contentType);
    }

    public static XmlConverter createNonStrict() {
        return createNonStrict(new Persister());
    }

    public static XmlConverter createNonStrict(Serializer serializer) {
        return createNonStrict(serializer, MEDIA_TYPE);
    }

    // Guarding public API nullability.
    public static XmlConverter createNonStrict(Serializer serializer, MediaType contentType) {
        if (serializer == null) throw new NullPointerException("serializer == null");
        return new XmlConverter(serializer, false, contentType);
    }

    @NotNull
    @SuppressWarnings("unchecked")
    @Override
    public <T> T convert(@NotNull ResponseBody body, @NotNull Type type, boolean needDecodeResult) throws IOException {
        try {
            if (!(type instanceof Class<?>)) {
                throw new IllegalArgumentException("type must be a Class, but it was " + type);
            }
            Class<T> cls = (Class<T>) type;
            T read;
            if (needDecodeResult || type == String.class) {
                String result = body.string();
                if (needDecodeResult) {
                    result = RxHttpPlugins.onResultDecoder(result);
                }
                if (type == String.class) {
                    return (T) result;
                }
                read = serializer.read(cls, result, strict);
            } else {
                read = serializer.read(cls, body.charStream(), strict);
            }
            if (read == null) {
                throw new IllegalStateException("XmlConverter Could not deserialize body as " + cls);
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

    @SuppressWarnings("deprecation")
    @Override
    public <T> RequestBody convert(T value) throws IOException {
        Buffer buffer = new Buffer();
        try {
            OutputStreamWriter osw = new OutputStreamWriter(buffer.outputStream(), CHARSET);
            serializer.write(value, osw);
            osw.flush();
        } catch (RuntimeException | IOException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return RequestBody.create(contentType, buffer.readByteString());
    }
}
