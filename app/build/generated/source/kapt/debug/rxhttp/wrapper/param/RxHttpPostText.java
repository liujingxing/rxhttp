package rxhttp.wrapper.param;

import com.example.httpsender.param.PostText;
import java.lang.String;

/**
 * Github
 * https://github.com/liujingxing/RxHttp
 * https://github.com/liujingxing/RxLife
 */
public class RxHttpPostText extends RxHttpJsonParam {
  public RxHttpPostText(PostText param) {
    super(param);
  }

  public RxHttpPostText setText(String text) {
    ((PostText)param).setText(text);
    return this;
  }
}
