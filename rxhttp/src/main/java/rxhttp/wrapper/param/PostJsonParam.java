package rxhttp.wrapper.param;


import rxhttp.wrapper.utils.BuildUtil;
import io.reactivex.annotations.NonNull;
import okhttp3.RequestBody;

/**
 * 发送Post请求，参数以Json数据的形式提交
 * User: ljx
 * Date: 2019/1/19
 * Time: 11:36
 */
public class PostJsonParam extends AbstractPostParam {

    protected PostJsonParam(@NonNull String url) {
        super(url);
    }

    static PostJsonParam with(String url) {
        return new PostJsonParam(url);
    }

    @Override
    public RequestBody getRequestBody() {
        return BuildUtil.buildJsonRequestBody(this);
    }
}
