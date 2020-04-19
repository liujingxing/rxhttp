package rxhttp;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

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

/**
 * RxHttp 插件类
 * User: ljx
 * Date: 2019-07-14
 * Time: 11:24
 */
public class RxHttpPlugins {

    private static Function<? super Param, ? extends Param> mOnParamAssembly;
    private static Function<? super String, String> decoder;
    private static IConverter converter = GsonConverter.create();

    private static List<String> excludeCacheKeys = Collections.emptyList();

    private static InternalCache cache;
    private static CacheStrategy cacheStrategy = new CacheStrategy(CacheMode.ONLY_NETWORK);

    //设置公共参数装饰
    public static void setOnParamAssembly(@Nullable Function<? super Param, ? extends Param> onParamAssembly) {
        mOnParamAssembly = onParamAssembly;
    }

    //设置解码/解密器,可用于对Http返回的String 字符串解码/解密
    public static void setResultDecoder(@Nullable Function<? super String, String> decoder) {
        RxHttpPlugins.decoder = decoder;
    }

    public static void setConverter(@NonNull IConverter converter) {
        if (converter == null)
            throw new IllegalArgumentException("converter can not be null");
        RxHttpPlugins.converter = converter;
    }

    public static IConverter getConverter() {
        return converter;
    }

    /**
     * <P>对Param参数添加一层装饰,可以在该层做一些与业务相关工作，
     * <P>例如：添加公共参数/请求头信息
     *
     * @param source Param
     * @return 装饰后的参数
     */
    public static Param onParamAssembly(Param source) {
        if (source == null || !source.isAssemblyEnabled()) return source;
        Function<? super Param, ? extends Param> f = mOnParamAssembly;
        if (f != null) {
            Param p = apply(f, source);
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
        Function<? super String, String> f = decoder;
        if (f != null) {
            return apply(f, source);
        }
        return source;
    }

    @NonNull
    private static <T, R> R apply(@NonNull Function<T, R> f, @NonNull T t) {
        try {
            return f.apply(t);
        } catch (Throwable ex) {
            throw ExceptionHelper.wrapOrThrow(ex);
        }
    }

    public static CacheStrategy getCacheStrategy() {
        if (cacheStrategy == null) {
            cacheStrategy = new CacheStrategy(CacheMode.ONLY_NETWORK);
        }
        return new CacheStrategy(cacheStrategy);
    }

    public static InternalCache getCache() {
        return cache;
    }

    public static void setCache(File directory, long maxSize) {
        setCache(directory, maxSize, CacheMode.ONLY_NETWORK, -1);
    }

    public static void setCache(File directory, long maxSize, long cacheValidTime) {
        setCache(directory, maxSize, CacheMode.ONLY_NETWORK, cacheValidTime);
    }

    public static void setCache(File directory, long maxSize, CacheMode cacheMode) {
        setCache(directory, maxSize, cacheMode, -1);
    }

    public static void setCache(File directory, long maxSize, CacheMode cacheMode, long cacheValidTime) {
        CacheManager rxHttpCache = new CacheManager(directory, maxSize);
        RxHttpPlugins.cache = rxHttpCache.internalCache;
        RxHttpPlugins.cacheStrategy = new CacheStrategy(cacheMode, cacheValidTime);
    }

    public static void setExcludeCacheKeys(String... keys) {
        excludeCacheKeys = Arrays.asList(keys);
    }

    public static List<String> getExcludeCacheKeys() {
        return excludeCacheKeys;
    }

    /**
     * 取消所有请求
     */
    public static void cancelAll() {
        HttpSender.cancelAll();
    }

    /**
     * @param tag 根据Tag取消请求
     */
    public static void cancelAll(Object tag) {
        HttpSender.cancelTag(tag);
    }
}
