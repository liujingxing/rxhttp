package rxhttp.wrapper.`param`

import kotlin.Any
import kotlin.String
import kotlin.jvm.JvmStatic

/**
 * 本类由@Converter、@Domain、@OkClient注解中的className字段生成  类命名方式: Rx + {className字段值} + Http
 * Github
 * https://github.com/liujingxing/rxhttp
 * https://github.com/liujingxing/rxlife
 * https://github.com/liujingxing/rxhttp/wiki/FAQ
 * https://github.com/liujingxing/rxhttp/wiki/更新日志
 */
public object RxSimpleHttp {
  /**
   * 本类所有方法都会调用本方法
   */
  private fun <R : RxHttp<*, *>> R.wrapper(): R {
    setSimpleClient()
    setDomainToUpdateIfAbsent()
    return this
  }

  @JvmStatic
  public fun `get`(url: String, vararg formatArgs: Any?) = RxHttp.get(url, *formatArgs).wrapper()

  @JvmStatic
  public fun head(url: String, vararg formatArgs: Any?) = RxHttp.head(url, *formatArgs).wrapper()

  @JvmStatic
  public fun postBody(url: String, vararg formatArgs: Any?) = RxHttp.postBody(url,
      *formatArgs).wrapper()

  @JvmStatic
  public fun putBody(url: String, vararg formatArgs: Any?) = RxHttp.putBody(url,
      *formatArgs).wrapper()

  @JvmStatic
  public fun patchBody(url: String, vararg formatArgs: Any?) = RxHttp.patchBody(url,
      *formatArgs).wrapper()

  @JvmStatic
  public fun deleteBody(url: String, vararg formatArgs: Any?) = RxHttp.deleteBody(url,
      *formatArgs).wrapper()

  @JvmStatic
  public fun postForm(url: String, vararg formatArgs: Any?) = RxHttp.postForm(url,
      *formatArgs).wrapper()

  @JvmStatic
  public fun putForm(url: String, vararg formatArgs: Any?) = RxHttp.putForm(url,
      *formatArgs).wrapper()

  @JvmStatic
  public fun patchForm(url: String, vararg formatArgs: Any?) = RxHttp.patchForm(url,
      *formatArgs).wrapper()

  @JvmStatic
  public fun deleteForm(url: String, vararg formatArgs: Any?) = RxHttp.deleteForm(url,
      *formatArgs).wrapper()

  @JvmStatic
  public fun postJson(url: String, vararg formatArgs: Any?) = RxHttp.postJson(url,
      *formatArgs).wrapper()

  @JvmStatic
  public fun putJson(url: String, vararg formatArgs: Any?) = RxHttp.putJson(url,
      *formatArgs).wrapper()

  @JvmStatic
  public fun patchJson(url: String, vararg formatArgs: Any?) = RxHttp.patchJson(url,
      *formatArgs).wrapper()

  @JvmStatic
  public fun deleteJson(url: String, vararg formatArgs: Any?) = RxHttp.deleteJson(url,
      *formatArgs).wrapper()

  @JvmStatic
  public fun postJsonArray(url: String, vararg formatArgs: Any?) = RxHttp.postJsonArray(url,
      *formatArgs).wrapper()

  @JvmStatic
  public fun putJsonArray(url: String, vararg formatArgs: Any?) = RxHttp.putJsonArray(url,
      *formatArgs).wrapper()

  @JvmStatic
  public fun patchJsonArray(url: String, vararg formatArgs: Any?) = RxHttp.patchJsonArray(url,
      *formatArgs).wrapper()

  @JvmStatic
  public fun deleteJsonArray(url: String, vararg formatArgs: Any?) = RxHttp.deleteJsonArray(url,
      *formatArgs).wrapper()

  @JvmStatic
  public fun postEncryptJson(url: String, vararg formatArgs: Any?) = RxHttp.postEncryptJson(url,
      *formatArgs).wrapper()

  @JvmStatic
  public fun postEncryptForm(url: String, vararg formatArgs: Any?) = RxHttp.postEncryptForm(url,
      *formatArgs).wrapper()

  @JvmStatic
  public fun postEncryptForm(
    url: String,
    method: Method,
    vararg formatArgs: Any?,
  ) = RxHttp.postEncryptForm(url, method, *formatArgs).wrapper()

  @JvmStatic
  public fun getEncrypt(url: String, vararg formatArgs: Any?) = RxHttp.getEncrypt(url,
      *formatArgs).wrapper()

  @JvmStatic
  public fun postEncryptJson1(url: String, vararg formatArgs: Any?) = RxHttp.postEncryptJson1(url,
      *formatArgs).wrapper()
}
