package httpsender.wrapper.param;


import httpsender.wrapper.utils.BuildUtil;
import io.reactivex.annotations.NonNull;
import okhttp3.Request;

/**
 * Post请求继承本类
 * User: ljx
 * Date: 2019/1/19
 * Time: 11:36
 */
public abstract class AbstractPostParam extends AbstractParam implements PostRequest {

    public AbstractPostParam(@NonNull String url) {
        super(url);
    }

    @Override
    public final Request buildRequest() {
        return BuildUtil.buildPostRequest(this);
    }
}
