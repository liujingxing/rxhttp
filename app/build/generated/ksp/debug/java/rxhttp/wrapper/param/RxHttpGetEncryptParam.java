package rxhttp.wrapper.param;

import android.graphics.Point;
import com.example.httpsender.param.GetEncryptParam;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Github
 * https://github.com/liujingxing/rxhttp
 * https://github.com/liujingxing/rxlife
 */
public class RxHttpGetEncryptParam extends RxHttpNoBodyParam {
  public RxHttpGetEncryptParam(GetEncryptParam param) {
    super(param);
  }

  public <T extends Point, R extends CharSequence> RxHttpGetEncryptParam test(List<R> a,
      Map<T, R> map, T[]... b) throws IOException, IllegalArgumentException {
    ((GetEncryptParam)param).test(a,map,b);
    return this;
  }
}
