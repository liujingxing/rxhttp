package httpsender.wrapper.param;

import java.util.LinkedHashMap;

import httpsender.wrapper.utils.BuildUtil;
import io.reactivex.annotations.NonNull;
import io.reactivex.annotations.Nullable;
import okhttp3.CacheControl;
import okhttp3.Headers;
import okhttp3.Headers.Builder;

/**
 * User: ljx
 * Date: 2019/1/19
 * Time: 14:35
 */
public abstract class AbstractParam extends LinkedHashMap<String, String> implements Param {

    private String  mUrl;    //链接地址
    private Builder mHBuilder; //请求头构造器

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
    public final Param add(String key, String value) {
        super.put(key, value);
        return this;
    }

    @Override
    public CacheControl getCacheControl() {
        return null;
    }

    @Override
    public Object getTag() {
        return null;
    }

    /**
     * @return 所有参数(不包括url及请求头)以 key=value 格式拼接(用 & 拼接)在一起
     */
    @Override
    public final String toString() {
        return BuildUtil.toKeyValue(this);
    }
}
