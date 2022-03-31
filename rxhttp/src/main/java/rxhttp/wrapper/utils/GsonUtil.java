package rxhttp.wrapper.utils;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
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

    private static Gson gson;

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
        if (gson == null) {
            gson = new GsonBuilder()
                .disableHtmlEscaping()
                .registerTypeAdapter(String.class, new StringAdapter())
                .registerTypeAdapter(int.class, new IntegerDefault0Adapter())
                .registerTypeAdapter(Integer.class, new IntegerDefault0Adapter())
                .registerTypeAdapter(double.class, new DoubleDefault0Adapter())
                .registerTypeAdapter(Double.class, new DoubleDefault0Adapter())
                .registerTypeAdapter(long.class, new LongDefault0Adapter())
                .registerTypeAdapter(Long.class, new LongDefault0Adapter())
                .create();
        }
        return gson;
    }

    private static class StringAdapter implements JsonSerializer<String>, JsonDeserializer<String> {
        @Override
        public String deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {
            if (json instanceof JsonPrimitive) {
                return json.getAsString();
            } else {
                return json.toString();
            }
        }

        @Override
        public JsonElement serialize(String src, Type typeOfSrc, JsonSerializationContext context) {
            return new JsonPrimitive(src);
        }
    }

    private static class IntegerDefault0Adapter implements JsonSerializer<Integer>, JsonDeserializer<Integer> {
        @Override
        public Integer deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {
            try {
                String str = json.getAsString();
                if ("".equals(str) || "null".equals(str)) {//定义为int类型,如果后台返回""或者null,则返回0
                    return 0;
                }
            } catch (Exception ignore) {
            }
            return json.getAsInt();
        }

        @Override
        public JsonElement serialize(Integer src, Type typeOfSrc, JsonSerializationContext context) {
            return new JsonPrimitive(src);
        }
    }

    private static class DoubleDefault0Adapter implements JsonSerializer<Double>, JsonDeserializer<Double> {
        @Override
        public Double deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            try {
                String str = json.getAsString();
                if ("".equals(str) || "null".equals(str)) {//定义为double类型,如果后台返回""或者null,则返回0.00
                    return 0.00;
                }
            } catch (Exception ignore) {
            }
            return json.getAsDouble();
        }

        @Override
        public JsonElement serialize(Double src, Type typeOfSrc, JsonSerializationContext context) {
            return new JsonPrimitive(src);
        }
    }

    private static class LongDefault0Adapter implements JsonSerializer<Long>, JsonDeserializer<Long> {
        @Override
        public Long deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {
            try {
                String str = json.getAsString();
                if ("".equals(str) || "null".equals(str)) { //定义为long类型,如果后台返回""或者null,则返回0
                    return 0L;
                }
            } catch (Exception ignore) {
            }
            return json.getAsLong();
        }

        @Override
        public JsonElement serialize(Long src, Type typeOfSrc, JsonSerializationContext context) {
            return new JsonPrimitive(src);
        }
    }
}