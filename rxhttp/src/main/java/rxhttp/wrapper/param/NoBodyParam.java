package rxhttp.wrapper.param;

import okhttp3.Request;
import rxhttp.wrapper.utils.BuildUtil;

/**
 * User: ljx
 * Date: 2019-09-09
 * Time: 21:08
 */
public class NoBodyParam extends AbstractParam<NoBodyParam> implements NoBodyRequest {

    public NoBodyParam(String url, String method) {
        super(url, method);
    }

    @Override
    public Request buildRequest() {
        return BuildUtil.buildRequest(this, method);
    }
}
