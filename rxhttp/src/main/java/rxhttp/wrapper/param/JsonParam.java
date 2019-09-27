package rxhttp.wrapper.param;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.RequestBody;

/**
 * post、put、patch、delete请求，参数以{application/json;charset=utf-8}形式提交
 * User: ljx
 * Date: 2019-09-09
 * Time: 21:08
 */
public class JsonParam extends AbstractParam<JsonParam> {

    public static final MediaType MEDIA_TYPE_JSON = MediaType.parse("application/json;charset=utf-8");

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
        String json = params == null ? "" : new JSONObject(params).toString();
        return RequestBody.create(MEDIA_TYPE_JSON, json);
    }

    @Deprecated
    public JsonParam setJsonParams(String jsonParams) {
        return addJsonParams(jsonParams);
    }

    public JsonParam addJsonParams(String jsonParams) {
        JSONObject jsonObject = null;
        try {
            jsonObject = new JSONObject(jsonParams);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return addJsonParams(jsonObject);
    }

    public JsonParam addJsonParams(JSONObject jsonObject) {
        if (jsonObject == null) {
            return this;
        }
        Iterator<String> keys = jsonObject.keys();
        while (keys.hasNext()) {
            String key = keys.next();
            add(key, jsonObject.opt(key));
        }
        return this;
    }
}
