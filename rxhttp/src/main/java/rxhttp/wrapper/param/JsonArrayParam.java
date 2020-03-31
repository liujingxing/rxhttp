package rxhttp.wrapper.param;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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
public class JsonArrayParam extends AbstractParam<JsonArrayParam> implements IJsonArray<JsonArrayParam> {

    private List<Object> mList;

    /**
     * @param url    请求路径
     * @param method Method#POST  Method#PUT  Method#DELETE  Method#PATCH
     */
    public JsonArrayParam(String url, Method method) {
        super(url, method);
    }

    @Override
    public RequestBody getRequestBody() {
        final List<?> jsonArray = mList;
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
    public JsonArrayParam add(@NonNull Object object) {
        List<Object> list = mList;
        if (list == null) {
            list = mList = new ArrayList<>();
        }
        list.add(object);
        return this;
    }

    @Override
    public JsonArrayParam add(String key, @NonNull Object value) {
        HashMap<String, Object> map = new HashMap<>();
        map.put(key, value);
        return add(map);
    }

    @Nullable
    public List<Object> getList() {
        return mList;
    }

    @Override
    public String getCacheKey() {
        String cacheKey = super.getCacheKey();
        if (cacheKey != null) return cacheKey;
        List<Object> list = CacheUtil.excludeCacheKey(mList);
        String json = GsonUtil.toJson(list);
        HttpUrl httpUrl = HttpUrl.get(getSimpleUrl());
        Builder builder = httpUrl.newBuilder().addQueryParameter("json", json);
        return builder.toString();
    }

    @Override
    public String toString() {
        return "JsonArrayParam{" +
            "url=" + getUrl() +
            "mList=" + mList +
            '}';
    }
}
