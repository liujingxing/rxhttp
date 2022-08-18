package rxhttp.wrapper.converter;

import com.google.protobuf.ExtensionRegistryLite;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.MessageLite;
import com.google.protobuf.Parser;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import rxhttp.wrapper.callback.IConverter;

/**
 * User: ljx
 * Date: 2019-11-24
 * Time: 14:53
 */
public class ProtoConverter implements IConverter {

    private final ExtensionRegistryLite registry;
    private final MediaType contentType;

    public ProtoConverter() {
        this(null);
    }

    public ProtoConverter(ExtensionRegistryLite registry) {
        this(registry, MediaType.get("application/x-protobuf"));
    }

    public ProtoConverter(ExtensionRegistryLite registry, MediaType contentType) {
        this.registry = registry;
        this.contentType = contentType;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T convert(ResponseBody body, Type type, boolean needDecodeResult) throws IOException {
        if (!(type instanceof Class<?>)) {
            return null;
        }
        Class<?> c = (Class<?>) type;
        if (!MessageLite.class.isAssignableFrom(c)) {
            return null;
        }

        Parser<MessageLite> parser;
        try {
            Method method = c.getDeclaredMethod("parser");
            //noinspection unchecked
            parser = (Parser<MessageLite>) method.invoke(null);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e.getCause());
        } catch (NoSuchMethodException | IllegalAccessException ignored) {
            // If the method is missing, fall back to original static field for pre-3.0 support.
            try {
                Field field = c.getDeclaredField("PARSER");
                //noinspection unchecked
                parser = (Parser<MessageLite>) field.get(null);
            } catch (NoSuchFieldException | IllegalAccessException e) {
                throw new IllegalArgumentException("Found a protobuf message but "
                    + c.getName()
                    + " had no parser() method or PARSER field.");
            }
        }

        try {
            T t = (T) (registry == null ? parser.parseFrom(body.byteStream())
                : parser.parseFrom(body.byteStream(), registry));
            if (t == null) {
                throw new IllegalStateException("ProtoConverter Could not deserialize body as " + type);
            }
            return t;
        } catch (InvalidProtocolBufferException e) {
            throw new RuntimeException(e); // Despite extending IOException, this is data mismatch.
        } finally {
            body.close();
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    public <T> RequestBody convert(T value) throws IOException {
        if (!(value instanceof MessageLite)) return null;
        MessageLite messageLite = (MessageLite) value;
        byte[] bytes = messageLite.toByteArray();
        return RequestBody.create(contentType, bytes);
    }
}
