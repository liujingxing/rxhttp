package rxhttp.wrapper.param;

import java.util.List;
import java.util.Map;

import okhttp3.RequestBody;
import rxhttp.wrapper.annotations.NonNull;
import rxhttp.wrapper.annotations.Nullable;
import rxhttp.wrapper.entity.KeyValuePair;

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

    @Override
    public final RequestBody getRequestBody() {
        return null;
    }

    /**
     * @return List
     * @deprecated please use {@link #getQueryParam()} instead, scheduled to be removed in RxHttp 3.0 release.
     */
    @Deprecated
    public List<KeyValuePair> getKeyValuePairs() {
        return getQueryParam();
    }

    @Override
    public String toString() {
        String url = getSimpleUrl();
        if (url.startsWith("http")) {
            url = getUrl();
        }
        return url;
    }
}
