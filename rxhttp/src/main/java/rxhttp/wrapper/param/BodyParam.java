package rxhttp.wrapper.param;

import okhttp3.Request;
import rxhttp.wrapper.utils.BuildUtil;

/**
 * 有body的请求继承本类，如:post、put、patch、delete
 * User: ljx
 * Date: 2019-09-09
 * Time: 21:08
 */
public abstract class BodyParam<T extends BodyParam> extends AbstractParam<T> implements BodyRequest {

    public BodyParam(String url, String method) {
        super(url, method);
    }

    @Override
    public Request buildRequest() {
        return BuildUtil.buildRequest(this, method);
    }
}
