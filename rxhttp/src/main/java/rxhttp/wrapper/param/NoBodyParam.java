package rxhttp.wrapper.param;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import okhttp3.HttpUrl;
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

    private List<KeyValuePair> mKeyValuePairs; //键值对数组

    /**
     * @param url    请求路径
     * @param method Method#GET  Method#HEAD  Method#DELETE
     */
    public NoBodyParam(String url, Method method) {
        super(url, method);
    }

    @Override
    public final String getUrl() {
        return getHttpUrl().toString();
    }

    @Override
    public HttpUrl getHttpUrl() {
        return BuildUtil.getHttpUrl(getSimpleUrl(), mKeyValuePairs);
    }

    @Override
    public NoBodyParam add(String key, Object value) {
        if (value == null) value = "";
        return add(new KeyValuePair(key, value));
    }

    public NoBodyParam addEncoded(String key, Object value) {
        return add(new KeyValuePair(key, value, true));
    }

    public NoBodyParam removeAllBody(String key) {
        final List<KeyValuePair> keyValuePairs = mKeyValuePairs;
        if (keyValuePairs == null) return this;
        Iterator<KeyValuePair> iterator = keyValuePairs.iterator();
        while (iterator.hasNext()) {
            KeyValuePair next = iterator.next();
            if (next.equals(key))
                iterator.remove();
        }
        return this;
    }

    public NoBodyParam removeAllBody() {
        final List<KeyValuePair> keyValuePairs = mKeyValuePairs;
        if (keyValuePairs != null)
            keyValuePairs.clear();
        return this;
    }

    public NoBodyParam set(String key, Object value) {
        removeAllBody(key);
        return add(key, value);
    }

    public NoBodyParam setEncoded(String key, Object value) {
        removeAllBody(key);
        return addEncoded(key, value);
    }

    @Nullable
    public Object queryValue(String key) {
        final List<KeyValuePair> keyValuePairs = mKeyValuePairs;
        if (keyValuePairs == null) return this;
        for (KeyValuePair pair : keyValuePairs) {
            if (pair.equals(key))
                return pair.getValue();
        }
        return null;
    }

    @NonNull
    public List<Object> queryValues(String key) {
        final List<KeyValuePair> keyValuePairs = mKeyValuePairs;
        if (keyValuePairs == null) return Collections.emptyList();
        List<Object> values = new ArrayList<>();
        for (KeyValuePair pair : keyValuePairs) {
            if (pair.equals(key))
                values.add(pair.getValue());
        }
        return Collections.unmodifiableList(values);
    }

    private NoBodyParam add(KeyValuePair keyValuePair) {
        List<KeyValuePair> keyValuePairs = mKeyValuePairs;
        if (keyValuePairs == null) {
            keyValuePairs = mKeyValuePairs = new ArrayList<>();
        }
        keyValuePairs.add(keyValuePair);
        return this;
    }

    @Override
    public final RequestBody getRequestBody() {
        return null;
    }

    @Override
    public String getCacheKey() {
        String cacheKey = super.getCacheKey();
        if (cacheKey != null) return cacheKey;
        List<KeyValuePair> keyValuePairs = CacheUtil.excludeCacheKey(mKeyValuePairs);
        return BuildUtil.getHttpUrl(getSimpleUrl(), keyValuePairs).toString();
    }

    public List<KeyValuePair> getKeyValuePairs() {
        return mKeyValuePairs;
    }

    @Override
    public String toString() {
        return getUrl();
    }
}
