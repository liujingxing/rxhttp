package rxhttp.wrapper.param;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.List;
import java.util.Map;

import rxhttp.wrapper.param.JsonArrayParam;

/**
 * Github
 * https://github.com/liujingxing/rxhttp
 * https://github.com/liujingxing/rxlife
 * https://github.com/liujingxing/rxhttp/wiki/FAQ
 * https://github.com/liujingxing/rxhttp/wiki/更新日志
 */
public class RxHttpJsonArrayParam extends RxHttpAbstractBodyParam<JsonArrayParam, RxHttpJsonArrayParam> {
    public RxHttpJsonArrayParam(JsonArrayParam param) {
        super(param);
    }

    public RxHttpJsonArrayParam add(String key, Object value) {
      param.add(key,value);
      return this;
    }
    
    public RxHttpJsonArrayParam add(String key, Object value, boolean isAdd) {
      if(isAdd) {
        param.add(key,value);
      }
      return this;
    }
    
    public RxHttpJsonArrayParam addAll(Map<String, ?> map) {
      param.addAll(map);
      return this;
    }

    public RxHttpJsonArrayParam add(Object object) {
        param.add(object);
        return this;
    }

    public RxHttpJsonArrayParam addAll(List<?> list) {
        param.addAll(list);
        return this;
    }

    /**
     * 添加多个对象，将字符串转JsonElement对象,并根据不同类型,执行不同操作,可输入任意非空字符串
     */
    public RxHttpJsonArrayParam addAll(String jsonElement) {
        param.addAll(jsonElement);
        return this;
    }

    public RxHttpJsonArrayParam addAll(JsonArray jsonArray) {
        param.addAll(jsonArray);
        return this;
    }

    /**
     * 将Json对象里面的key-value逐一取出，添加到Json数组中，成为单独的对象
     */
    public RxHttpJsonArrayParam addAll(JsonObject jsonObject) {
        param.addAll(jsonObject);
        return this;
    }

    public RxHttpJsonArrayParam addJsonElement(String jsonElement) {
        param.addJsonElement(jsonElement);
        return this;
    }

    /**
     * 添加一个JsonElement对象(Json对象、json数组等)
     */
    public RxHttpJsonArrayParam addJsonElement(String key, String jsonElement) {
        param.addJsonElement(key, jsonElement);
        return this;
    }
}
