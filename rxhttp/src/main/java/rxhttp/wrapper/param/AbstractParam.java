package rxhttp.wrapper.param;


import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

import okhttp3.CacheControl;
import okhttp3.Headers;
import okhttp3.Headers.Builder;
import okhttp3.HttpUrl;
import okhttp3.Request;
import okhttp3.RequestBody;
import rxhttp.RxHttpPlugins;
import rxhttp.wrapper.annotations.NonNull;
import rxhttp.wrapper.annotations.Nullable;
import rxhttp.wrapper.cahce.CacheMode;
import rxhttp.wrapper.cahce.CacheStrategy;
import rxhttp.wrapper.callback.IConverter;
import rxhttp.wrapper.entity.KeyValuePair;
import rxhttp.wrapper.utils.BuildUtil;
import rxhttp.wrapper.utils.LogUtil;

/**
 * 此类是唯一直接实现Param接口的类
 * User: ljx
 * Date: 2019/1/19
 * Time: 14:35
 */
@SuppressWarnings("unchecked")
public abstract class AbstractParam<P extends Param<P>> implements Param<P> {

    private String mUrl;    //链接地址
    private Builder mHBuilder; //请求头构造器
    private final Method mMethod;  //请求方法
    private final CacheStrategy mCacheStrategy;
    private final Request.Builder requestBuilder = new Request.Builder(); //请求构造器
    private List<KeyValuePair> queryPairs; //查询参数，拼接在Url后面

    private boolean mIsAssemblyEnabled = true;//是否添加公共参数


    /**
     * @param url    请求路径
     * @param method Method#GET  Method#HEAD  Method#POST  Method#PUT  Method#DELETE  Method#PATCH
     */
    public AbstractParam(@NonNull String url, Method method) {
        this.mUrl = url;
        this.mMethod = method;
        mCacheStrategy = RxHttpPlugins.getCacheStrategy();
    }

    public P setUrl(@NonNull String url) {
        mUrl = url;
        return (P) this;
    }

    @Override
    public P addQuery(String key, Object value) {
        if (value == null) value = "";
        return addQuery(new KeyValuePair(key, value));
    }

    @Override
    public P addEncodedQuery(String key, Object value) {
        if (value == null) value = "";
        return addQuery(new KeyValuePair(key, value, true));
    }

    @Override
    public P removeAllQuery() {
        final List<KeyValuePair> pairs = queryPairs;
        if (pairs != null) pairs.clear();
        return (P) this;
    }

    @Override
    public P removeAllQuery(String key) {
        final List<KeyValuePair> pairs = queryPairs;
        if (pairs != null) {
            Iterator<KeyValuePair> iterator = pairs.iterator();
            while (iterator.hasNext()) {
                KeyValuePair next = iterator.next();
                if (next.equals(key))
                    iterator.remove();
            }
        }
        return (P) this;
    }

    private P addQuery(KeyValuePair keyValuePair) {
        if (queryPairs == null) queryPairs = new ArrayList<>();
        queryPairs.add(keyValuePair);
        return (P) this;
    }

    public List<KeyValuePair> getQueryPairs() {
        return queryPairs;
    }

    @Override
    public final String getUrl() {
        return getHttpUrl().toString();
    }

    @Override
    public final String getSimpleUrl() {
        return mUrl;
    }

    @Override
    public HttpUrl getHttpUrl() {
        return BuildUtil.getHttpUrl(mUrl, queryPairs);
    }

    @Override
    public Method getMethod() {
        return mMethod;
    }

    @Nullable
    @Override
    public final Headers getHeaders() {
        return mHBuilder == null ? null : mHBuilder.build();
    }

    @Override
    public final Builder getHeadersBuilder() {
        if (mHBuilder == null)
            mHBuilder = new Builder();
        return mHBuilder;
    }

    @Override
    public P setHeadersBuilder(Builder builder) {
        mHBuilder = builder;
        return (P) this;
    }

    @Override
    public P cacheControl(CacheControl cacheControl) {
        requestBuilder.cacheControl(cacheControl);
        return (P) this;
    }

    @Override
    public <T> P tag(Class<? super T> type, T tag) {
        requestBuilder.tag(type, tag);
        return (P) this;
    }

    @Override
    public final P setAssemblyEnabled(boolean enabled) {
        mIsAssemblyEnabled = enabled;
        return (P) this;
    }

    @Override
    public final boolean isAssemblyEnabled() {
        return mIsAssemblyEnabled;
    }

    public Request.Builder getRequestBuilder() {
        return requestBuilder;
    }

    @Override
    public final CacheStrategy getCacheStrategy() {
        String cacheKey = getCacheKey();
        mCacheStrategy.setCacheKey(cacheKey);
        return mCacheStrategy;
    }

    @Override
    public String getCacheKey() {
        return mCacheStrategy.getCacheKey();
    }

    @Override
    public final P setCacheKey(String cacheKey) {
        mCacheStrategy.setCacheKey(cacheKey);
        return (P) this;
    }

    @Override
    public final long getCacheValidTime() {
        return mCacheStrategy.getCacheValidTime();
    }

    @Override
    public final P setCacheValidTime(long cacheTime) {
        mCacheStrategy.setCacheValidTime(cacheTime);
        return (P) this;
    }

    @Override
    public final CacheMode getCacheMode() {
        return mCacheStrategy.getCacheMode();
    }

    @Override
    public final P setCacheMode(CacheMode cacheMode) {
        mCacheStrategy.setCacheMode(cacheMode);
        return (P) this;
    }

    @Override
    public final Request buildRequest() {
        Param<?> param = RxHttpPlugins.onParamAssembly(this);
        Request request = BuildUtil.buildRequest(param, requestBuilder);
        LogUtil.log(request);
        return request;
    }

    protected IConverter getConverter() {
        Request request = getRequestBuilder().build();
        return request.tag(IConverter.class);
    }

    protected final RequestBody convert(Object object) {
        IConverter converter = Objects.requireNonNull(getConverter(), "converter can not be null");
        try {
            return converter.convert(object);
        } catch (IOException e) {
            throw new IllegalArgumentException("Unable to convert " + object + " to RequestBody", e);
        }
    }
}
