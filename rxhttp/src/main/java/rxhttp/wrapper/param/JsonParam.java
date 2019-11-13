package rxhttp.wrapper.param;

import java.util.HashMap;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import rxhttp.wrapper.utils.GsonUtil;

/**
 * post、put、patch、delete请求，参数以{application/json;charset=utf-8}形式提交
 * User: ljx
 * Date: 2019-09-09
 * Time: 21:08
 */
public class JsonParam extends AbstractParam<JsonParam> implements IJsonObject<JsonParam>{

    public static final MediaType MEDIA_TYPE_JSON = MediaType.parse("application/json; charset=utf-8");

    /**
     * @param url    请求路径
     * @param method {@link Method#POST,Method#PUT,Method#DELETE,Method#PATCH}
     */
    public JsonParam(String url, Method method) {
        super(url, method);
    }

    @Override
    public RequestBody getRequestBody() {
        final Map<String, Object> params = getParams();
        String json = params == null ? "" : GsonUtil.toJson(params);
        return RequestBody.create(MEDIA_TYPE_JSON, json);
    }

    /**
     * @deprecated Use {@link #addAll(String)} instead.
     */
    @Deprecated
    public JsonParam setJsonParams(String jsonParams) {
        return addAll(jsonParams);
    }

    /**
     * @deprecated Use {@link #addAll(String)} instead.
     */
    @Deprecated
    public JsonParam addJsonParams(String jsonParams) {
        return addAll(jsonParams);
    }
}
