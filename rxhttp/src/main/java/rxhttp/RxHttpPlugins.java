package rxhttp;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import okhttp3.Call;
import okhttp3.Dispatcher;
import okhttp3.OkHttpClient;
import rxhttp.wrapper.cache.CacheManager;
import rxhttp.wrapper.cache.CacheMode;
import rxhttp.wrapper.cache.CacheStrategy;
import rxhttp.wrapper.cache.InternalCache;
import rxhttp.wrapper.callback.Consumer;
import rxhttp.wrapper.callback.Converter;
import rxhttp.wrapper.callback.Function;
import rxhttp.wrapper.converter.GsonConverter;
import rxhttp.wrapper.param.Param;
import rxhttp.wrapper.utils.LogUtil;

/**
 * User: ljx
 * Date: 2019-07-14
 * Time: 11:24
 */
public class RxHttpPlugins {

    private static final RxHttpPlugins plugins = new RxHttpPlugins();

    private OkHttpClient okClient;

    private Consumer<? super Param<?>> onParamAssembly;
    private Function<String, String> decoder;
    private Converter converter = GsonConverter.create();

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
    public RxHttpPlugins setOnParamAssembly(Consumer<? super Param<?>> onParamAssembly) {
        this.onParamAssembly = onParamAssembly;
        return this;
    }

    /**
     * 设置统一数据解码/解密器，每次请求成功后会回调该接口并传入Http请求的结果
     * 通过该接口，可以统一对数据解密，并将解密后的数据返回即可
     * 若部分接口不需要回调该接口，发请求前，调用 RxHttp#setDecoderEnabled(boolean) 方法设置false即可
     */
    public RxHttpPlugins setResultDecoder(Function<String, String> decoder) {
        this.decoder = decoder;
        return this;
    }

    public RxHttpPlugins setConverter(Converter converter) {
        if (converter == null)
            throw new IllegalArgumentException("converter can not be null");
        this.converter = converter;
        return this;
    }

    public static Converter getConverter() {
        return plugins.converter;
    }

    public static void onParamAssembly(@NotNull Param<?> source) {
        if (!source.isAssemblyEnabled()) return;
        Consumer<? super Param<?>> consumer = plugins.onParamAssembly;
        if (consumer != null) {
            consumer.accept(source);
        }
    }

    // Decoder source
    public static String onResultDecoder(String source) throws IOException {
        Function<String, String> f = plugins.decoder;
        return f != null ? f.apply(source) : source;
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

    //Default OkHttpClient object in RxHttp
    private static OkHttpClient getDefaultOkHttpClient() {
        return new OkHttpClient.Builder().build();
    }
}
