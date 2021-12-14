package rxhttp.wrapper.`param`

import com.example.httpsender.`param`.PostEncryptJsonParam1
import kotlin.Unit

/**
 * Github
 * https://github.com/liujingxing/rxhttp
 * https://github.com/liujingxing/rxlife
 */
public class RxHttpPostEncryptJsonParam1 : RxHttp<PostEncryptJsonParam1> {
  public constructor(`param`: PostEncryptJsonParam1) : super(param)

  public fun test(): Unit {
    param.test()
  }
}
