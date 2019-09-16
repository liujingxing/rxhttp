package rxhttp.wrapper.param;

import okhttp3.RequestBody;

/**
 * Get、Head没有body的请求调用此类
 * User: ljx
 * Date: 2019-09-09
 * Time: 21:08
 */
public class NoBodyParam extends AbstractParam<NoBodyParam> {

    public NoBodyParam(String url, String method) {
        super(url, method);
    }

    @Override
    public RequestBody getRequestBody() {
        return null;
    }
}
