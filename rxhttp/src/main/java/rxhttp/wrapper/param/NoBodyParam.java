package rxhttp.wrapper.param;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

import okhttp3.RequestBody;

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

    public NoBodyParam addAllEncoded(@NotNull Map<String, ?> map) {
        return addAllEncodedQuery(map);
    }

    @Override
    public final RequestBody getRequestBody() {
        return null;
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
