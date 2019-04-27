package rxhttp.wrapper.param;


import android.text.TextUtils;

import io.reactivex.annotations.NonNull;
import okhttp3.RequestBody;
import rxhttp.wrapper.utils.BuildUtil;

/**
 * 发送Put请求，参数以Json数据的形式提交
 * User: ljx
 * Date: 2019/1/19
 * Time: 11:36
 */
public class PutJsonParam extends AbstractPutParam {

    private String jsonParams; //Json 字符串参数

    protected PutJsonParam(@NonNull String url) {
        super(url);
    }

    static PutJsonParam with(String url) {
        return new PutJsonParam(url);
    }

    @Override
    public RequestBody getRequestBody() {
        String json = jsonParams;
        if (TextUtils.isEmpty(json)) {
            json = BuildUtil.mapToJson(this);
        }
        return BuildUtil.buildJsonRequestBody(json);
    }

    @Override
    public Param setJsonParams(String jsonParams) {
        this.jsonParams = jsonParams;
        return this;
    }
}
