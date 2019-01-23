package httpsender.wrapper.param;


import httpsender.wrapper.utils.BuildUtil;
import io.reactivex.annotations.NonNull;
import okhttp3.Request;

/**
 * 发送Head请求，调用此类
 * User: ljx
 * Date: 2019/1/19
 * Time: 14:35
 */
public final class HeadParam extends AbstractParam implements HeadRequest {

    private HeadParam(@NonNull String url) {
        super(url);
    }

    static HeadParam with(String url) {
        return new HeadParam(url);
    }

    @Override
    public Request buildRequest() {
        return BuildUtil.buildHeadRequest(this);
    }
}
