package rxhttp.wrapper.param;


import rxhttp.wrapper.utils.BuildUtil;
import io.reactivex.annotations.NonNull;
import okhttp3.RequestBody;

/**
 * 发送Put请求，参数以Json数据的形式提交
 * User: ljx
 * Date: 2019/1/19
 * Time: 11:36
 */
public class PutJsonParam extends AbstractPutParam {

    protected PutJsonParam(@NonNull String url) {
        super(url);
    }

    static PutJsonParam with(String url) {
        return new PutJsonParam(url);
    }

    @Override
    public RequestBody getRequestBody() {
        return BuildUtil.buildJsonRequestBody(this);
    }
}
