package rxhttp.wrapper.param;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.ArrayList;
import java.util.HashMap;
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
public class JsonArrayParam extends AbstractBodyParam<JsonArrayParam> {

    private List<Object> bodyParam;

    /**
     * @param url    request url
     * @param method {@link Method#POST}、{@link Method#PUT}、{@link Method#DELETE}、{@link Method#PATCH}
     */
    public JsonArrayParam(String url, Method method) {
        super(url, method);
    }

    @Override
    public RequestBody getRequestBody() {
        final List<?> jsonArray = bodyParam;
        if (jsonArray == null)
            return RequestBody.create(null, new byte[0]);
        return convert(jsonArray);
    }

    /**
     * JsonArray类型请求，所有add系列方法内部最终都会调用此方法
     *
     * @param object Object
     * @return JsonArrayParam
     */
    public JsonArrayParam add(@Nullable Object object) {
        initList();
        bodyParam.add(object);
        return this;
    }

    @Override
    public JsonArrayParam add(String key, @Nullable Object value) {
        HashMap<String, Object> map = new HashMap<>();
        map.put(key, value);
        return add(map);
    }

    public JsonArrayParam addAll(String jsonElement) {
        JsonElement element = JsonParser.parseString(jsonElement);
        if (element.isJsonArray()) {
            return addAll(element.getAsJsonArray());
        } else if (element.isJsonObject()) {
            return addAll(element.getAsJsonObject());
        }
        return add(JsonUtil.toAny(element));
    }

    public JsonArrayParam addAll(JsonObject jsonObject) {
        return addAll(JsonUtil.toMap(jsonObject));
    }

    @Override
    public JsonArrayParam addAll(Map<String, ?> map) {
        initList();
        return super.addAll(map);
    }

    public JsonArrayParam addAll(JsonArray jsonArray) {
        return addAll(JsonUtil.toList(jsonArray));
    }

    public JsonArrayParam addAll(List<?> list) {
        initList();
        for (Object object : list) {
            add(object);
        }
        return this;
    }

    public JsonArrayParam addJsonElement(String jsonElement) {
        JsonElement element = JsonParser.parseString(jsonElement);
        return add(JsonUtil.toAny(element));
    }

    public JsonArrayParam addJsonElement(String key, String jsonElement) {
        JsonElement element = JsonParser.parseString(jsonElement);
        return add(key, JsonUtil.toAny(element));
    }

    /**
     * @return List
     * @deprecated please use {@link #getBodyParam()} instead, scheduled to be removed in RxHttp 3.0 release.
     */
    @Deprecated
    @Nullable
    public List<Object> getList() {
        return getBodyParam();
    }

    @Nullable
    public List<Object> getBodyParam() {
        return bodyParam;
    }

    @Override
    public String buildCacheKey() {
        List<KeyValuePair> queryPairs = CacheUtil.excludeCacheKey(getQueryParam());
        HttpUrl httpUrl = BuildUtil.getHttpUrl(getSimpleUrl(), queryPairs, getPaths());
        List<Object> list = CacheUtil.excludeCacheKey(bodyParam);
        String json = GsonUtil.toJson(list);
        Builder builder = httpUrl.newBuilder().addQueryParameter("json", json);
        return builder.toString();
    }

    private void initList() {
        if (bodyParam == null) bodyParam = new ArrayList<>();
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
        return "JsonArrayParam{" +
            "url = " + url +
            " bodyParam = " + bodyParam +
            '}';
    }
}
