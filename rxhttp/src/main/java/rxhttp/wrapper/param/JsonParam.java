package rxhttp.wrapper.param;


import java.util.LinkedHashMap;
import java.util.Map;

import io.reactivex.annotations.Nullable;
import okhttp3.RequestBody;
import rxhttp.wrapper.utils.GsonUtil;

/**
 * post、put、patch、delete请求，参数以{application/json; charset=utf-8}形式提交
 * User: ljx
 * Date: 2019-09-09
 * Time: 21:08
 */
public class JsonParam extends AbstractParam<JsonParam> implements IJsonObject<JsonParam> {

    private Map<String, Object> mParam; //请求参数

    /**
     * @param url    请求路径
     * @param method {@link Method#POST,Method#PUT,Method#DELETE,Method#PATCH}
     */
    public JsonParam(String url, Method method) {
        super(url, method);
    }

    @Override
    public RequestBody getRequestBody() {
        final Map<String, Object> params = mParam;
        String json = params == null ? "{}" : GsonUtil.toJson(params);
        return RequestBody.create(MEDIA_TYPE_JSON, json);
    }

    @Override
    public JsonParam add(String key, Object value) {
        if (value == null) value = "";
        Map<String, Object> param = mParam;
        if (param == null) {
            param = mParam = new LinkedHashMap<>();
        }
        param.put(key, value);
        return this;
    }

    @Nullable
    public Map<String, Object> getParams() {
        return mParam;
    }

    @Override
    public String toString() {
        return getSimpleUrl() + "\n\nparams = " + GsonUtil.toJson(mParam);
    }
}
