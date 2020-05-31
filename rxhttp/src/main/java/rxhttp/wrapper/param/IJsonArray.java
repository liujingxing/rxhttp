package rxhttp.wrapper.param;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import java.util.List;

import rxhttp.wrapper.annotations.NonNull;

/**
 * User: ljx
 * Date: 2019-11-10
 * Time: 11:54
 */
@SuppressWarnings("unchecked")
public interface IJsonArray<P extends Param<P>> extends IJsonObject<P> {

    /**
     * 添加一个对象，JsonArray类型请求，所有add系列方法内部最终都会调用此方法
     *
     * @param object Object
     * @return P
     */
    P add(@NonNull Object object);

    /**
     * 添加多个对象
     *
     * @param list List
     * @return P
     */
    default P addAll(@NonNull List<?> list) {
        for (Object object : list) {
            add(object);
        }
        return (P) this;
    }

    /**
     * 添加多个对象，将字符串转JsonElement对象,并根据不同类型,执行不同操作
     *
     * @param jsonElement 可输入任意非空字符串
     * @return P
     */
    @Override
    default P addAll(@NonNull String jsonElement) {
        JsonElement parse = new JsonParser().parse(jsonElement);
        if (parse.isJsonArray()) {
            return addAll(parse.getAsJsonArray());
        } else if (parse.isJsonObject()) {
            return addAll(parse.getAsJsonObject());
        }
        return add(parse);
    }

    /**
     * 添加多个对象
     *
     * @param jsonArray JsonArray
     * @return P
     */
    default P addAll(@NonNull JsonArray jsonArray) {
        for (JsonElement next : jsonArray) {
            add(next);
        }
        return (P) this;
    }

    /**
     * 添加一个JsonElement对象(Json对象、json数组等)
     *
     * @param jsonElement 可输入任意非空字符串
     * @return P
     */
    default P addJsonElement(@NonNull String jsonElement) {
        return add(new JsonParser().parse(jsonElement));
    }
}
