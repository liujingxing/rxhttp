package rxhttp.wrapper.param;

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
     * @param method {@link Method#GET,Method#HEAD,Method#DELETE}
     */
    public NoBodyParam(String url, Method method) {
        super(url, method);
    }

    @Override
    public final RequestBody getRequestBody() {
        return null;
    }
}
