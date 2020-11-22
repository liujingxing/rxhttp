package rxhttp.wrapper.param;

import com.google.gson.JsonObject;

import rxhttp.wrapper.param.JsonParam;
/**
 * Github
 * https://github.com/liujingxing/RxHttp
 * https://github.com/liujingxing/RxLife
 * https://github.com/liujingxing/okhttp-RxHttp/wiki/FAQ
 * https://github.com/liujingxing/okhttp-RxHttp/wiki/更新日志
 */
public class RxHttpJsonParam extends RxHttpBodyParam<JsonParam, RxHttpJsonParam> {
    public RxHttpJsonParam(JsonParam param) {
        super(param);
    }

    /**
     * 将Json对象里面的key-value逐一取出，添加到另一个Json对象中，
     * 输入非Json对象将抛出{@link IllegalStateException}异常
     */
    public RxHttpJsonParam addAll(String jsonObject) {
        param.addAll(jsonObject);
        return this;
    }

    /**
     * 将Json对象里面的key-value逐一取出，添加到另一个Json对象中
     */
    public RxHttpJsonParam addAll(JsonObject jsonObject) {
        param.addAll(jsonObject);
        return this;
    }

    /**
     * 添加一个JsonElement对象(Json对象、json数组等)
     */
    public RxHttpJsonParam addJsonElement(String key, String jsonElement) {
        param.addJsonElement(key, jsonElement);
        return this;
    }
}
