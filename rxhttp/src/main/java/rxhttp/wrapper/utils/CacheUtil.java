package rxhttp.wrapper.utils;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import rxhttp.RxHttpPlugins;
import rxhttp.wrapper.entity.KeyValuePair;

/**
 * User: ljx
 * Date: 2019-12-21
 * Time: 15:41
 */
public class CacheUtil {

    //过滤要剔除的cacheKey
    @SuppressWarnings("unchecked")
    public static <T> List<T> excludeCacheKey(List<T> objects) {
        if (objects == null) return null;
        List<String> excludeCacheKeys = RxHttpPlugins.getExcludeCacheKeys();
        if (excludeCacheKeys.isEmpty()) return objects;
        List<Object> newList = new ArrayList<>();
        for (Object object : objects) {
            if (object instanceof KeyValuePair) {
                KeyValuePair pair = (KeyValuePair) object;
                if (excludeCacheKeys.contains(pair.getKey())) continue;
            } else if (object instanceof Map) {
                Map<?, ?> map = excludeCacheKey((Map<?, ?>) object);
                if (map == null || map.size() == 0) continue;
            } else if (object instanceof JsonObject) {
                JsonObject jsonObject = excludeCacheKey((JsonObject) object);
                if (jsonObject == null || jsonObject.size() == 0) continue;
            }
            newList.add(object);
        }
        return (List<T>) newList;
    }

    //过滤要剔除的cacheKey
    public static Map<?, ?> excludeCacheKey(Map<?, ?> param) {
        if (param == null) return null;
        List<String> excludeCacheKeys = RxHttpPlugins.getExcludeCacheKeys();
        if (excludeCacheKeys.isEmpty()) return param;
        Map<String, Object> newParam = new LinkedHashMap<>();
        for (Map.Entry<?, ?> entry : param.entrySet()) {
            String key = entry.getKey().toString();
            if (excludeCacheKeys.contains(key)) continue;
            newParam.put(key, entry.getValue());
        }
        return newParam;
    }

    //过滤要剔除的cacheKey
    private static JsonObject excludeCacheKey(JsonObject jsonObject) {
        if (jsonObject == null) return null;
        List<String> excludeCacheKeys = RxHttpPlugins.getExcludeCacheKeys();
        if (excludeCacheKeys.isEmpty()) return jsonObject;
        JsonObject newParam = new JsonObject();
        for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
            String key = entry.getKey();
            if (excludeCacheKeys.contains(key)) continue;
            newParam.add(key, entry.getValue());
        }
        return newParam;
    }
}
