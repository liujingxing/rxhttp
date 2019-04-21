package rxhttp.wrapper.param;


import rxhttp.wrapper.utils.BuildUtil;
import io.reactivex.annotations.NonNull;
import okhttp3.Request;

/**
 * 发送Get请求，调用此类
 * User: ljx
 * Date: 2019/1/19
 * Time: 14:35
 */
public final class GetParam extends AbstractParam implements GetRequest {

    private GetParam(@NonNull String url) {
        super(url);
    }

    static GetParam with(String url) {
        return new GetParam(url);
    }

    @Override
    public Request buildRequest() {
        return BuildUtil.buildGetRequest(this);
    }
}
