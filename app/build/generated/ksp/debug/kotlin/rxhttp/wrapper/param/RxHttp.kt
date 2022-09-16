package rxhttp.wrapper.`param`

import com.example.httpsender.RxHttpManager.fastJsonConverter
import com.example.httpsender.RxHttpManager.simpleClient
import com.example.httpsender.RxHttpManager.xmlConverter
import com.example.httpsender.`param`.GetEncryptParam
import com.example.httpsender.`param`.PostEncryptFormParam
import com.example.httpsender.`param`.PostEncryptJsonParam
import com.example.httpsender.`param`.PostEncryptJsonParam1
import com.example.httpsender.entity.PageList
import com.example.httpsender.entity.Url.baseUrl
import java.io.IOException
import java.lang.Class
import java.lang.reflect.Type
import java.util.concurrent.TimeUnit
import kotlin.Any
import kotlin.Boolean
import kotlin.Long
import kotlin.String
import kotlin.Suppress
import kotlin.Unit
import kotlin.collections.List
import kotlin.collections.Map
import kotlin.jvm.JvmName
import kotlin.jvm.JvmOverloads
import kotlin.jvm.JvmStatic
import kotlin.jvm.Throws
import okhttp3.CacheControl
import okhttp3.Call
import okhttp3.Headers
import okhttp3.Headers.Builder
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import rxhttp.RxHttpPlugins
import rxhttp.wrapper.cahce.CacheMode
import rxhttp.wrapper.cahce.CacheStrategy
import rxhttp.wrapper.callback.IConverter
import rxhttp.wrapper.entity.DownloadOffSize
import rxhttp.wrapper.entity.ParameterizedTypeImpl
import rxhttp.wrapper.intercept.CacheInterceptor
import rxhttp.wrapper.intercept.LogInterceptor
import rxhttp.wrapper.parse.Parser
import rxhttp.wrapper.parse.SmartParser
import rxhttp.wrapper.utils.LogUtil

/**
 * Github
 * https://github.com/liujingxing/rxhttp
 * https://github.com/liujingxing/rxlife
 * https://github.com/liujingxing/rxhttp/wiki/FAQ
 * https://github.com/liujingxing/rxhttp/wiki/更新日志
 */
@Suppress("UNCHECKED_CAST", "UPPER_BOUND_VIOLATED_BASED_ON_JAVA_ANNOTATIONS")
public open class RxHttp<P : Param<P>, R : RxHttp<P, R>> protected constructor(
  public val `param`: P,
) : BaseRxHttp() {
  private var connectTimeoutMillis: Long = 0L

  private var readTimeoutMillis: Long = 0L

  private var writeTimeoutMillis: Long = 0L

  private var converter: IConverter = RxHttpPlugins.getConverter()

  private var okClient: OkHttpClient = RxHttpPlugins.getOkHttpClient()

  public var request: Request? = null

  @get:JvmName("getUrl")
  public val url: String
    get() {
      addDefaultDomainIfAbsent()
      return param.getUrl()
    }

  @get:JvmName("getSimpleUrl")
  public val simpleUrl: String
    get() = param.getSimpleUrl()

  @get:JvmName("getHeaders")
  public val headers: Headers
    get() = param.getHeaders()

  @get:JvmName("getHeadersBuilder")
  public val headersBuilder: Headers.Builder
    get() = param.getHeadersBuilder()

  @get:JvmName("getCacheStrategy")
  public val cacheStrategy: CacheStrategy
    get() = param.getCacheStrategy()

  private var _okHttpClient: OkHttpClient? = null

  @get:JvmName("getOkHttpClient")
  public val okHttpClient: OkHttpClient
    get() {
      if (_okHttpClient != null) return _okHttpClient!!
      val okClient = this.okClient
      var builder : OkHttpClient.Builder? = null

      if (LogUtil.isDebug()) {
          val b = builder ?: okClient.newBuilder().also { builder = it }
          b.addInterceptor(LogInterceptor(okClient))
      }

      if (connectTimeoutMillis != 0L) {
          val b = builder ?: okClient.newBuilder().also { builder = it }
          b.connectTimeout(connectTimeoutMillis, TimeUnit.MILLISECONDS)
      }

      if (readTimeoutMillis != 0L) {
          val b = builder ?: okClient.newBuilder().also { builder = it }
          b.readTimeout(readTimeoutMillis, TimeUnit.MILLISECONDS)
      }

      if (writeTimeoutMillis != 0L) {
         val b = builder ?: okClient.newBuilder().also { builder = it }
         b.writeTimeout(writeTimeoutMillis, TimeUnit.MILLISECONDS)
      }

      if (param.getCacheMode() != CacheMode.ONLY_NETWORK) {                      
          val b = builder ?: okClient.newBuilder().also { builder = it }        
          b.addInterceptor(CacheInterceptor(cacheStrategy))
      }
                                                                              
      _okHttpClient = builder?.build() ?: okClient
      return _okHttpClient!!
    }

  public fun connectTimeout(connectTimeout: Long): R {
    connectTimeoutMillis = connectTimeout
    return this as R
  }

  public fun readTimeout(readTimeout: Long): R {
    readTimeoutMillis = readTimeout
    return this as R
  }

  public fun writeTimeout(writeTimeout: Long): R {
    writeTimeoutMillis = writeTimeout
    return this as R
  }

  public fun setUrl(url: String): R {
    param.setUrl(url)
    return this as R
  }

  /**
   * For example:
   *                                          
   * ```                                                  
   * RxHttp.get("/service/{page}/...")  
   *     .addPath("page", 1)
   *     ...
   * ```
   * url = /service/1/...
   */
  public fun addPath(name: String, `value`: Any): R {
    param.addPath(name, value)
    return this as R
  }

  public fun addEncodedPath(name: String, `value`: Any): R {
    param.addEncodedPath(name, value)
    return this as R
  }

  public fun addQuery(key: String): R {
    param.addQuery(key, null)
    return this as R
  }

  public fun addEncodedQuery(key: String): R {
    param.addEncodedQuery(key, null)
    return this as R
  }

  public fun addQuery(key: String, `value`: Any?): R {
    param.addQuery(key, value)
    return this as R
  }

  public fun addEncodedQuery(key: String, `value`: Any?): R {
    param.addEncodedQuery(key, value)
    return this as R
  }

  public fun addAllQuery(key: String, list: List<*>): R {
    param.addAllQuery(key, list)
    return this as R
  }

  public fun addAllEncodedQuery(key: String, list: List<*>): R {
    param.addAllEncodedQuery(key, list)
    return this as R
  }

  public fun addAllQuery(map: Map<String, *>): R {
    param.addAllQuery(map)
    return this as R
  }

  public fun addAllEncodedQuery(map: Map<String, *>): R {
    param.addAllEncodedQuery(map)
    return this as R
  }

  @JvmOverloads
  public fun addHeader(line: String, isAdd: Boolean = true): R {
    if (isAdd) param.addHeader(line)
    return this as R
  }

  /**
   * Add a header with the specified name and value. Does validation of header names, allowing
   * non-ASCII values.
   */
  public fun addNonAsciiHeader(key: String, `value`: String): R {
    param.addNonAsciiHeader(key,value)
    return this as R
  }

  /**
   * Set a header with the specified name and value. Does validation of header names, allowing
   * non-ASCII values.
   */
  public fun setNonAsciiHeader(key: String, `value`: String): R {
    param.setNonAsciiHeader(key,value)
    return this as R
  }

  @JvmOverloads
  public fun addHeader(
    key: String,
    `value`: String,
    isAdd: Boolean = true,
  ): R {
    if (isAdd) param.addHeader(key, value)
    return this as R
  }

  public fun addAllHeader(headers: Map<String, String>): R {
    param.addAllHeader(headers)
    return this as R
  }

  public fun addAllHeader(headers: Headers): R {
    param.addAllHeader(headers)
    return this as R
  }

  public fun setHeader(key: String, `value`: String): R {
    param.setHeader(key,value)
    return this as R
  }

  public fun setAllHeader(headers: Map<String, String>): R {
    param.setAllHeader(headers)
    return this as R
  }

  @JvmOverloads
  public fun setRangeHeader(startIndex: Long, endIndex: Long = -1L) = setRangeHeader(startIndex,
      endIndex, false)

  public fun setRangeHeader(startIndex: Long, connectLastProgress: Boolean) =
      setRangeHeader(startIndex, -1, connectLastProgress)

  /**
   * 设置断点下载开始/结束位置
   * @param startIndex 断点下载开始位置
   * @param endIndex 断点下载结束位置，默认为-1，即默认结束位置为文件末尾
   * @param connectLastProgress 是否衔接上次的下载进度，该参数仅在带进度断点下载时生效
   */
  public override fun setRangeHeader(
    startIndex: Long,
    endIndex: Long,
    connectLastProgress: Boolean,
  ): R {
    param.setRangeHeader(startIndex, endIndex)                         
    if (connectLastProgress)                                            
        param.tag(DownloadOffSize::class.java, DownloadOffSize(startIndex))
    return this as R                                                    
  }

  public fun removeAllHeader(key: String): R {
    param.removeAllHeader(key)
    return this as R
  }

  public fun setHeadersBuilder(builder: Headers.Builder): R {
    param.setHeadersBuilder(builder)
    return this as R
  }

  /**
   * 设置单个接口是否需要添加公共参数,
   * 即是否回调通过{@link RxHttpPlugins#setOnParamAssembly(Function)}方法设置的接口,默认为true
   */
  public fun setAssemblyEnabled(enabled: Boolean): R {
    param.setAssemblyEnabled(enabled)
    return this as R
  }

  /**
   * 设置单个接口是否需要对Http返回的数据进行解码/解密,
   * 即是否回调通过{@link RxHttpPlugins#setResultDecoder(Function)}方法设置的接口,默认为true
   */
  public fun setDecoderEnabled(enabled: Boolean): R {
    param.addHeader(Param.DATA_DECRYPT, enabled.toString())
    return this as R
  }

  public fun isAssemblyEnabled() = param.isAssemblyEnabled()

  public fun getHeader(key: String) = param.getHeader(key)

  public fun tag(tag: Any): R {
    param.tag(tag)
    return this as R
  }

  public fun <T> tag(type: Class<in T>, tag: T): R {
    param.tag(type, tag)
    return this as R
  }

  public fun cacheControl(cacheControl: CacheControl): R {
    param.cacheControl(cacheControl)
    return this as R
  }

  public fun setCacheKey(cacheKey: String): R {
    param.setCacheKey(cacheKey)
    return this as R
  }

  public fun setCacheValidTime(cacheValidTime: Long): R {
    param.setCacheValidTime(cacheValidTime)
    return this as R
  }

  public fun setCacheMode(cacheMode: CacheMode): R {
    param.setCacheMode(cacheMode)
    return this as R
  }

  @Throws(IOException::class)
  public fun execute(): Response = newCall().execute()

  @Throws(IOException::class)
  public fun <T> execute(parser: Parser<T>): T = parser.onParse(execute())

  @Throws(IOException::class)
  public fun executeString(): String = executeClass(String::class.java)

  @Throws(IOException::class)
  public fun <T> executeList(clazz: Class<T>): List<T> {
    val tTypeList = ParameterizedTypeImpl.get(List::class.java, clazz)
    return executeClass(tTypeList)
  }

  @Throws(IOException::class)
  public fun <T> executeClass(clazz: Class<T>): T = executeClass(clazz as Type)

  @Throws(IOException::class)
  public fun <T> executeClass(type: Type): T = execute(SmartParser.wrap(type))

  public override fun newCall(): Call {
    val request = buildRequest()
    return okHttpClient.newCall(request)
  }

  public fun buildRequest(): Request {
    if (request == null) {
        doOnStart()
        request = param.buildRequest()
    }
    return request!!
  }

  /**
   * 请求开始前内部调用，用于添加默认域名等操作
   */
  private fun doOnStart(): Unit {
    setConverterToParam(converter)
    addDefaultDomainIfAbsent()
  }

  public fun <T> asResponse(type: Type) = asParser(wrapResponseParser<T>(type))

  public fun <T> asResponse(type: Class<T>): ObservableParser<T> = asResponse(type as Type)

  public fun <T> asResponseList(type: Class<T>): ObservableParser<List<T>> {
    val typeList = ParameterizedTypeImpl.get(List::class.java, type)
    return asResponse(typeList)
  }

  public fun <T> asResponsePageList(type: Class<T>): ObservableParser<PageList<T>> {
    val typePageList = ParameterizedTypeImpl.get(PageList::class.java, type)
    return asResponse(typePageList)
  }

  public fun setXmlConverter() = setConverter(xmlConverter)

  public fun setFastJsonConverter() = setConverter(fastJsonConverter)

  public fun setConverter(converter: IConverter): R {
    this.converter = converter
    return this as R
  }

  /**
   * 给Param设置转换器，此方法会在请求发起前，被RxHttp内部调用
   */
  private fun setConverterToParam(converter: IConverter): R {
    param.tag(IConverter::class.java, converter)
    return this as R
  }

  public fun setOkClient(okClient: OkHttpClient): R {
    this.okClient = okClient
    return this as R
  }

  public fun setSimpleClient() = setOkClient(simpleClient)

  /**
   * 给Param设置默认域名(如果缺席的话)，此方法会在请求发起前，被RxHttp内部调用
   */
  private fun addDefaultDomainIfAbsent() = setDomainIfAbsent(baseUrl)

  public fun setDomainIfAbsent(domain: String): R {
    val newUrl = addDomainIfAbsent(param.getSimpleUrl(), domain)
    param.setUrl(newUrl)
    return this as R
  }

  private fun addDomainIfAbsent(url: String, domain: String): String = if (url.startsWith("http")) {
      url                             
  } else if (url.startsWith("/")) {   
      if (domain.endsWith("/"))       
          domain + url.substring(1)  
      else                            
          domain + url               
  } else if (domain.endsWith("/")) {  
      domain + url                   
  } else {                            
      domain + "/" + url             
  }                                   

  public companion object {
    /**
     * For example:
     *                                          
     * ```                                                  
     * RxHttp.get("/service/%d/...", 1)  
     *     .addQuery("size", 20)
     *     ...
     * ```
     *  url = /service/1/...?size=20
     */
    @JvmStatic
    public fun `get`(url: String, vararg formatArgs: Any?) = RxHttpNoBodyParam(Param.get(format(url,
        *formatArgs)))

    @JvmStatic
    public fun head(url: String, vararg formatArgs: Any?) = RxHttpNoBodyParam(Param.head(format(url,
        *formatArgs)))

    @JvmStatic
    public fun postBody(url: String, vararg formatArgs: Any?) =
        RxHttpBodyParam(Param.postBody(format(url, *formatArgs)))

    @JvmStatic
    public fun putBody(url: String, vararg formatArgs: Any?) =
        RxHttpBodyParam(Param.putBody(format(url, *formatArgs)))

    @JvmStatic
    public fun patchBody(url: String, vararg formatArgs: Any?) =
        RxHttpBodyParam(Param.patchBody(format(url, *formatArgs)))

    @JvmStatic
    public fun deleteBody(url: String, vararg formatArgs: Any?) =
        RxHttpBodyParam(Param.deleteBody(format(url, *formatArgs)))

    @JvmStatic
    public fun postForm(url: String, vararg formatArgs: Any?) =
        RxHttpFormParam(Param.postForm(format(url, *formatArgs)))

    @JvmStatic
    public fun putForm(url: String, vararg formatArgs: Any?) =
        RxHttpFormParam(Param.putForm(format(url, *formatArgs)))

    @JvmStatic
    public fun patchForm(url: String, vararg formatArgs: Any?) =
        RxHttpFormParam(Param.patchForm(format(url, *formatArgs)))

    @JvmStatic
    public fun deleteForm(url: String, vararg formatArgs: Any?) =
        RxHttpFormParam(Param.deleteForm(format(url, *formatArgs)))

    @JvmStatic
    public fun postJson(url: String, vararg formatArgs: Any?) =
        RxHttpJsonParam(Param.postJson(format(url, *formatArgs)))

    @JvmStatic
    public fun putJson(url: String, vararg formatArgs: Any?) =
        RxHttpJsonParam(Param.putJson(format(url, *formatArgs)))

    @JvmStatic
    public fun patchJson(url: String, vararg formatArgs: Any?) =
        RxHttpJsonParam(Param.patchJson(format(url, *formatArgs)))

    @JvmStatic
    public fun deleteJson(url: String, vararg formatArgs: Any?) =
        RxHttpJsonParam(Param.deleteJson(format(url, *formatArgs)))

    @JvmStatic
    public fun postJsonArray(url: String, vararg formatArgs: Any?) =
        RxHttpJsonArrayParam(Param.postJsonArray(format(url, *formatArgs)))

    @JvmStatic
    public fun putJsonArray(url: String, vararg formatArgs: Any?) =
        RxHttpJsonArrayParam(Param.putJsonArray(format(url, *formatArgs)))

    @JvmStatic
    public fun patchJsonArray(url: String, vararg formatArgs: Any?) =
        RxHttpJsonArrayParam(Param.patchJsonArray(format(url, *formatArgs)))

    @JvmStatic
    public fun deleteJsonArray(url: String, vararg formatArgs: Any?) =
        RxHttpJsonArrayParam(Param.deleteJsonArray(format(url, *formatArgs)))

    @JvmStatic
    public fun postEncryptJson(url: String, vararg formatArgs: Any?) =
        RxHttpPostEncryptJsonParam(PostEncryptJsonParam(format(url, *formatArgs)))

    @JvmStatic
    public fun postEncryptForm(url: String, vararg formatArgs: Any?) =
        RxHttpPostEncryptFormParam(PostEncryptFormParam(format(url, *formatArgs)))

    @JvmStatic
    public fun postEncryptForm(
      url: String,
      method: Method,
      vararg formatArgs: Any?,
    ) = RxHttpPostEncryptFormParam(PostEncryptFormParam(format(url, *formatArgs), method))

    @JvmStatic
    public fun getEncrypt(url: String, vararg formatArgs: Any?) =
        RxHttpGetEncryptParam(GetEncryptParam(format(url, *formatArgs)))

    @JvmStatic
    public fun postEncryptJson1(url: String, vararg formatArgs: Any?) =
        RxHttpPostEncryptJsonParam1(PostEncryptJsonParam1(format(url, *formatArgs)))

    /**
     * Returns a formatted string using the specified format string and arguments.
     */
    private fun format(url: String, vararg formatArgs: Any?) = if(formatArgs.isNullOrEmpty()) url
        else String.format(url, *formatArgs)
  }
}
