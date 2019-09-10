package rxhttp.wrapper.param;

import java.util.LinkedHashMap;
import java.util.Map;

import io.reactivex.annotations.NonNull;
import io.reactivex.annotations.Nullable;
import okhttp3.CacheControl;
import okhttp3.Headers;
import okhttp3.Headers.Builder;
import rxhttp.wrapper.utils.BuildUtil;

/**
 * 此类是唯一直接实现Param接口的类
 * User: ljx
 * Date: 2019/1/19
 * Time: 14:35
 */
@SuppressWarnings("unchecked")
public abstract class AbstractParam<T extends Param> extends LinkedHashMap<String, Object> implements Param<T> {

    private   String  mUrl;    //链接地址
    protected String  method;
    private   Builder mHBuilder; //请求头构造器

    private boolean mIsAssemblyEnabled = true;

    private Object       mTag;
    private CacheControl mCacheControl;

    public AbstractParam(@NonNull String url, String method) {
        this.mUrl = url;
        this.method = method;
    }

    /**
     * @return 带参数的url
     */
    @Override
    public final String getUrl() {
        return BuildUtil.mergeUrlAndParams(mUrl, this);
    }

    public T setUrl(@NonNull String url) {
        mUrl = url;
        return (T) this;
    }

    /**
     * @return 不带参数的url
     */
    @Override
    public final String getSimpleUrl() {
        return mUrl;
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
    public T setHeadersBuilder(Builder builder) {
        mHBuilder = builder;
        return (T) this;
    }

    @Override
    public final T addHeader(String key, String value) {
        getHeadersBuilder().add(key, value);
        return (T) this;
    }

    @Override
    public final T addHeader(String line) {
        getHeadersBuilder().add(line);
        return (T) this;
    }

    @Override
    public final T setHeader(String key, String value) {
        getHeadersBuilder().set(key, value);
        return (T) this;
    }

    @Override
    public final String getHeader(String key) {
        return getHeadersBuilder().get(key);
    }

    @Override
    public final T removeAllHeader(String key) {
        getHeadersBuilder().removeAll(key);
        return (T) this;
    }

    @Override
    public final T add(String key, Object value) {
        if (value == null) value = "";
        super.put(key, value);
        return (T) this;
    }

    @Override
    public T add(Map<? extends String, ?> map) {
        putAll(map);
        return (T) this;
    }

    @Override
    public final Map<String, Object> getParams() {
        return this;
    }

    @Override
    public CacheControl getCacheControl() {
        return mCacheControl;
    }

    @Override
    public T cacheControl(CacheControl cacheControl) {
        mCacheControl = cacheControl;
        return (T) this;
    }

    @Override
    public T tag(Object tag) {
        mTag = tag;
        return (T) this;
    }

    @Override
    public Object getTag() {
        return mTag;
    }

    @Override
    public final T setAssemblyEnabled(boolean enabled) {
        mIsAssemblyEnabled = enabled;
        return (T) this;
    }

    @Override
    public final boolean isAssemblyEnabled() {
        return mIsAssemblyEnabled;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + " {" +
                "  \nurl = " + mUrl + '\'' +
                ", \nparam = { " + BuildUtil.toKeyValue(this) + " }" +
                ", \nheaders = { " + (mHBuilder == null ? "" : mHBuilder.build().toString().replace("\n", ",")) + " }" +
                ", \nisAssemblyEnabled = " + mIsAssemblyEnabled +
                ", \ntag = " + mTag +
                ", \ncacheControl = " + mCacheControl +
                " }";
    }
}
