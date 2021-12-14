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
import com.example.httpsender.entity.Url.update
import com.example.httpsender.parser.ResponseParser
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Scheduler
import io.reactivex.rxjava3.functions.Consumer
import java.io.IOException
import java.lang.Class
import java.util.concurrent.TimeUnit
import kotlin.Any
import kotlin.Boolean
import kotlin.Deprecated
import kotlin.Long
import kotlin.String
import kotlin.Unit
import kotlin.collections.List
import kotlin.collections.Map
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
import rxhttp.wrapper.callback.IConverter
import rxhttp.wrapper.entity.DownloadOffSize
import rxhttp.wrapper.entity.ParameterizedTypeImpl
import rxhttp.wrapper.entity.Progress
import rxhttp.wrapper.intercept.CacheInterceptor
import rxhttp.wrapper.intercept.LogInterceptor
import rxhttp.wrapper.parse.Parser
import rxhttp.wrapper.parse.SimpleParser
import rxhttp.wrapper.utils.LogUtil

/**
 * Github
 * https://github.com/liujingxing/rxhttp
 * https://github.com/liujingxing/rxlife
 * https://github.com/liujingxing/rxhttp/wiki/FAQ
 * https://github.com/liujingxing/rxhttp/wiki/更新日志
 */
public open class RxHttp<P : Param<P>, R : RxHttp<P, R>> protected constructor(
  public val `param`: P
) : BaseRxHttp() {
  private var connectTimeoutMillis: Long = 0L

  private var readTimeoutMillis: Long = 0L

  private var writeTimeoutMillis: Long = 0L

  private var realOkClient: OkHttpClient? = null

  private var okClient: OkHttpClient = RxHttpPlugins.getOkHttpClient()

  protected var converter: IConverter = RxHttpPlugins.getConverter()
    private set

  protected var isAsync: Boolean = true

  public var request: Request? = null

  public fun connectTimeout(connectTimeout: Long) = apply { connectTimeoutMillis = connectTimeout }

  public fun readTimeout(readTimeout: Long) = apply { readTimeoutMillis = readTimeout }

  public fun writeTimeout(writeTimeout: Long) = apply { writeTimeoutMillis = writeTimeout }

  public fun setUrl(url: String) = apply { param.setUrl(url) }

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
  public fun addPath(name: String, `value`: Any) = apply { param.addPath(name, value) }

  public fun addEncodedPath(name: String, `value`: Any) = apply {
    param.addEncodedPath(name, value)
  }

  public fun addQuery(key: String) = apply { param.addQuery(key, null) }

  public fun addEncodedQuery(key: String) = apply {
    param.addEncodedQuery(key, null)
  }

  public fun addQuery(key: String, `value`: Any?) = apply { param.addQuery(key, value) }

  public fun addEncodedQuery(key: String, `value`: Any?) = apply {
    param.addEncodedQuery(key, value)
  }

  public fun addAllQuery(key: String, list: List<*>) = apply { param.addAllQuery(key, list) }

  public fun addAllEncodedQuery(key: String, list: List<*>) = apply {
    param.addAllEncodedQuery(key, list)
  }

  public fun addAllQuery(map: Map<String, *>) = apply { param.addAllQuery(map) }

  public fun addAllEncodedQuery(map: Map<String, *>) = apply { param.addAllEncodedQuery(map) }

  @JvmOverloads
  public fun addHeader(line: String, isAdd: Boolean = true) = apply {
    if (isAdd) param.addHeader(line)
  }

  /**
   * Add a header with the specified name and value. Does validation of header names, allowing
   * non-ASCII values.
   */
  public fun addNonAsciiHeader(key: String, `value`: String) = apply {
    param.addNonAsciiHeader(key, value)
  }

  /**
   * Set a header with the specified name and value. Does validation of header names, allowing
   * non-ASCII values.
   */
  public fun setNonAsciiHeader(key: String, `value`: String) = apply {
    param.setNonAsciiHeader(key, value)
  }

  @JvmOverloads
  public fun addHeader(
    key: String,
    `value`: String,
    isAdd: Boolean = true
  ) = apply {
    if (isAdd) param.addHeader(key, value)
  }

  public fun addAllHeader(headers: Map<String, String>) = apply { param.addAllHeader(headers) }

  public fun addAllHeader(headers: Headers) = apply { param.addAllHeader(headers) }

  public fun setHeader(key: String, `value`: String) = apply { param.setHeader(key, value) }

  public fun setAllHeader(headers: Map<String, String>) = apply { param.setAllHeader(headers) }

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
    connectLastProgress: Boolean
  ) = apply {
    param.setRangeHeader(startIndex, endIndex)                         
    if (connectLastProgress)                                            
        param.tag(DownloadOffSize::class.java, DownloadOffSize(startIndex))
  }    

  public fun removeAllHeader(key: String) = apply { param.removeAllHeader(key) }

  public fun setHeadersBuilder(builder: Headers.Builder) = apply {
    param.setHeadersBuilder(builder)
  }

  /**
   * 设置单个接口是否需要添加公共参数,
   * 即是否回调通过{@link RxHttpPlugins#setOnParamAssembly(Function)}方法设置的接口,默认为true
   */
  public fun setAssemblyEnabled(enabled: Boolean) = apply { param.setAssemblyEnabled(enabled) }

  /**
   * 设置单个接口是否需要对Http返回的数据进行解码/解密,
   * 即是否回调通过{@link RxHttpPlugins#setResultDecoder(Function)}方法设置的接口,默认为true
   */
  public fun setDecoderEnabled(enabled: Boolean) = apply {
    param.addHeader(Param.DATA_DECRYPT, enabled.toString())
  }

  public fun isAssemblyEnabled() = param.isAssemblyEnabled()

  public fun getUrl(): String {
    addDefaultDomainIfAbsent()
    return param.getUrl()
  }

  public fun getSimpleUrl() = param.getSimpleUrl()

  public fun getHeader(key: String) = param.getHeader(key)

  public fun getHeaders() = param.getHeaders()

  public fun getHeadersBuilder() = param.getHeadersBuilder()

  public fun tag(tag: Any) = apply { param.tag(tag) }

  public fun <T> tag(type: Class<in T>, tag: T) = apply { param.tag(type, tag) }

  public fun cacheControl(cacheControl: CacheControl) = apply { param.cacheControl(cacheControl) }

  public fun getCacheStrategy() = param.getCacheStrategy()

  public fun setCacheKey(cacheKey: String) = apply { param.setCacheKey(cacheKey) }

  public fun setCacheValidTime(cacheValidTime: Long) = apply {
    param.setCacheValidTime(cacheValidTime)
  }

  public fun setCacheMode(cacheMode: CacheMode) = apply { param.setCacheMode(cacheMode) }

  @Throws(IOException::class)
  public fun execute(): Response = newCall().execute()

  @Throws(IOException::class)
  public fun <T> execute(parser: Parser<T>): T = parser.onParse(execute())

  @Throws(IOException::class)
  public fun executeString(): String = executeClass(String::class.java)

  @Throws(IOException::class)
  public fun <T> executeList(type: Class<T>): List<T> {
    val tTypeList = ParameterizedTypeImpl.get(List::class.java, type)
    return execute(SimpleParser(tTypeList))
  }

  @Throws(IOException::class)
  public fun <T> executeClass(type: Class<T>): T = execute(SimpleParser(type))

  public override fun newCall(): Call {
    val request = buildRequest()
    val okClient = getOkHttpClient()
    return okClient.newCall(request)
  }

  public fun buildRequest(): Request {
    if (request == null) {
        doOnStart()
        request = param.buildRequest()
    }
    return request!!
  }

  public fun getOkHttpClient(): OkHttpClient {
    if (realOkClient != null) return realOkClient!!
    val okClient = this.okClient
    var builder : OkHttpClient.Builder? = null

    if (LogUtil.isDebug()) {
        builder = okClient.newBuilder()
        builder.addInterceptor(LogInterceptor(okClient))
    }

    if (connectTimeoutMillis != 0L) {
        if (builder == null) builder = okClient.newBuilder()
        builder.connectTimeout(connectTimeoutMillis, TimeUnit.MILLISECONDS)
    }

    if (readTimeoutMillis != 0L) {
        if (builder == null) builder = okClient.newBuilder()
        builder.readTimeout(readTimeoutMillis, TimeUnit.MILLISECONDS)
    }

    if (writeTimeoutMillis != 0L) {
       if (builder == null) builder = okClient.newBuilder()
       builder.writeTimeout(writeTimeoutMillis, TimeUnit.MILLISECONDS)
    }

    if (param.getCacheMode() != CacheMode.ONLY_NETWORK) {                      
        if (builder == null) builder = okClient.newBuilder()           
        builder.addInterceptor(CacheInterceptor(getCacheStrategy()))
    }
                                                                            
    realOkClient = builder?.build() ?: okClient
    return realOkClient!!
  }

  /**
   * 请求开始前内部调用，用于添加默认域名等操作
   */
  private fun doOnStart(): Unit {
    setConverterToParam(converter)
    addDefaultDomainIfAbsent()
  }

  @Deprecated("please use `setSync()` instead",
  ReplaceWith("setSync()"),
  DeprecationLevel.ERROR)
  public fun subscribeOnCurrent() = setSync()

  /**
   * sync request 
   */
  public fun setSync() = apply { isAsync = false }

  public override fun <T> asParser(
    parser: Parser<T>,
    scheduler: Scheduler?,
    progressConsumer: Consumer<Progress>?
  ): Observable<T> {
    val observableCall = if(isAsync) ObservableCallEnqueue(this)
        else ObservableCallExecute(this)                                
    return observableCall.asParser(parser, scheduler, progressConsumer)
  }

  public fun <T> asResponse(type: Class<T>): Observable<T> = asParser(ResponseParser(type))

  public fun <T> asResponseList(type: Class<T>): Observable<List<T>> {
    val typeList = ParameterizedTypeImpl.get(List::class.java, type)
    return asParser(ResponseParser(typeList))
  }

  public fun <T> asResponsePageList(type: Class<T>): Observable<PageList<T>> {
    val typePageList = ParameterizedTypeImpl.get(PageList::class.java, type)
    return asParser(ResponseParser(typePageList))
  }

  public fun setXmlConverter() = setConverter(xmlConverter)

  public fun setFastJsonConverter() = setConverter(fastJsonConverter)

  public fun setConverter(converter: IConverter) = apply { this.converter = converter }

  /**
   * 给Param设置转换器，此方法会在请求发起前，被RxHttp内部调用
   */
  private fun setConverterToParam(converter: IConverter) = apply {
    param.tag(IConverter::class.java, converter)
  }

  public fun setOkClient(okClient: OkHttpClient) = apply { this.okClient = okClient }

  public fun setSimpleClient() = setOkClient(simpleClient)

  /**
   * 给Param设置默认域名(如果缺席的话)，此方法会在请求发起前，被RxHttp内部调用
   */
  private fun addDefaultDomainIfAbsent() = setDomainIfAbsent(baseUrl)

  public fun setDomainToUpdateIfAbsent() = setDomainIfAbsent(update)

  public fun setDomainIfAbsent(domain: String) = apply {    
    val newUrl = addDomainIfAbsent(param.getSimpleUrl(), domain)
    param.setUrl(newUrl)
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
    public fun `get`(url: String, vararg formatArgs: Any) = RxHttpNoBodyParam(Param.get(format(url,
        formatArgs)))

    @JvmStatic
    public fun head(url: String, vararg formatArgs: Any) = RxHttpNoBodyParam(Param.head(format(url,
        formatArgs)))

    @JvmStatic
    public fun postBody(url: String, vararg formatArgs: Any) =
        RxHttpBodyParam(Param.postBody(format(url, formatArgs)))

    @JvmStatic
    public fun putBody(url: String, vararg formatArgs: Any) =
        RxHttpBodyParam(Param.putBody(format(url, formatArgs)))

    @JvmStatic
    public fun patchBody(url: String, vararg formatArgs: Any) =
        RxHttpBodyParam(Param.patchBody(format(url, formatArgs)))

    @JvmStatic
    public fun deleteBody(url: String, vararg formatArgs: Any) =
        RxHttpBodyParam(Param.deleteBody(format(url, formatArgs)))

    @JvmStatic
    public fun postForm(url: String, vararg formatArgs: Any) =
        RxHttpFormParam(Param.postForm(format(url, formatArgs)))

    @JvmStatic
    public fun putForm(url: String, vararg formatArgs: Any) =
        RxHttpFormParam(Param.putForm(format(url, formatArgs)))

    @JvmStatic
    public fun patchForm(url: String, vararg formatArgs: Any) =
        RxHttpFormParam(Param.patchForm(format(url, formatArgs)))

    @JvmStatic
    public fun deleteForm(url: String, vararg formatArgs: Any) =
        RxHttpFormParam(Param.deleteForm(format(url, formatArgs)))

    @JvmStatic
    public fun postJson(url: String, vararg formatArgs: Any) =
        RxHttpJsonParam(Param.postJson(format(url, formatArgs)))

    @JvmStatic
    public fun putJson(url: String, vararg formatArgs: Any) =
        RxHttpJsonParam(Param.putJson(format(url, formatArgs)))

    @JvmStatic
    public fun patchJson(url: String, vararg formatArgs: Any) =
        RxHttpJsonParam(Param.patchJson(format(url, formatArgs)))

    @JvmStatic
    public fun deleteJson(url: String, vararg formatArgs: Any) =
        RxHttpJsonParam(Param.deleteJson(format(url, formatArgs)))

    @JvmStatic
    public fun postJsonArray(url: String, vararg formatArgs: Any) =
        RxHttpJsonArrayParam(Param.postJsonArray(format(url, formatArgs)))

    @JvmStatic
    public fun putJsonArray(url: String, vararg formatArgs: Any) =
        RxHttpJsonArrayParam(Param.putJsonArray(format(url, formatArgs)))

    @JvmStatic
    public fun patchJsonArray(url: String, vararg formatArgs: Any) =
        RxHttpJsonArrayParam(Param.patchJsonArray(format(url, formatArgs)))

    @JvmStatic
    public fun deleteJsonArray(url: String, vararg formatArgs: Any) =
        RxHttpJsonArrayParam(Param.deleteJsonArray(format(url, formatArgs)))

    @JvmStatic
    public fun postEncryptJson(url: String, vararg formatArgs: Any) =
        RxHttpPostEncryptJsonParam(PostEncryptJsonParam(format(url, formatArgs)))

    @JvmStatic
    public fun postEncryptForm(url: String, vararg formatArgs: Any) =
        RxHttpPostEncryptFormParam(PostEncryptFormParam(format(url, formatArgs)))

    @JvmStatic
    public fun postEncryptForm(
      url: String,
      method: Method,
      vararg formatArgs: Any
    ) = RxHttpPostEncryptFormParam(PostEncryptFormParam(format(url, formatArgs), method))

    @JvmStatic
    public fun getEncrypt(url: String, vararg formatArgs: Any) =
        RxHttpGetEncryptParam(GetEncryptParam(format(url, formatArgs)))

    @JvmStatic
    public fun postEncryptJson1(url: String, vararg formatArgs: Any) =
        RxHttpPostEncryptJsonParam1(PostEncryptJsonParam1(format(url, formatArgs)))

    /**
     * Returns a formatted string using the specified format string and arguments.
     */
    private fun format(url: String, vararg formatArgs: Any) = if(formatArgs.isNullOrEmpty()) url
        else String.format(url, formatArgs)
  }
}
