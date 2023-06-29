package rxhttp.wrapper.param;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import okhttp3.CacheControl;

/**
 * User: ljx
 * Date: 2019/1/19
 * Time: 10:25
 */
@SuppressWarnings("unchecked")
public interface IParam<P extends Param<P>> {

    P setUrl(@NotNull String url);

    P add(String key, Object value);

    P addPath(String name, Object value);

    P addEncodedPath(String name, Object value);

    default P addAll(@NotNull Map<String, ?> map) {
        for (Entry<String, ?> entry : map.entrySet()) {
            add(entry.getKey(), entry.getValue());
        }
        return (P) this;
    }

    P addQuery(String key, @Nullable Object value);

    P addEncodedQuery(String key, @Nullable Object value);

    P removeAllQuery(String key);

    default P setQuery(String key, @Nullable Object value) {
        removeAllQuery(key);
        return addQuery(key, value);
    }

    default P setEncodedQuery(String key, @Nullable Object value) {
        removeAllQuery(key);
        return addEncodedQuery(key, value);
    }

    default P addAllQuery(String key, @Nullable List<?> values) {
        if (values == null) return addQuery(key, null);
        for (Object value : values) {
            addQuery(key, value);
        }
        return (P) this;
    }

    default P addAllEncodedQuery(String key, @Nullable List<?> values) {
        if (values == null) return addEncodedQuery(key, null);
        for (Object value : values) {
            addEncodedQuery(key, value);
        }
        return (P) this;
    }

    default P addAllQuery(@NotNull Map<String, ?> map) {
        for (Entry<String, ?> entry : map.entrySet()) {
            addQuery(entry.getKey(), entry.getValue());
        }
        return (P) this;
    }

    default P addAllEncodedQuery(@NotNull Map<String, ?> map) {
        for (Entry<String, ?> entry : map.entrySet()) {
            addEncodedQuery(entry.getKey(), entry.getValue());
        }
        return (P) this;
    }

    /**
     * @return 判断是否对参数添加装饰，即是否添加公共参数
     */
    boolean isAssemblyEnabled();

    /**
     * 设置是否对参数添加装饰，即是否添加公共参数
     *
     * @param enabled true 是
     * @return Param
     */
    P setAssemblyEnabled(boolean enabled);

    default P tag(@Nullable Object tag) {
        return tag(Object.class, tag);
    }

    <T> P tag(Class<? super T> type, @Nullable T tag);

    P cacheControl(CacheControl cacheControl);
}
