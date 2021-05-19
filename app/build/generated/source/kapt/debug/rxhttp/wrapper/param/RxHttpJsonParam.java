package rxhttp.wrapper.param;

import com.google.gson.JsonObject;

import java.util.Map;

import rxhttp.wrapper.param.JsonParam;
/**
 * Github
 * https://github.com/liujingxing/rxhttp
 * https://github.com/liujingxing/rxlife
 * https://github.com/liujingxing/rxhttp/wiki/FAQ
 * https://github.com/liujingxing/rxhttp/wiki/更新日志
 */
public class RxHttpJsonParam extends RxHttpAbstractBodyParam<JsonParam, RxHttpJsonParam> {
    public RxHttpJsonParam(JsonParam param) {
        super(param);
    }

    public RxHttpJsonParam add(String key, Object value) {
      param.add(key,value);
      return this;
    }
    
    public RxHttpJsonParam add(String key, Object value, boolean isAdd) {
      if(isAdd) {
        param.add(key,value);
      }
      return this;
    }
    
    public RxHttpJsonParam addAll(Map<String, ?> map) {
      param.addAll(map);
      return this;
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
