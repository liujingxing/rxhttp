package rxhttp.wrapper.param;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import java.util.List;

import io.reactivex.annotations.NonNull;

/**
 * User: ljx
 * Date: 2019-11-10
 * Time: 11:54
 */
@SuppressWarnings("unchecked")
public interface IJsonArray<P extends Param> {

    P add(@NonNull Object object);

    default P addAll(@NonNull List<?> list) {
        for (Object object : list) {
            add(object);
        }
        return (P) this;
    }

    default P addAll(@NonNull JsonArray jsonArray) {
        for (JsonElement next : jsonArray) {
            add(next);
        }
        return (P) this;
    }

    default P addJsonObject(@NonNull String jsonObject) {
        return add(new JsonParser().parse(jsonObject));
    }
}
