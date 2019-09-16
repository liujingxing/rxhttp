package rxhttp.wrapper.param;

import java.util.Map;

import io.reactivex.annotations.NonNull;
import okhttp3.CacheControl;

/**
 * User: ljx
 * Date: 2019/1/19
 * Time: 10:25
 */
public interface IParam<P extends Param> {

    Map<String, Object> getParams();

    P setUrl(@NonNull String url);

    P add(String key, Object value);

    P add(Map<? extends String, ?> map);

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

    P tag(Object tag);

    P cacheControl(CacheControl cacheControl);
}
