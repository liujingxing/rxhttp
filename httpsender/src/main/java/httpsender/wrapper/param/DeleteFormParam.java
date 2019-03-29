package httpsender.wrapper.param;


import httpsender.wrapper.utils.BuildUtil;
import io.reactivex.annotations.NonNull;
import okhttp3.RequestBody;

/**
 * 发送Delete请求，参数以Form表单键值对的形式提交
 * User: ljx
 * Date: 2019/1/19
 * Time: 11:36
 */
public class DeleteFormParam extends AbstractDeleteParam {

    protected DeleteFormParam(@NonNull String url) {
        super(url);
    }

    static DeleteFormParam with(String url) {
        return new DeleteFormParam(url);
    }

    @Override
    public RequestBody getRequestBody() {
        return BuildUtil.buildFormRequestBody(this);
    }
}
