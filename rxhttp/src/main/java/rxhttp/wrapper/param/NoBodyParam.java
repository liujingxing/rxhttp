package rxhttp.wrapper.param;

import java.util.LinkedHashMap;
import java.util.Map;

import io.reactivex.annotations.NonNull;
import okhttp3.HttpUrl;
import okhttp3.RequestBody;
import rxhttp.wrapper.utils.BuildUtil;

/**
 * Get、Head没有body的请求调用此类
 * User: ljx
 * Date: 2019-09-09
 * Time: 21:08
 */
public class NoBodyParam extends AbstractParam<NoBodyParam> {

    private Map<String, Object> mParam; //请求参数
    /**
     * @param url    请求路径
     * @param method {@link Method#GET,Method#HEAD,Method#DELETE}
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
        return BuildUtil.getHttpUrl(getSimpleUrl(), mParam);
    }

    @Override
    public NoBodyParam add(String key, Object value) {
        if (value == null) value = "";
        Map<String, Object> param = mParam;
        if (param == null) {
            param = mParam = new LinkedHashMap<>();
        }
        param.put(key, value);
        return this;
    }

    @Override
    public final RequestBody getRequestBody() {
        return null;
    }

    @NonNull
    public Map<String, Object> getParams() {
        return mParam;
    }

    @Override
    public String toString() {
        return getUrl();
    }
}
