package httpsender.wrapper.param;


import httpsender.wrapper.utils.BuildUtil;
import io.reactivex.annotations.NonNull;
import okhttp3.RequestBody;

/**
 * 发送Delete请求，参数以Json数据的形式提交
 * User: ljx
 * Date: 2019/1/19
 * Time: 11:36
 */
public class DeleteJsonParam extends AbstractDeleteParam {

    protected DeleteJsonParam(@NonNull String url) {
        super(url);
    }

    static DeleteJsonParam with(String url) {
        return new DeleteJsonParam(url);
    }

    @Override
    public RequestBody getRequestBody() {
        return BuildUtil.buildJsonRequestBody(this);
    }
}
