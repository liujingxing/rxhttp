package rxhttp.wrapper.`param`

import com.example.httpsender.`param`.PostEncryptFormParam
import kotlin.Float
import kotlin.Int
import kotlin.Long
import kotlin.String

/**
 * Github
 * https://github.com/liujingxing/rxhttp
 * https://github.com/liujingxing/rxlife
 */
public class RxHttpPostEncryptFormParam : RxHttpFormParam {
  public constructor(`param`: PostEncryptFormParam) : super(param)

  public fun test1(s: String): RxHttpPostEncryptFormParam {
    (param as PostEncryptFormParam).test1(s)
    return this
  }

  public fun test2(a: Long, b: Float): RxHttpPostEncryptFormParam {
    (param as PostEncryptFormParam).test2(a,b)
    return this
  }

  public fun add(a: Int, b: Int): Int = (param as PostEncryptFormParam).add(a,b)
}
