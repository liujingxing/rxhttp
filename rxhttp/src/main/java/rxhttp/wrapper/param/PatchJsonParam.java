package rxhttp.wrapper.param;


import android.text.TextUtils;

import rxhttp.wrapper.utils.BuildUtil;
import io.reactivex.annotations.NonNull;
import okhttp3.RequestBody;

/**
 * 发送Patch请求，参数以Json数据的形式提交
 * User: ljx
 * Date: 2019/1/19
 * Time: 11:36
 */
public class PatchJsonParam extends AbstractPatchParam {

    private String jsonParams; //Json 字符串参数

    protected PatchJsonParam(@NonNull String url) {
        super(url);
    }

    static PatchJsonParam with(String url) {
        return new PatchJsonParam(url);
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
