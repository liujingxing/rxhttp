package rxhttp.wrapper.param;

import java.util.Map;

import io.reactivex.annotations.NonNull;
import okhttp3.CacheControl;

/**
 * User: ljx
 * Date: 2019/1/19
 * Time: 10:25
 */
public interface ParamBuilder {

    Map<String, Object> getParams();

    Param setUrl(@NonNull String url);

    Param add(String key, Object value);

    Param add(Map<? extends String, ?> map);

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
    Param setAssemblyEnabled(boolean enabled);

    Param tag(Object tag);

    Param cacheControl(CacheControl cacheControl);

    /**
     * 对Json 形式的请求，可直接调用此方法传入Json字符串做参数
     * 注:
     * 1、调用此方法后传入一个长度大于0的字符串后，通过add添加的请求参数将失效(请求头不影响)
     * 2、非Json形式的请求调用此方法，将会抛出异常
     *
     * @param jsonParams Json字符串
     * @return Param对象
     */
    default Param setJsonParams(String jsonParams) {
        throw new UnsupportedOperationException("Please override setJsonParams method if you need");
    }

}
