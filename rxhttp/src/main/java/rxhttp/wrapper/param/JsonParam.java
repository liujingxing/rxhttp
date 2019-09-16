package rxhttp.wrapper.param;

import android.text.TextUtils;

import okhttp3.RequestBody;
import rxhttp.wrapper.utils.BuildUtil;

/**
 * post、put、patch、delete请求，参数以{application/json;charset=utf-8}形式提交
 * User: ljx
 * Date: 2019-09-09
 * Time: 21:08
 */
public class JsonParam extends AbstractParam<JsonParam> {

    protected String jsonParams; //Json 字符串参数

    public JsonParam(String url, String method) {
        super(url, method);
    }

    @Override
    public RequestBody getRequestBody() {
        String json = jsonParams;
        if (TextUtils.isEmpty(json)) {
            json = BuildUtil.mapToJson(this);
        }
        return BuildUtil.buildJsonRequestBody(json);
    }

    public JsonParam setJsonParams(String jsonParams) {
        this.jsonParams = jsonParams;
        return this;
    }
}
