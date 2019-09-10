package rxhttp.wrapper.param;

import android.text.TextUtils;

import okhttp3.Request;
import okhttp3.RequestBody;
import rxhttp.wrapper.utils.BuildUtil;

/**
 * User: ljx
 * Date: 2019-09-09
 * Time: 21:08
 */
public class JsonParam extends AbstractParam<JsonParam> implements BodyRequest {

    protected String jsonParams; //Json 字符串参数

    public JsonParam(String url, String method) {
        super(url, method);
    }

    @Override
    public Request buildRequest() {
        return BuildUtil.buildRequest(this, method);
    }

    @Override
    public RequestBody getRequestBody() {
        String json = jsonParams;
        if (TextUtils.isEmpty(json)) {
            json = BuildUtil.mapToJson(this);
        }
        return BuildUtil.buildJsonRequestBody(json);
    }

    public Param setJsonParams(String jsonParams) {
        this.jsonParams = jsonParams;
        return this;
    }
}
