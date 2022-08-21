package rxhttp.wrapper.utils;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSyntaxException;

import java.lang.reflect.Type;

import rxhttp.wrapper.annotations.NonNull;
import rxhttp.wrapper.annotations.Nullable;

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

    /**
     * json字符串转对象，解析失败，不会抛出异常，会直接返回null
     *
     * @param json json字符串
     * @param type 对象类类型
     * @param <T>  返回类型
     * @return T，返回对象有可能为空
     */
    @Nullable
    public static <T> T getObject(String json, Type type) {
        try {
            return fromJson(json, type);
        } catch (Exception ignore) {
            return null;
        }
    }

    /**
     * json字符串转对象，解析失败，将抛出对应的{@link JsonSyntaxException}异常，根据异常可查找原因
     *
     * @param json json字符串
     * @param type 对象类类型
     * @param <T>  返回类型
     * @return T，返回对象不为空
     */
    @NonNull
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