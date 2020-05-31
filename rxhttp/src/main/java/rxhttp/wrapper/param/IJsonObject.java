package rxhttp.wrapper.param;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.Map.Entry;

import okhttp3.MediaType;
import rxhttp.wrapper.annotations.NonNull;

/**
 * User: ljx
 * Date: 2019-11-10
 * Time: 11:54
 */
@SuppressWarnings("unchecked")
public interface IJsonObject<P extends Param<P>> {

    MediaType MEDIA_TYPE_JSON = MediaType.parse("application/json; charset=utf-8");

    P add(String key, @NonNull Object object);

    /**
     * 将Json对象里面的key-value逐一取出，添加到另一个Json对象中，
     * 输入非Json对象将抛出{@link IllegalStateException}异常
     *
     * @param jsonObject 字符串Json对象
     * @return P
     */
    default P addAll(@NonNull String jsonObject) {
        return addAll(new JsonParser().parse(jsonObject).getAsJsonObject());
    }

    /**
     * 将Json对象里面的key-value逐一取出，添加到另一个Json对象中
     *
     * @param jsonObject Json对象
     * @return P
     */
    default P addAll(@NonNull JsonObject jsonObject) {
        for (Entry<String, JsonElement> next : jsonObject.entrySet()) {
            add(next.getKey(), next.getValue());
        }
        return (P) this;
    }

    /**
     * 添加一个JsonElement对象(Json对象、json数组等)
     *
     * @param key         key
     * @param jsonElement 可输入任意非空字符串
     * @return P
     */
    default P addJsonElement(String key, @NonNull String jsonElement) {
        return add(key, new JsonParser().parse(jsonElement));
    }
}
