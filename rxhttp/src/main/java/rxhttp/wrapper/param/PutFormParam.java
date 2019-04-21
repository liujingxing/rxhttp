package rxhttp.wrapper.param;


import rxhttp.wrapper.utils.BuildUtil;
import io.reactivex.annotations.NonNull;
import okhttp3.RequestBody;

/**
 * 发送Put请求，参数以Form表单键值对的形式提交
 * User: ljx
 * Date: 2019/1/19
 * Time: 11:36
 */
public class PutFormParam extends AbstractPutParam {

    protected PutFormParam(@NonNull String url) {
        super(url);
    }

    static PutFormParam with(String url) {
        return new PutFormParam(url);
    }

    @Override
    public RequestBody getRequestBody() {
        return BuildUtil.buildFormRequestBody(this);
    }
}
