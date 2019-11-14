package rxhttp.wrapper.param;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import io.reactivex.annotations.NonNull;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import rxhttp.wrapper.utils.GsonUtil;

/**
 * post、put、patch、delete请求，参数以{application/json; charset=utf-8}形式提交
 * User: ljx
 * Date: 2019-09-09
 * Time: 21:08
 */
public class JsonArrayParam extends AbstractParam<JsonArrayParam> implements IJsonArray<JsonArrayParam> {

    public static final MediaType MEDIA_TYPE_JSON = MediaType.parse("application/json; charset=utf-8");

    private List<Object> mList;

    /**
     * @param url    请求路径
     * @param method {@link Method#POST, Method#PUT, Method#DELETE, Method#PATCH}
     */
    public JsonArrayParam(String url, Method method) {
        super(url, method);
    }

    @Override
    public RequestBody getRequestBody() {
        final List<?> jsonArray = mList;
        String json = jsonArray == null ? "[]" : GsonUtil.toJson(mList);
        return RequestBody.create(MEDIA_TYPE_JSON, json);
    }

    public JsonArrayParam add(@NonNull Object object) {
        List<Object> list = mList;
        if (list == null) {
            list = mList = new ArrayList<>();
        }
        list.add(object);
        return this;
    }

    @Override
    public JsonArrayParam add(String key, Object value) {
        HashMap<String, Object> map = new HashMap<>();
        map.put(key, value);
        return add(map);
    }

    @Override
    public String toString() {
        return getSimpleUrl() + "\n\nparams = " + GsonUtil.toJson(mList);
    }
}
