package rxhttp.wrapper.param;

import java.util.LinkedHashMap;

import rxhttp.wrapper.utils.BuildUtil;
import io.reactivex.annotations.NonNull;
import io.reactivex.annotations.Nullable;
import okhttp3.CacheControl;
import okhttp3.Headers;
import okhttp3.Headers.Builder;

/**
 * 此类是唯一直接实现Param接口的类
 * User: ljx
 * Date: 2019/1/19
 * Time: 14:35
 */
public abstract class AbstractParam extends LinkedHashMap<String, Object> implements Param {

    private String  mUrl;    //链接地址
    private Builder mHBuilder; //请求头构造器

    private boolean mIsAssemblyEnabled = true;

    private Object       mTag;
    private CacheControl mCacheControl;

    public AbstractParam(@NonNull String url) {
        this.mUrl = url;
    }

    /**
     * @return 带参数的url
     */
    @Override
    public final String getUrl() {
        return BuildUtil.mergeUrlAndParams(mUrl, this);
    }

    public AbstractParam setUrl(@NonNull String url) {
        mUrl = url;
        return this;
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
    public Param setHeadersBuilder(Builder builder) {
        mHBuilder = builder;
        return this;
    }

    @Override
    public final Param addHeader(String key, String value) {
        getHeadersBuilder().add(key, value);
        return this;
    }

    @Override
    public final Param addHeader(String line) {
        getHeadersBuilder().add(line);
        return this;
    }

    @Override
    public final Param setHeader(String key, String value) {
        getHeadersBuilder().set(key, value);
        return this;
    }

    @Override
    public final String getHeader(String key) {
        return getHeadersBuilder().get(key);
    }

    @Override
    public final Param removeAllHeader(String key) {
        getHeadersBuilder().removeAll(key);
        return this;
    }

    @Override
    public final Param add(String key, Object value) {
        if (value == null) value = "";
        super.put(key, value);
        return this;
    }

    @Override
    public CacheControl getCacheControl() {
        return mCacheControl;
    }

    @Override
    public Param cacheControl(CacheControl cacheControl) {
        mCacheControl = cacheControl;
        return this;
    }

    @Override
    public Param tag(Object tag) {
        mTag = tag;
        return this;
    }

    @Override
    public Object getTag() {
        return mTag;
    }

    @Override
    public final Param setAssemblyEnabled(boolean enabled) {
        mIsAssemblyEnabled = enabled;
        return this;
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
                ", \nheaders = { " + mHBuilder.build().toString().replace("\n", ",") + " }" +
                ", \nisAssemblyEnabled = " + mIsAssemblyEnabled +
                ", \ntag = " + mTag +
                ", \ncacheControl = " + mCacheControl +
                " }";
    }
}
