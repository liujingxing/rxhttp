package rxhttp.wrapper.param;

import okhttp3.CacheControl;
import okhttp3.Headers;

/**
 * 用于构建没有RequestBody请求的Request
 * User: ljx
 * Date: 2019/1/19
 * Time: 17:24
 */
public interface NoBodyRequest {

    /**
     * @return 带参数的url
     */
    String getUrl();

    /**
     * @return 不带参数的url
     */
    String getSimpleUrl();


    /**
     * @return 请求头信息
     */
    String getMethod();

    /**
     * @return 请求头信息
     */
    Headers getHeaders();

    /**
     * @return tag
     */
    Object getTag();

    /**
     * @return 缓存控制器
     */
    CacheControl getCacheControl();
}
