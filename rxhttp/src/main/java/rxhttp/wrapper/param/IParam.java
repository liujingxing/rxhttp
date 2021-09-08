package rxhttp.wrapper.param;

import java.util.Map;
import java.util.Map.Entry;

import okhttp3.CacheControl;
import rxhttp.wrapper.annotations.NonNull;
import rxhttp.wrapper.annotations.Nullable;

/**
 * User: ljx
 * Date: 2019/1/19
 * Time: 10:25
 */
@SuppressWarnings("unchecked")
public interface IParam<P extends Param<P>> {

    P setUrl(@NonNull String url);

    P add(String key, Object value);

    default P addAll(@NonNull Map<String, ?> map) {
        for (Entry<String, ?> entry : map.entrySet()) {
            add(entry.getKey(), entry.getValue());
        }
        return (P) this;
    }

    P addQuery(String key, @Nullable Object value);

    P addEncodedQuery(String key, @Nullable Object value);

    default P addAllQuery(@NonNull Map<String, ?> map) {
        for (Entry<String, ?> entry : map.entrySet()) {
            addQuery(entry.getKey(), entry.getValue());
        }
        return (P) this;
    }

    default P addAllEncodedQuery(@NonNull Map<String, ?> map) {
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
