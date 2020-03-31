package rxhttp.wrapper.param;


import java.util.LinkedHashMap;
import java.util.Map;

import okhttp3.HttpUrl;
import okhttp3.HttpUrl.Builder;
import okhttp3.RequestBody;
import rxhttp.wrapper.annotations.NonNull;
import rxhttp.wrapper.annotations.Nullable;
import rxhttp.wrapper.utils.CacheUtil;
import rxhttp.wrapper.utils.GsonUtil;

/**
 * post、put、patch、delete请求，参数以{application/json; charset=utf-8}形式提交
 * User: ljx
 * Date: 2019-09-09
 * Time: 21:08
 */
public class JsonParam extends AbstractParam<JsonParam> implements IJsonObject<JsonParam> {

    private Map<String, Object> mParam; //请求参数

    /**
     * @param url    请求路径
     * @param method Method#POST  Method#PUT  Method#DELETE  Method#PATCH
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
    public JsonParam add(String key, @NonNull Object value) {
        Map<String, Object> param = mParam;
        if (param == null) {
            param = mParam = new LinkedHashMap<>();
        }
        param.put(key, value);
        return this;
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

    @Override
    public String toString() {
        return "JsonParam{" +
            "url=" + getUrl() +
            "mParam=" + mParam +
            '}';
    }
}
