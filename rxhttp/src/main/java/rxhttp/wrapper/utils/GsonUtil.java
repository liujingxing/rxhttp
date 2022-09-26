package rxhttp.wrapper.utils;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSyntaxException;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Type;

/**
 * User: ljx
 * Date: 2018/01/19
 * Time: 10:46
 */
public class GsonUtil {

    private static final JsonDeserializer<String> STRING = (json, typeOfT, context) -> {
        if (json instanceof JsonPrimitive) {
            return json.getAsString();
        } else {
            return json.toString();
        }
    };
    private static final JsonDeserializer<Integer> INTEGER = (json, typeOfT, context) -> {
        return isEmpty(json) ? 0 : json.getAsInt();
    };
    private static final JsonDeserializer<Float> FLOAT = (json, typeOfT, context) -> {
        return isEmpty(json) ? 0.0f : json.getAsFloat();
    };
    private static final JsonDeserializer<Double> DOUBLE = (json, typeOfT, context) -> {
        return isEmpty(json) ? 0.0 : json.getAsDouble();
    };
    private static final JsonDeserializer<Long> LONG = (json, typeOfT, context) -> {
        return isEmpty(json) ? 0L : json.getAsLong();
    };

    //return null when an exception occurs
    @Nullable
    public static <T> T getObject(String json, Type type) {
        try {
            return fromJson(json, type);
        } catch (Exception ignore) {
            return null;
        }
    }

    //throw exception if deserialization failed
    @NotNull
    public static <T> T fromJson(String json, Type type) {
        Gson gson = buildGson();
        T t = gson.fromJson(json, type);
        if (t == null) {
            throw new JsonSyntaxException("The string '" + json + "' could not be deserialized to " + type + " object");
        }
        return t;
    }

    public static String toJson(Object object) {
        return buildGson().toJson(object);
    }


    public static Gson buildGson() {
        return GsonHolder.gson;
    }

    private static Gson newGson() {
        return new GsonBuilder()
            .disableHtmlEscaping()
            .registerTypeAdapter(String.class, STRING)
            .registerTypeAdapter(int.class, INTEGER)
            .registerTypeAdapter(Integer.class, INTEGER)
            .registerTypeAdapter(float.class, FLOAT)
            .registerTypeAdapter(Float.class, FLOAT)
            .registerTypeAdapter(double.class, DOUBLE)
            .registerTypeAdapter(Double.class, DOUBLE)
            .registerTypeAdapter(long.class, LONG)
            .registerTypeAdapter(Long.class, LONG)
            .create();
    }

    private static boolean isEmpty(JsonElement jsonElement) {
        try {
            String str = jsonElement.getAsString();
            return "".equals(str) || "null".equals(str);
        } catch (Exception ignore) {
            return false;
        }
    }

    private static final class GsonHolder {
        static final Gson gson = newGson();
    }
}