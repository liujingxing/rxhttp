package rxhttp.wrapper.param;

import com.example.httpsender.param.PostEncryptFormParam;
import java.lang.String;
import java.util.List;

/**
 * Github
 * https://github.com/liujingxing/rxhttp
 * https://github.com/liujingxing/rxlife
 */
public class RxHttpPostEncryptFormParam extends RxHttpFormParam {
  public RxHttpPostEncryptFormParam(PostEncryptFormParam param) {
    super(param);
  }

  public RxHttpPostEncryptFormParam addAllQuery(String key, List<String> param) {
    ((PostEncryptFormParam)param).addAllQuery(key,param);
    return this;
  }

  public RxHttpPostEncryptFormParam test1(String s) {
    ((PostEncryptFormParam)param).test1(s);
    return this;
  }

  public RxHttpPostEncryptFormParam test2(long a, float b) {
    ((PostEncryptFormParam)param).test2(a,b);
    return this;
  }

  public int add(int a, int b) {
    return ((PostEncryptFormParam)param).add(a,b);
  }
}
