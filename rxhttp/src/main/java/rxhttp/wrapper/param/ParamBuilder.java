package rxhttp.wrapper.param;

import java.util.Map;

import io.reactivex.annotations.NonNull;
import okhttp3.CacheControl;

/**
 * User: ljx
 * Date: 2019/1/19
 * Time: 10:25
 */
public interface ParamBuilder<T extends Param> {

    Map<String, Object> getParams();

    T setUrl(@NonNull String url);

    T add(String key, Object value);

    T add(Map<? extends String, ?> map);

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
    T setAssemblyEnabled(boolean enabled);

    T tag(Object tag);

    T cacheControl(CacheControl cacheControl);
}
