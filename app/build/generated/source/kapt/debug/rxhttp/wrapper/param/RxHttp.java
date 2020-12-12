package rxhttp.wrapper.param;

import com.example.httpsender.RxHttpManager;
import com.example.httpsender.entity.PageList;
import com.example.httpsender.entity.Url;
import com.example.httpsender.param.GetEncryptParam;
import com.example.httpsender.param.PostEncryptFormParam;
import com.example.httpsender.param.PostEncryptJsonParam;
import com.example.httpsender.param.PostEncryptJsonParam1;
import com.example.httpsender.parser.ResponseParser;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Scheduler;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.functions.Consumer;
import java.io.IOException;
import java.lang.Class;
import java.lang.Deprecated;
import java.lang.Object;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import okhttp3.CacheControl;
import okhttp3.Call;
import okhttp3.Headers;
import okhttp3.Headers.Builder;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;
import rxhttp.HttpSender;
import rxhttp.RxHttpPlugins;
import rxhttp.wrapper.cahce.CacheMode;
import rxhttp.wrapper.cahce.CacheStrategy;
import rxhttp.wrapper.callback.Function;
import rxhttp.wrapper.callback.IConverter;
import rxhttp.wrapper.entity.DownloadOffSize;
import rxhttp.wrapper.entity.ParameterizedTypeImpl;
import rxhttp.wrapper.entity.Progress;
import rxhttp.wrapper.intercept.CacheInterceptor;
import rxhttp.wrapper.parse.Parser;
import rxhttp.wrapper.parse.SimpleParser;
import rxhttp.wrapper.utils.LogTime;
import rxhttp.wrapper.utils.LogUtil;

/**
 * Github
 * https://github.com/liujingxing/RxHttp
 * https://github.com/liujingxing/RxLife
 * https://github.com/liujingxing/okhttp-RxHttp/wiki/FAQ
 * https://github.com/liujingxing/okhttp-RxHttp/wiki/更新日志
 */
@SuppressWarnings("unchecked")
public class RxHttp<P extends Param, R extends RxHttp> extends BaseRxHttp {
  protected P param;

  private int connectTimeoutMillis;

  private int readTimeoutMillis;

  private int writeTimeoutMillis;

  private OkHttpClient realOkClient;

  private OkHttpClient okClient = HttpSender.getOkHttpClient();

  protected boolean isAsync = true;

  protected IConverter converter = RxHttpPlugins.getConverter();

  public Request request;

  protected RxHttp(P param) {
    this.param = param;
  }

  public static void setDebug(boolean debug) {
    setDebug(debug, false);
  }

  public static void setDebug(boolean debug, boolean segmentPrint) {
    LogUtil.setDebug(debug, segmentPrint);
  }

  public static void init(OkHttpClient okHttpClient) {
    HttpSender.init(okHttpClient);
  }

  public static void init(OkHttpClient okHttpClient, boolean debug) {
    HttpSender.init(okHttpClient,debug);
  }

  public static boolean isInit() {
    return HttpSender.isInit();
  }

  /**
   * 设置统一数据解码/解密器，每次请求成功后会回调该接口并传入Http请求的结果
   * 通过该接口，可以统一对数据解密，并将解密后的数据返回即可
   * 若部分接口不需要回调该接口，发请求前，调用{@link #setDecoderEnabled(boolean)}方法设置false即可
   */
  public static void setResultDecoder(Function<String, String> decoder) {
    RxHttpPlugins.setResultDecoder(decoder);
  }

  /**
   * 设置默认的转换器
   */
  public static void setConverter(IConverter converter) {
    RxHttpPlugins.setConverter(converter);
  }

  /**
   * 设置统一公共参数回调接口,通过该接口,可添加公共参数/请求头，每次请求前会回调该接口
   * 若部分接口不需要添加公共参数,发请求前，调用{@link #setAssemblyEnabled(boolean)}方法设置false即可
   */
  public static void setOnParamAssembly(Function<Param<?>, Param<?>> onParamAssembly) {
    RxHttpPlugins.setOnParamAssembly(onParamAssembly);
  }

  public R connectTimeout(int connectTimeout) {
    connectTimeoutMillis = connectTimeout;
    return (R)this;
  }

  public R readTimeout(int readTimeout) {
    readTimeoutMillis = readTimeout;
    return (R)this;
  }

  public R writeTimeout(int writeTimeout) {
    writeTimeoutMillis = writeTimeout;
    return (R)this;
  }

  public OkHttpClient getOkHttpClient() {
    if (realOkClient != null) return realOkClient;
    final OkHttpClient okHttpClient = okClient;
    OkHttpClient.Builder builder = null;

    if (connectTimeoutMillis != 0) {
      if (builder == null) builder = okHttpClient.newBuilder();
      builder.connectTimeout(connectTimeoutMillis, TimeUnit.MILLISECONDS);
    }

    if (readTimeoutMillis != 0) {
      if (builder == null) builder = okHttpClient.newBuilder();
      builder.readTimeout(readTimeoutMillis, TimeUnit.MILLISECONDS);
    }

    if (writeTimeoutMillis != 0) {
      if (builder == null) builder = okHttpClient.newBuilder();
      builder.writeTimeout(writeTimeoutMillis, TimeUnit.MILLISECONDS);
    }

    if (param.getCacheMode() != CacheMode.ONLY_NETWORK) {                      
      if (builder == null) builder = okHttpClient.newBuilder();              
      builder.addInterceptor(new CacheInterceptor(param.getCacheStrategy()));
    }
                                                                            
    realOkClient = builder != null ? builder.build() : okHttpClient;
    return realOkClient;
  }

  public static void dispose(Disposable disposable) {
    if (!isDisposed(disposable)) disposable.dispose();
  }

  public static boolean isDisposed(Disposable disposable) {
    return disposable == null || disposable.isDisposed();
  }

  public P getParam() {
    return param;
  }

  public R setParam(P param) {
    this.param = param;
    return (R)this;
  }

  /**
   * For example:
   *                                          
   * ```                                                  
   * RxHttp.get("/service/%1$s/...?pageSize=%2$s", 1, 20)   
   *     .asString()                                      
   *     .subscribe()                                     
   * ```                                                  
   */
  public static RxHttpNoBodyParam get(String url, Object... formatArgs) {
    return new RxHttpNoBodyParam(Param.get(format(url, formatArgs)));
  }

  public static RxHttpNoBodyParam head(String url, Object... formatArgs) {
    return new RxHttpNoBodyParam(Param.head(format(url, formatArgs)));
  }

  public static RxHttpFormParam postForm(String url, Object... formatArgs) {
    return new RxHttpFormParam(Param.postForm(format(url, formatArgs)));
  }

  public static RxHttpFormParam putForm(String url, Object... formatArgs) {
    return new RxHttpFormParam(Param.putForm(format(url, formatArgs)));
  }

  public static RxHttpFormParam patchForm(String url, Object... formatArgs) {
    return new RxHttpFormParam(Param.patchForm(format(url, formatArgs)));
  }

  public static RxHttpFormParam deleteForm(String url, Object... formatArgs) {
    return new RxHttpFormParam(Param.deleteForm(format(url, formatArgs)));
  }

  public static RxHttpJsonParam postJson(String url, Object... formatArgs) {
    return new RxHttpJsonParam(Param.postJson(format(url, formatArgs)));
  }

  public static RxHttpJsonParam putJson(String url, Object... formatArgs) {
    return new RxHttpJsonParam(Param.putJson(format(url, formatArgs)));
  }

  public static RxHttpJsonParam patchJson(String url, Object... formatArgs) {
    return new RxHttpJsonParam(Param.patchJson(format(url, formatArgs)));
  }

  public static RxHttpJsonParam deleteJson(String url, Object... formatArgs) {
    return new RxHttpJsonParam(Param.deleteJson(format(url, formatArgs)));
  }

  public static RxHttpJsonArrayParam postJsonArray(String url, Object... formatArgs) {
    return new RxHttpJsonArrayParam(Param.postJsonArray(format(url, formatArgs)));
  }

  public static RxHttpJsonArrayParam putJsonArray(String url, Object... formatArgs) {
    return new RxHttpJsonArrayParam(Param.putJsonArray(format(url, formatArgs)));
  }

  public static RxHttpJsonArrayParam patchJsonArray(String url, Object... formatArgs) {
    return new RxHttpJsonArrayParam(Param.patchJsonArray(format(url, formatArgs)));
  }

  public static RxHttpJsonArrayParam deleteJsonArray(String url, Object... formatArgs) {
    return new RxHttpJsonArrayParam(Param.deleteJsonArray(format(url, formatArgs)));
  }

  public static RxHttpPostEncryptJsonParam postEncryptJson(String url, Object... formatArgs) {
    return new RxHttpPostEncryptJsonParam(new PostEncryptJsonParam(format(url, formatArgs)));
  }

  public static RxHttpPostEncryptFormParam postEncryptForm(String url, Object... formatArgs) {
    return new RxHttpPostEncryptFormParam(new PostEncryptFormParam(format(url, formatArgs)));
  }

  public static RxHttpPostEncryptFormParam postEncryptForm(String url, Method method,
      Object... formatArgs) {
    return new RxHttpPostEncryptFormParam(new PostEncryptFormParam(format(url, formatArgs), method));
  }

  public static RxHttpGetEncryptParam getEncrypt(String url, Object... formatArgs) {
    return new RxHttpGetEncryptParam(new GetEncryptParam(format(url, formatArgs)));
  }

  public static RxHttpPostEncryptJsonParam1 postEncryptJson1(String url, Object... formatArgs) {
    return new RxHttpPostEncryptJsonParam1(new PostEncryptJsonParam1(format(url, formatArgs)));
  }

  public R setUrl(String url) {
    param.setUrl(url);
    return (R)this;
  }

  public R removeAllQuery() {
    param.removeAllQuery();
    return (R)this;
  }

  public R removeAllQuery(String key) {
    param.removeAllQuery(key);
    return (R)this;
  }

  public R addQuery(String key, Object value) {
    param.addQuery(key,value);
    return (R)this;
  }

  public R setQuery(String key, Object value) {
    param.setQuery(key,value);
    return (R)this;
  }

  public R addEncodedQuery(String key, Object value) {
    param.addEncodedQuery(key,value);
    return (R)this;
  }

  public R setEncodedQuery(String key, Object value) {
    param.setEncodedQuery(key,value);
    return (R)this;
  }

  public R addAllQuery(Map<String, ?> map) {
    param.addAllQuery(map);
    return (R)this;
  }

  public R setAllQuery(Map<String, ?> map) {
    param.setAllQuery(map);
    return (R)this;
  }

  public R addAllEncodedQuery(Map<String, ?> map) {
    param.addAllEncodedQuery(map);
    return (R)this;
  }

  public R setAllEncodedQuery(Map<String, ?> map) {
    param.setAllEncodedQuery(map);
    return (R)this;
  }

  public R addHeader(String line) {
    param.addHeader(line);
    return (R)this;
  }

  public R addHeader(String line, boolean isAdd) {
    if(isAdd) {
      param.addHeader(line);
    }
    return (R)this;
  }

  /**
   * Add a header with the specified name and value. Does validation of header names, allowing non-ASCII values.
   */
  public R addNonAsciiHeader(String key, String value) {
    param.addNonAsciiHeader(key,value);
    return (R)this;
  }

  /**
   * Set a header with the specified name and value. Does validation of header names, allowing non-ASCII values.
   */
  public R setNonAsciiHeader(String key, String value) {
    param.setNonAsciiHeader(key,value);
    return (R)this;
  }

  public R addHeader(String key, String value) {
    param.addHeader(key,value);
    return (R)this;
  }

  public R addHeader(String key, String value, boolean isAdd) {
    if(isAdd) {
      param.addHeader(key,value);
    }
    return (R)this;
  }

  public R addAllHeader(Map<String, String> headers) {
    param.addAllHeader(headers);
    return (R)this;
  }

  public R addAllHeader(Headers headers) {
    param.addAllHeader(headers);
    return (R)this;
  }

  public R setHeader(String key, String value) {
    param.setHeader(key,value);
    return (R)this;
  }

  public R setRangeHeader(long startIndex) {
    return setRangeHeader(startIndex, -1, false);
  }

  public R setRangeHeader(long startIndex, long endIndex) {
    return setRangeHeader(startIndex, endIndex, false);
  }

  public R setRangeHeader(long startIndex, boolean connectLastProgress) {
    return setRangeHeader(startIndex, -1, connectLastProgress);
  }

  /**
   * 设置断点下载开始/结束位置
   * @param startIndex 断点下载开始位置
   * @param endIndex 断点下载结束位置，默认为-1，即默认结束位置为文件末尾
   * @param connectLastProgress 是否衔接上次的下载进度，该参数仅在带进度断点下载时生效
   */
  public R setRangeHeader(long startIndex, long endIndex, boolean connectLastProgress) {
    param.setRangeHeader(startIndex, endIndex);                         
    if (connectLastProgress)                                            
      param.tag(DownloadOffSize.class, new DownloadOffSize(startIndex));
    return (R) this;                                                    
  }

  public R removeAllHeader(String key) {
    param.removeAllHeader(key);
    return (R)this;
  }

  public R setHeadersBuilder(Headers.Builder builder) {
    param.setHeadersBuilder(builder);
    return (R)this;
  }

  /**
   * 设置单个接口是否需要添加公共参数,
   * 即是否回调通过{@link #setOnParamAssembly(Function)}方法设置的接口,默认为true
   */
  public R setAssemblyEnabled(boolean enabled) {
    param.setAssemblyEnabled(enabled);
    return (R)this;
  }

  /**
   * 设置单个接口是否需要对Http返回的数据进行解码/解密,
   * 即是否回调通过{@link #setResultDecoder(Function)}方法设置的接口,默认为true
   */
  public R setDecoderEnabled(boolean enabled) {
    param.addHeader(Param.DATA_DECRYPT,String.valueOf(enabled));
    return (R)this;
  }

  public boolean isAssemblyEnabled() {
    return param.isAssemblyEnabled();
  }

  public String getUrl() {
    addDefaultDomainIfAbsent(param);
    return param.getUrl();
  }

  public String getSimpleUrl() {
    return param.getSimpleUrl();
  }

  public String getHeader(String key) {
    return param.getHeader(key);
  }

  public Headers getHeaders() {
    return param.getHeaders();
  }

  public Headers.Builder getHeadersBuilder() {
    return param.getHeadersBuilder();
  }

  public R tag(Object tag) {
    param.tag(tag);
    return (R)this;
  }

  public <T> R tag(Class<? super T> type, T tag) {
    param.tag(type,tag);
    return (R)this;
  }

  public R cacheControl(CacheControl cacheControl) {
    param.cacheControl(cacheControl);
    return (R)this;
  }

  public CacheStrategy getCacheStrategy() {
    return param.getCacheStrategy();
  }

  public R setCacheKey(String cacheKey) {
    param.setCacheKey(cacheKey);
    return (R)this;
  }

  public R setCacheValidTime(long cacheValidTime) {
    param.setCacheValidTime(cacheValidTime);
    return (R)this;
  }

  public R setCacheMode(CacheMode cacheMode) {
    param.setCacheMode(cacheMode);
    return (R)this;
  }

  public Response execute() throws IOException {
    return newCall().execute();
  }

  public <T> T execute(Parser<T> parser) throws IOException {
    return parser.onParse(execute());
  }

  public String executeString() throws IOException {
    return executeClass(String.class);
  }

  public <T> List<T> executeList(Class<T> type) throws IOException {
    Type tTypeList = ParameterizedTypeImpl.get(List.class, type);
    return execute(new SimpleParser<List<T>>(tTypeList));
  }

  public <T> T executeClass(Class<T> type) throws IOException {
    return execute(new SimpleParser<T>(type));
  }

  public final Call newCall() {
    Request request = buildRequest();
    OkHttpClient okClient = getOkHttpClient();
    return okClient.newCall(request);
  }

  public final Request buildRequest() {
    if (request == null) {
        doOnStart();
        request = param.buildRequest();
    }
    if (LogUtil.isDebug()) {
        request = request.newBuilder()
            .tag(LogTime.class, new LogTime())
            .build();
    }
    return request;
  }

  /**
   * 请求开始前内部调用，用于添加默认域名等操作
   */
  private final void doOnStart() {
    setConverter(param);
    addDefaultDomainIfAbsent(param);
  }

  /**
   * @deprecated please user {@link #setSync()} instead
   */
  @Deprecated
  public R subscribeOnCurrent() {
    return setSync();
  }

  /**
   * sync request 
   */
  public R setSync() {
    isAsync = false;
    return (R)this;
  }

  public <T> Observable<T> asParser(Parser<T> parser, Scheduler scheduler,
      Consumer<Progress> progressConsumer) {
    ObservableCall observableCall;                                      
    if (isAsync) {                                                      
      observableCall = new ObservableCallEnqueue(this);                 
    } else {                                                            
      observableCall = new ObservableCallExecute(this);                 
    }                                                                   
    return observableCall.asParser(parser, scheduler, progressConsumer);
  }

  public <T> Observable<T> asResponse(Class<T> type) {
    return asParser(new ResponseParser<T>(type));
  }

  public <T> Observable<List<T>> asResponseList(Class<T> type) {
    Type typeList = ParameterizedTypeImpl.get(List.class, type);
    return asParser(new ResponseParser<List<T>>(typeList));
  }

  public <T> Observable<PageList<T>> asResponsePageList(Class<T> type) {
    Type typePageList = ParameterizedTypeImpl.get(PageList.class, type);
    return asParser(new ResponseParser<PageList<T>>(typePageList));
  }

  public R setXmlConverter() {
    if (RxHttpManager.xmlConverter == null)
        throw new IllegalArgumentException("converter can not be null");;
    this.converter = RxHttpManager.xmlConverter;
    return (R)this;
  }

  public R setFastJsonConverter() {
    if (RxHttpManager.fastJsonConverter == null)
        throw new IllegalArgumentException("converter can not be null");;
    this.converter = RxHttpManager.fastJsonConverter;
    return (R)this;
  }

  /**
   * 给Param设置转换器，此方法会在请求发起前，被RxHttp内部调用
   */
  private R setConverter(P param) {
    param.tag(IConverter.class,converter);
    return (R)this;
  }

  public R setOkClient(@NotNull OkHttpClient okClient) {
    if (okClient == null) 
        throw new IllegalArgumentException("okClient can not be null");
    this.okClient = okClient;
    return (R)this;
  }

  public R setSimpleClient() {
    return setOkClient(RxHttpManager.simpleClient);
  }

  /**
   * 给Param设置默认域名(如何缺席的话)，此方法会在请求发起前，被RxHttp内部调用
   */
  private P addDefaultDomainIfAbsent(P param) {
    String newUrl = addDomainIfAbsent(param.getSimpleUrl(), Url.baseUrl);
    param.setUrl(newUrl);
    return param;
  }

  public R setDomainToUpdateIfAbsent() {
    String newUrl = addDomainIfAbsent(param.getSimpleUrl(), Url.update);
    param.setUrl(newUrl);
    return (R)this;
  }

  private static String addDomainIfAbsent(String url, String domain) {
    if (url.startsWith("http")) return url;
    if (url.startsWith("/")) {
        if (domain.endsWith("/"))
            return domain + url.substring(1);
        else
            return domain + url;
    } else if (domain.endsWith("/")) {
        return domain + url;
    } else {
        return domain + "/" + url;
    }
  }

  /**
   * Returns a formatted string using the specified format string and arguments.
   */
  private static String format(String url, Object... formatArgs) {
    return formatArgs == null || formatArgs.length == 0 ? url : String.format(url, formatArgs);
  }
}
