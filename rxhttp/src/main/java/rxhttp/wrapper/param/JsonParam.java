package rxhttp.wrapper.param;


import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import okhttp3.HttpUrl;
import okhttp3.HttpUrl.Builder;
import okhttp3.RequestBody;
import rxhttp.RxHttpPlugins;
import rxhttp.wrapper.annotations.Nullable;
import rxhttp.wrapper.callback.IConverter;
import rxhttp.wrapper.callback.JsonConverter;
import rxhttp.wrapper.entity.KeyValuePair;
import rxhttp.wrapper.utils.BuildUtil;
import rxhttp.wrapper.utils.CacheUtil;
import rxhttp.wrapper.utils.GsonUtil;
import rxhttp.wrapper.utils.JsonUtil;

/**
 * post、put、patch、delete请求，参数以{application/json; charset=utf-8}形式提交
 * User: ljx
 * Date: 2019-09-09
 * Time: 21:08
 */
public class JsonParam extends AbstractBodyParam<JsonParam> {

    private Map<String, Object> bodyParam; //请求参数

    /**
     * @param url    request url
     * @param method {@link Method#POST}、{@link Method#PUT}、{@link Method#DELETE}、{@link Method#PATCH}
     */
    public JsonParam(String url, Method method) {
        super(url, method);
    }

    @Override
    public RequestBody getRequestBody() {
        final Map<String, Object> bodyParam = this.bodyParam;
        if (bodyParam == null)
            return RequestBody.create(null, new byte[0]);
        return convert(bodyParam);
    }

    @Override
    public JsonParam add(String key, @Nullable Object value) {
        initMap();
        bodyParam.put(key, value);
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
        return super.addAll(map);
    }

    public JsonParam addJsonElement(String key, String jsonElement) {
        JsonElement element = JsonParser.parseString(jsonElement);
        return add(key, JsonUtil.toAny(element));
    }

    /**
     * @return Map
     * @deprecated please use {@link #getBodyParam()} instead, scheduled to be removed in RxHttp 3.0 release.
     */
    @Deprecated
    @Nullable
    public Map<String, Object> getParams() {
        return getBodyParam();
    }

    public Map<String, Object> getBodyParam() {
        return bodyParam;
    }

    @Override
    public String buildCacheKey() {
        List<KeyValuePair> queryPairs = CacheUtil.excludeCacheKey(getQueryParam());
        HttpUrl httpUrl = BuildUtil.getHttpUrl(getSimpleUrl(), queryPairs, getPaths());
        Map<?, ?> param = CacheUtil.excludeCacheKey(bodyParam);
        String json = GsonUtil.toJson(param);
        Builder builder = httpUrl.newBuilder().addQueryParameter("json", json);
        return builder.toString();
    }

    private void initMap() {
        if (bodyParam == null) bodyParam = new LinkedHashMap<>();
    }

    @Override
    protected IConverter getConverter() {
        IConverter converter = super.getConverter();
        if (!(converter instanceof JsonConverter)) {
            converter = RxHttpPlugins.getConverter();
        }
        return converter;
    }

    @Override
    public String toString() {
        String url = getSimpleUrl();
        if (url.startsWith("http")) {
            url = getUrl();
        }
        return "JsonParam{" +
            "url = " + url +
            " bodyParam = " + bodyParam +
            '}';
    }
}
