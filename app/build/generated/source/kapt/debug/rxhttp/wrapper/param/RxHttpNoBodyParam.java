package rxhttp.wrapper.param;

import java.lang.Object;
import java.lang.String;
import java.util.List;
import java.util.Map;

/**
 * Github
 * https://github.com/liujingxing/RxHttp
 * https://github.com/liujingxing/RxLife
 */
public class RxHttpNoBodyParam extends RxHttp<NoBodyParam, RxHttpNoBodyParam> {
  public RxHttpNoBodyParam(NoBodyParam param) {
    super(param);
  }

  public RxHttpNoBodyParam add(String key, Object value) {
    param.add(key,value);
    return this;
  }

  public RxHttpNoBodyParam addEncoded(String key, Object value) {
    param.addEncoded(key,value);
    return this;
  }

  public RxHttpNoBodyParam add(String key, Object value, boolean isAdd) {
    if(isAdd) {
      param.add(key,value);
    }
    return this;
  }

  public RxHttpNoBodyParam addAll(Map<? extends String, ?> map) {
    param.addAll(map);
    return this;
  }

  public RxHttpNoBodyParam removeAllBody() {
    param.removeAllBody();
    return this;
  }

  public RxHttpNoBodyParam removeAllBody(String key) {
    param.removeAllBody(key);
    return this;
  }

  public RxHttpNoBodyParam set(String key, Object value) {
    param.set(key,value);
    return this;
  }

  public RxHttpNoBodyParam setEncoded(String key, Object value) {
    param.setEncoded(key,value);
    return this;
  }

  public Object queryValue(String key) {
    return param.queryValue(key);
  }

  public List<Object> queryValues(String key) {
    return param.queryValues(key);
  }
}
