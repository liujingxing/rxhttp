package rxhttp.wrapper.`param`

import android.graphics.Point
import com.example.httpsender.`param`.GetEncryptParam
import java.io.IOException
import java.lang.IllegalArgumentException
import kotlin.Array
import kotlin.CharSequence
import kotlin.collections.MutableList
import kotlin.collections.MutableMap
import kotlin.jvm.Throws

/**
 * Github
 * https://github.com/liujingxing/rxhttp
 * https://github.com/liujingxing/rxlife
 */
public class RxHttpGetEncryptParam : RxHttpNoBodyParam {
  public constructor(`param`: GetEncryptParam) : super(param)

  @Throws(
    IOException::class,
    IllegalArgumentException::class
  )
  public fun <T : Point, R : CharSequence> test(
    a: MutableList<R>,
    map: MutableMap<T, R>,
    vararg b: Array<T>
  ): RxHttpGetEncryptParam {
    (param as GetEncryptParam).test(a,map,*b)
    return this
  }
}
