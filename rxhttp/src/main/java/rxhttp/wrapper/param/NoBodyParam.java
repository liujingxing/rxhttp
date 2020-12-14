package rxhttp.wrapper.param;

import java.util.List;
import java.util.Map;

import okhttp3.RequestBody;
import rxhttp.wrapper.annotations.NonNull;
import rxhttp.wrapper.annotations.Nullable;
import rxhttp.wrapper.entity.KeyValuePair;
import rxhttp.wrapper.utils.BuildUtil;
import rxhttp.wrapper.utils.CacheUtil;

/**
 * Get、Head没有body的请求调用此类
 * User: ljx
 * Date: 2019-09-09
 * Time: 21:08
 */
public class NoBodyParam extends AbstractParam<NoBodyParam> {

    /**
     * @param url    请求路径
     * @param method Method#GET  Method#HEAD  Method#DELETE
     */
    public NoBodyParam(String url, Method method) {
        super(url, method);
    }

    @Override
    public NoBodyParam add(String key, @Nullable Object value) {
        return addQuery(key, value);
    }

    public NoBodyParam addEncoded(String key, @Nullable Object value) {
        return addEncodedQuery(key, value);
    }

    public NoBodyParam addAllEncoded(@NonNull Map<String, ?> map) {
        return addAllEncodedQuery(map);
    }

    public NoBodyParam set(String key, Object value) {
        return setQuery(key, value);
    }

    public NoBodyParam setEncoded(String key, Object value) {
        return setEncodedQuery(key, value);
    }

    @Override
    public final RequestBody getRequestBody() {
        return null;
    }

    @Override
    public String getCacheKey() {
        String cacheKey = super.getCacheKey();
        if (cacheKey != null) return cacheKey;
        List<KeyValuePair> keyValuePairs = CacheUtil.excludeCacheKey(getQueryPairs());
        return BuildUtil.getHttpUrl(getSimpleUrl(), keyValuePairs).toString();
    }

    /**
     * @deprecated please user {@link #getQueryPairs()} instead
     */
    @Deprecated
    public List<KeyValuePair> getKeyValuePairs() {
        return getQueryPairs();
    }

    @Override
    public String toString() {
        return getUrl();
    }
}
