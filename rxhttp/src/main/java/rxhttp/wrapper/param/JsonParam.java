package rxhttp.wrapper.param;


import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import okhttp3.HttpUrl;
import okhttp3.HttpUrl.Builder;
import okhttp3.RequestBody;
import rxhttp.wrapper.annotations.Nullable;
import rxhttp.wrapper.utils.CacheUtil;
import rxhttp.wrapper.utils.GsonUtil;
import rxhttp.wrapper.utils.JsonUtil;

/**
 * post、put、patch、delete请求，参数以{application/json; charset=utf-8}形式提交
 * User: ljx
 * Date: 2019-09-09
 * Time: 21:08
 */
public class JsonParam extends BodyParam<JsonParam> {

    private Map<String, Object> mParam; //请求参数

    /**
     * @param url    request url
     * @param method {@link Method#POST}、{@link Method#PUT}、{@link Method#DELETE}、{@link Method#PATCH}
     */
    public JsonParam(String url, Method method) {
        super(url, method);
    }

    @Override
    public RequestBody getRequestBody() {
        final Map<String, Object> params = mParam;
        if (params == null)
            return RequestBody.create(null, new byte[0]);
        return convert(params);
    }

    @Override
    public JsonParam add(String key, @Nullable Object value) {
        initMap();
        mParam.put(key, value);
        return this;
    }

    public JsonParam addAll(String jsonObject) {
        return addAll(JsonParser.parseString(jsonObject).getAsJsonObject());
    }

    public JsonParam addAll(JsonObject jsonObject) {
        return addAll(JsonUtil.toMap(jsonObject));
    }

    @Override
    public JsonParam addAll(Map<String, ?> map) {
        initMap();
        for (Entry<String, ?> entry : map.entrySet()) {
            add(entry.getKey(), entry.getValue());
        }
        return this;
    }

    public JsonParam addJsonElement(String key, String jsonElement) {
        JsonElement element = JsonParser.parseString(jsonElement);
        return add(key, JsonUtil.toAny(element));
    }

    @Nullable
    public Map<String, Object> getParams() {
        return mParam;
    }

    @Override
    public String getCacheKey() {
        String cacheKey = super.getCacheKey();
        if (cacheKey != null) return cacheKey;
        Map<?, ?> param = CacheUtil.excludeCacheKey(mParam);
        String json = GsonUtil.toJson(param);
        HttpUrl httpUrl = HttpUrl.get(getSimpleUrl());
        Builder builder = httpUrl.newBuilder().addQueryParameter("json", json);
        return builder.toString();
    }

    private void initMap() {
        if (mParam == null) mParam = new LinkedHashMap<>();
    }

    @Override
    public String toString() {
        return "JsonParam{" +
            "url=" + getUrl() +
            "mParam=" + mParam +
            '}';
    }
}
