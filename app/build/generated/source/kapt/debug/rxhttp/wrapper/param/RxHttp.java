package rxhttp.wrapper.param;

import com.example.httpsender.RxHttpManager;
import com.example.httpsender.entity.PageList;
import com.example.httpsender.entity.Url;
import com.example.httpsender.param.GetEncryptParam;
import com.example.httpsender.param.PostEncryptFormParam;
import com.example.httpsender.param.PostEncryptJsonParam;
import com.example.httpsender.param.PostEncryptJsonParam1;
import com.example.httpsender.parser.ResponseParser;
import io.reactivex.Observable;
import io.reactivex.Scheduler;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import java.io.IOException;
import java.lang.Class;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.lang.reflect.Type;
import java.util.List;
import okhttp3.CacheControl;
import okhttp3.Call;
import okhttp3.Headers;
import okhttp3.Headers.Builder;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import rxhttp.HttpSender;
import rxhttp.RxHttpPlugins;
import rxhttp.wrapper.cahce.CacheMode;
import rxhttp.wrapper.callback.Function;
import rxhttp.wrapper.callback.IConverter;
import rxhttp.wrapper.entity.ParameterizedTypeImpl;
import rxhttp.wrapper.entity.Progress;
import rxhttp.wrapper.entity.ProgressT;
import rxhttp.wrapper.parse.Parser;

/**
 * Github
 * https://github.com/liujingxing/RxHttp
 * https://github.com/liujingxing/RxLife
 */
@SuppressWarnings("unchecked")
public class RxHttp<P extends Param, R extends RxHttp> extends BaseRxHttp {
  protected P param;

  /**
   * The request is executed on the IO thread by default
   */
  protected Scheduler scheduler = Schedulers.io();

  protected IConverter converter = RxHttpPlugins.getConverter();

  private long breakDownloadOffSize = 0L;

  protected RxHttp(P param) {
    this.param = param;
  }

  public static void setDebug(boolean debug) {
    HttpSender.setDebug(debug);
  }

  public static void init(OkHttpClient okHttpClient) {
    HttpSender.init(okHttpClient);
  }

  public static void init(OkHttpClient okHttpClient, boolean debug) {
    HttpSender.init(okHttpClient,debug);
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
  public static void setOnParamAssembly(Function<Param, Param> onParamAssembly) {
    RxHttpPlugins.setOnParamAssembly(onParamAssembly);
  }

  public static OkHttpClient getOkHttpClient() {
    return HttpSender.getOkHttpClient();
  }

  public P getParam() {
    return param;
  }

  public R setParam(P param) {
    this.param = param;
    return (R)this;
  }

  public static RxHttpNoBodyParam get(String url, Object... formatArgs) {
    return with(Param.get(format(url, formatArgs)));
  }

  public static RxHttpNoBodyParam head(String url, Object... formatArgs) {
    return with(Param.head(format(url, formatArgs)));
  }

  public static RxHttpFormParam postForm(String url, Object... formatArgs) {
    return with(Param.postForm(format(url, formatArgs)));
  }

  public static RxHttpFormParam putForm(String url, Object... formatArgs) {
    return with(Param.putForm(format(url, formatArgs)));
  }

  public static RxHttpFormParam patchForm(String url, Object... formatArgs) {
    return with(Param.patchForm(format(url, formatArgs)));
  }

  public static RxHttpFormParam deleteForm(String url, Object... formatArgs) {
    return with(Param.deleteForm(format(url, formatArgs)));
  }

  public static RxHttpJsonParam postJson(String url, Object... formatArgs) {
    return with(Param.postJson(format(url, formatArgs)));
  }

  public static RxHttpJsonParam putJson(String url, Object... formatArgs) {
    return with(Param.putJson(format(url, formatArgs)));
  }

  public static RxHttpJsonParam patchJson(String url, Object... formatArgs) {
    return with(Param.patchJson(format(url, formatArgs)));
  }

  public static RxHttpJsonParam deleteJson(String url, Object... formatArgs) {
    return with(Param.deleteJson(format(url, formatArgs)));
  }

  public static RxHttpJsonArrayParam postJsonArray(String url, Object... formatArgs) {
    return with(Param.postJsonArray(format(url, formatArgs)));
  }

  public static RxHttpJsonArrayParam putJsonArray(String url, Object... formatArgs) {
    return with(Param.putJsonArray(format(url, formatArgs)));
  }

  public static RxHttpJsonArrayParam patchJsonArray(String url, Object... formatArgs) {
    return with(Param.patchJsonArray(format(url, formatArgs)));
  }

  public static RxHttpJsonArrayParam deleteJsonArray(String url, Object... formatArgs) {
    return with(Param.deleteJsonArray(format(url, formatArgs)));
  }

  public static RxHttpPostEncryptJsonParam postEncryptJson(String url, Object... formatArgs) {
    return new RxHttpPostEncryptJsonParam(new PostEncryptJsonParam(format(url, formatArgs)));
  }

  public static RxHttpPostEncryptFormParam postEncryptForm(String url, Object... formatArgs) {
    return new RxHttpPostEncryptFormParam(new PostEncryptFormParam(format(url, formatArgs)));
  }

  public static RxHttpGetEncryptParam getEncrypt(String url, Object... formatArgs) {
    return new RxHttpGetEncryptParam(new GetEncryptParam(format(url, formatArgs)));
  }

  public static RxHttpPostEncryptJsonParam1 postEncryptJson1(String url, Object... formatArgs) {
    return new RxHttpPostEncryptJsonParam1(new PostEncryptJsonParam1(format(url, formatArgs)));
  }

  public static RxHttpNoBodyParam with(NoBodyParam noBodyParam) {
    return new RxHttpNoBodyParam(noBodyParam);
  }

  public static RxHttpFormParam with(FormParam formParam) {
    return new RxHttpFormParam(formParam);
  }

  public static RxHttpJsonParam with(JsonParam jsonParam) {
    return new RxHttpJsonParam(jsonParam);
  }

  public static RxHttpJsonArrayParam with(JsonArrayParam jsonArrayParam) {
    return new RxHttpJsonArrayParam(jsonArrayParam);
  }

  public R setUrl(String url) {
    param.setUrl(url);
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
    param.setRangeHeader(startIndex,endIndex);
    if(connectLastProgress) breakDownloadOffSize = startIndex;
    return (R)this;
  }

  @Override
  public long getBreakDownloadOffSize() {
    return breakDownloadOffSize;
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
    doOnStart();
    return HttpSender.execute(param);
  }

  public <T> T execute(Parser<T> parser) throws IOException {
    return parser.onParse(execute());
  }

  public Call newCall() {
    return newCall(getOkHttpClient());
  }

  public Call newCall(OkHttpClient okHttp) {
    return HttpSender.newCall(okHttp, buildRequest());
  }

  @Override
  public final Request buildRequest() {
    doOnStart();
    return param.buildRequest();
  }

  /**
   * 请求开始前内部调用，用于添加默认域名等操作
   */
  void doOnStart() {
    setConverter(param);
    addDefaultDomainIfAbsent(param);
  }

  public R subscribeOn(Scheduler scheduler) {
    this.scheduler=scheduler;
    return (R)this;
  }

  /**
   * 设置在当前线程发请求
   */
  public R subscribeOnCurrent() {
    this.scheduler=null;
    return (R)this;
  }

  public R subscribeOnIo() {
    this.scheduler=Schedulers.io();
    return (R)this;
  }

  public R subscribeOnComputation() {
    this.scheduler=Schedulers.computation();
    return (R)this;
  }

  public R subscribeOnNewThread() {
    this.scheduler=Schedulers.newThread();
    return (R)this;
  }

  public R subscribeOnSingle() {
    this.scheduler=Schedulers.single();
    return (R)this;
  }

  public R subscribeOnTrampoline() {
    this.scheduler=Schedulers.trampoline();
    return (R)this;
  }

  @Override
  public <T> Observable<T> asParser(Parser<T> parser) {
        doOnStart();
        Observable<T> observable = new ObservableHttp<T>(param, parser);
        if (scheduler != null) {
            observable = observable.subscribeOn(scheduler);
        }
        return observable;
  }

  @Override
  public Observable<String> asDownload(String destPath, Consumer<Progress> progressConsumer,
      Scheduler observeOnScheduler) {
        doOnStart();
        Observable<Progress> observable = new ObservableDownload(param, destPath, breakDownloadOffSize);
        if (scheduler != null)
            observable = observable.subscribeOn(scheduler);
        if (observeOnScheduler != null) {
            observable = observable.observeOn(observeOnScheduler);
        }
        return observable.doOnNext(progressConsumer)
            .filter(progress -> progress instanceof ProgressT)
            .map(progress -> ((ProgressT<String>) progress).getResult());
  }

  public <T> Observable<T> asResponse(Class<T> tType) {
    return asParser(new ResponseParser<T>(tType));
  }

  public <T> Observable<List<T>> asResponseList(Class<T> tType) {
    Type tTypeList = ParameterizedTypeImpl.get(List.class, tType);
    return asParser(new ResponseParser<List<T>>(tTypeList));
  }

  public <T> Observable<PageList<T>> asResponsePageList(Class<T> tType) {
    Type tTypePageList = ParameterizedTypeImpl.get(PageList.class, tType);
    return asParser(new ResponseParser<PageList<T>>(tTypePageList));
  }

  public R setFastJsonConverter() {
    if (RxHttpManager.fastJsonConverter == null)
        throw new IllegalArgumentException("converter can not be null");;
    this.converter = RxHttpManager.fastJsonConverter;
    return (R)this;
  }

  public R setXmlConverter() {
    if (RxHttpManager.xmlConverter == null)
        throw new IllegalArgumentException("converter can not be null");;
    this.converter = RxHttpManager.xmlConverter;
    return (R)this;
  }

  /**
   * 给Param设置转换器，此方法会在请求发起前，被RxHttp内部调用
   */
  private R setConverter(P param) {
    param.tag(IConverter.class,converter);
    return (R)this;
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

  private static String format(String url, Object... formatArgs) {
    return formatArgs == null || formatArgs.length == 0 ? url : String.format(url, formatArgs);
  }
}
