package rxhttp;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Dispatcher;
import okhttp3.OkHttpClient;
import rxhttp.wrapper.annotations.NonNull;
import rxhttp.wrapper.annotations.Nullable;
import rxhttp.wrapper.cahce.CacheManager;
import rxhttp.wrapper.cahce.CacheMode;
import rxhttp.wrapper.cahce.CacheStrategy;
import rxhttp.wrapper.cahce.InternalCache;
import rxhttp.wrapper.callback.Function;
import rxhttp.wrapper.callback.IConverter;
import rxhttp.wrapper.converter.GsonConverter;
import rxhttp.wrapper.exception.ExceptionHelper;
import rxhttp.wrapper.param.Param;
import rxhttp.wrapper.ssl.HttpsUtils;
import rxhttp.wrapper.ssl.HttpsUtils.SSLParams;
import rxhttp.wrapper.utils.LogUtil;

/**
 * RxHttp 插件类
 * User: ljx
 * Date: 2019-07-14
 * Time: 11:24
 */
public class RxHttpPlugins {

    private static final RxHttpPlugins plugins = new RxHttpPlugins();

    private OkHttpClient okClient;

    private Function<? super Param<?>, ? extends Param<?>> onParamAssembly;
    private Function<String, String> decoder;
    private IConverter converter = GsonConverter.create();

    private List<String> excludeCacheKeys = Collections.emptyList();

    private InternalCache cache;
    private CacheStrategy cacheStrategy = new CacheStrategy(CacheMode.ONLY_NETWORK);

    private RxHttpPlugins() {
    }

    public static RxHttpPlugins init(OkHttpClient okHttpClient) {
        plugins.okClient = okHttpClient;
        return plugins;
    }

    public static boolean isInit() {
        return plugins.okClient != null;
    }

    public static OkHttpClient getOkHttpClient() {
        if (plugins.okClient == null)
            init(getDefaultOkHttpClient());
        return plugins.okClient;
    }

    public static OkHttpClient.Builder newOkClientBuilder() {
        return getOkHttpClient().newBuilder();
    }

    public RxHttpPlugins setDebug(boolean debug) {
        return setDebug(debug, false, -1);
    }

    public RxHttpPlugins setDebug(boolean debug, boolean segmentPrint) {
        return setDebug(debug, segmentPrint, -1);
    }

    public RxHttpPlugins setDebug(boolean debug, boolean segmentPrint, int indentSpaces) {
        LogUtil.setDebug(debug, segmentPrint, indentSpaces);
        return this;
    }

    /**
     * 设置统一公共参数回调接口,通过该接口,可添加公共参数/请求头，每次请求前会回调该接口
     * 若部分接口不需要添加公共参数,发请求前，调用 RxHttp#setAssemblyEnabled(boolean) 方法设置false即可
     */
    public RxHttpPlugins setOnParamAssembly(@Nullable Function<? super Param<?>, ? extends Param<?>> onParamAssembly) {
        this.onParamAssembly = onParamAssembly;
        return this;
    }

    /**
     * 设置统一数据解码/解密器，每次请求成功后会回调该接口并传入Http请求的结果
     * 通过该接口，可以统一对数据解密，并将解密后的数据返回即可
     * 若部分接口不需要回调该接口，发请求前，调用 RxHttp#setDecoderEnabled(boolean) 方法设置false即可
     */
    public RxHttpPlugins setResultDecoder(@Nullable Function<String, String> decoder) {
        this.decoder = decoder;
        return this;
    }

    public RxHttpPlugins setConverter(@NonNull IConverter converter) {
        if (converter == null)
            throw new IllegalArgumentException("converter can not be null");
        this.converter = converter;
        return this;
    }

    public static IConverter getConverter() {
        return plugins.converter;
    }

    /**
     * <P>对Param参数添加一层装饰,可以在该层做一些与业务相关工作，
     * <P>例如：添加公共参数/请求头信息
     *
     * @param source Param
     * @return 装饰后的参数
     */
    public static Param<?> onParamAssembly(Param<?> source) {
        if (source == null || !source.isAssemblyEnabled()) return source;
        Function<? super Param<?>, ? extends Param<?>> f = plugins.onParamAssembly;
        if (f != null) {
            Param<?> p = apply(f, source);
            if (p == null) {
                throw new NullPointerException("onParamAssembly return must not be null");
            }
            return p;
        }
        return source;
    }

    /**
     * 对字符串进行解码/解密
     *
     * @param source String字符串
     * @return 解码/解密后字符串
     */
    public static String onResultDecoder(String source) {
        Function<String, String> f = plugins.decoder;
        if (f != null) {
            return apply(f, source);
        }
        return source;
    }

    public RxHttpPlugins setCache(File directory, long maxSize) {
        return setCache(directory, maxSize, CacheMode.ONLY_NETWORK, Long.MAX_VALUE);
    }

    public RxHttpPlugins setCache(File directory, long maxSize, long cacheValidTime) {
        return setCache(directory, maxSize, CacheMode.ONLY_NETWORK, cacheValidTime);
    }

    public RxHttpPlugins setCache(File directory, long maxSize, CacheMode cacheMode) {
        return setCache(directory, maxSize, cacheMode, Long.MAX_VALUE);
    }

    public RxHttpPlugins setCache(File directory, long maxSize, CacheMode cacheMode, long cacheValidTime) {
        if (maxSize <= 0) {
            throw new IllegalArgumentException("maxSize > 0 required but it was " + maxSize);
        }
        if (cacheValidTime <= 0) {
            throw new IllegalArgumentException("cacheValidTime > 0 required but it was " + cacheValidTime);
        }
        CacheManager rxHttpCache = new CacheManager(directory, maxSize);
        cache = rxHttpCache.internalCache;
        cacheStrategy = new CacheStrategy(cacheMode, cacheValidTime);
        return this;
    }

    public static CacheStrategy getCacheStrategy() {
        return new CacheStrategy(plugins.cacheStrategy);
    }

    public static InternalCache getCache() {
        return plugins.cache;
    }

    /**
     * Call {@link RxHttpPlugins#setCache(File,long)} setCache method to set the cache directory and size before using the cache
     */
    public static InternalCache getCacheOrThrow() {
        final InternalCache cache = plugins.cache;
        if (cache == null) {
            throw new IllegalArgumentException("Call 'setCache(File,long)' method to set the cache directory and size before using the cache");
        }
        return cache;
    }

    public RxHttpPlugins setExcludeCacheKeys(String... keys) {
        excludeCacheKeys = Arrays.asList(keys);
        return this;
    }

    public static List<String> getExcludeCacheKeys() {
        return plugins.excludeCacheKeys;
    }

    //Cancel all requests
    public static void cancelAll() {
        cancelAll(plugins.okClient);
    }

    //Cancel the request according to tag
    public static void cancelAll(Object tag) {
        cancelAll(plugins.okClient, tag);
    }

    public static void cancelAll(@Nullable OkHttpClient okClient) {
        if (okClient == null) return;
        okClient.dispatcher().cancelAll();
    }

    public static void cancelAll(@Nullable OkHttpClient okClient, @Nullable Object tag) {
        if (tag == null || okClient == null) return;
        Dispatcher dispatcher = okClient.dispatcher();

        for (Call call : dispatcher.queuedCalls()) {
            if (tag.equals(call.request().tag())) {
                call.cancel();
            }
        }

        for (Call call : dispatcher.runningCalls()) {
            if (tag.equals(call.request().tag())) {
                call.cancel();
            }
        }
    }

    @NonNull
    private static <T, R> R apply(@NonNull Function<T, R> f, @NonNull T t) {
        try {
            return f.apply(t);
        } catch (Throwable ex) {
            throw ExceptionHelper.wrapOrThrow(ex);
        }
    }

    //Default OkHttpClient object in RxHttp
    private static OkHttpClient getDefaultOkHttpClient() {
        SSLParams sslParams = HttpsUtils.getSslSocketFactory();
        return new OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .writeTimeout(10, TimeUnit.SECONDS)
            .sslSocketFactory(sslParams.sSLSocketFactory, sslParams.trustManager)
            .hostnameVerifier((hostname, session) -> true)
            .build();
    }
}
