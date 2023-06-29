package rxhttp.wrapper.param;


import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import okhttp3.CacheControl;
import okhttp3.Headers;
import okhttp3.Headers.Builder;
import okhttp3.HttpUrl;
import okhttp3.Request;
import okhttp3.RequestBody;
import rxhttp.RxHttpPlugins;
import rxhttp.wrapper.cache.CacheMode;
import rxhttp.wrapper.cache.CacheStrategy;
import rxhttp.wrapper.callback.IConverter;
import rxhttp.wrapper.entity.KeyValuePair;
import rxhttp.wrapper.utils.BuildUtil;
import rxhttp.wrapper.utils.CacheUtil;

/**
 * User: ljx
 * Date: 2019/1/19
 * Time: 14:35
 */
public abstract class AbstractParam<P extends Param<P>> extends Param<P> {

    private String url;    //链接地址
    private Builder HBuilder; //请求头构造器
    private final Method method;  //请求方法
    private final CacheStrategy cacheStrategy;  //缓存策略
    private List<KeyValuePair> queryParam; //查询参数，拼接在Url后面
    private List<KeyValuePair> paths;      //Replace '{XXX}' in the url
    private final Request.Builder requestBuilder = new Request.Builder(); //请求构造器

    private boolean isAssemblyEnabled = true;//是否添加公共参数

    /**
     * @param url    请求路径
     * @param method Method#GET  Method#HEAD  Method#POST  Method#PUT  Method#DELETE  Method#PATCH
     */
    public AbstractParam(@NotNull String url, Method method) {
        this.url = url;
        this.method = method;
        cacheStrategy = RxHttpPlugins.getCacheStrategy();
    }

    public P setUrl(@NotNull String url) {
        this.url = url;
        return self();
    }

    @Override
    public P addPath(String name, Object value) {
        return addPath(new KeyValuePair(name, value));
    }

    @Override
    public P addEncodedPath(String name, Object value) {
        return addPath(new KeyValuePair(name, value, true));
    }

    private P addPath(KeyValuePair keyValuePair) {
        if (paths == null) paths = new ArrayList<>();
        paths.add(keyValuePair);
        return self();
    }

    @Override
    public P addQuery(String key, @Nullable Object value) {
        return addQuery(new KeyValuePair(key, value));
    }

    @Override
    public P addEncodedQuery(String key, @Nullable Object value) {
        return addQuery(new KeyValuePair(key, value, true));
    }

    private P addQuery(KeyValuePair keyValuePair) {
        if (queryParam == null) queryParam = new ArrayList<>();
        queryParam.add(keyValuePair);
        return self();
    }

    @Nullable
    public List<KeyValuePair> getQueryParam() {
        return queryParam;
    }

    public List<KeyValuePair> getPaths() {
        return paths;
    }

    @Override
    public final String getUrl() {
        return getHttpUrl().toString();
    }

    @Override
    public final String getSimpleUrl() {
        return url;
    }

    @Override
    public HttpUrl getHttpUrl() {
        return BuildUtil.getHttpUrl(url, queryParam, paths);
    }

    @Override
    public Method getMethod() {
        return method;
    }

    @Nullable
    @Override
    public final Headers getHeaders() {
        return HBuilder == null ? null : HBuilder.build();
    }

    @Override
    public final Builder getHeadersBuilder() {
        if (HBuilder == null)
            HBuilder = new Builder();
        return HBuilder;
    }

    @Override
    public P setHeadersBuilder(Builder builder) {
        HBuilder = builder;
        return self();
    }

    @Override
    public P cacheControl(CacheControl cacheControl) {
        requestBuilder.cacheControl(cacheControl);
        return self();
    }

    @Override
    public <T> P tag(Class<? super T> type, T tag) {
        requestBuilder.tag(type, tag);
        return self();
    }

    @Override
    public final P setAssemblyEnabled(boolean enabled) {
        isAssemblyEnabled = enabled;
        return self();
    }

    @Override
    public final boolean isAssemblyEnabled() {
        return isAssemblyEnabled;
    }

    public Request.Builder getRequestBuilder() {
        return requestBuilder;
    }

    @Override
    public final CacheStrategy getCacheStrategy() {
        if (getCacheKey() == null) {
            setCacheKey(buildCacheKey());
        }
        return cacheStrategy;
    }

    @Override
    public final String getCacheKey() {
        return cacheStrategy.getCacheKey();
    }

    @Override
    public final P setCacheKey(String cacheKey) {
        cacheStrategy.setCacheKey(cacheKey);
        return self();
    }

    public String buildCacheKey() {
        List<KeyValuePair> queryPairs = CacheUtil.excludeCacheKey(getQueryParam());
        return BuildUtil.getHttpUrl(getSimpleUrl(), queryPairs, paths).toString();
    }

    @Override
    public final long getCacheValidTime() {
        return cacheStrategy.getCacheValidTime();
    }

    @Override
    public final P setCacheValidTime(long cacheTime) {
        cacheStrategy.setCacheValidTime(cacheTime);
        return self();
    }

    @Override
    public final CacheMode getCacheMode() {
        return cacheStrategy.getCacheMode();
    }

    @Override
    public final P setCacheMode(CacheMode cacheMode) {
        cacheStrategy.setCacheMode(cacheMode);
        return self();
    }

    @Override
    public final Request buildRequest() {
        RxHttpPlugins.onParamAssembly(this);
        return BuildUtil.buildRequest(this, requestBuilder);
    }

    protected IConverter getConverter() {
        Request request = getRequestBuilder().build();
        IConverter converter = request.tag(IConverter.class);
        return Objects.requireNonNull(converter, "converter can not be null");
    }

    protected final RequestBody convert(Object object) {
        try {
            return getConverter().convert(object);
        } catch (IOException e) {
            throw new IllegalArgumentException("Unable to convert " + object + " to RequestBody", e);
        }
    }

    @Override
    public P removeAllQuery(String key) {
        final List<KeyValuePair> queryParam = this.queryParam;
        if (queryParam == null) return self();
        Iterator<KeyValuePair> iterator = queryParam.iterator();
        while (iterator.hasNext()) {
            KeyValuePair next = iterator.next();
            if (next.getKey().equals(key))
                iterator.remove();
        }
        return self();
    }

    @SuppressWarnings("unchecked")
    protected P self() {
        return (P) this;
    }
}
