package rxhttp.wrapper.param;

import okhttp3.CacheControl;
import okhttp3.Headers;
import okhttp3.Request;
import okhttp3.RequestBody;
import rxhttp.wrapper.utils.BuildUtil;

/**
 * 用于构建一个{@link Request}
 * User: ljx
 * Date: 2019/1/19
 * Time: 17:24
 */
public interface IRequest {

    /**
     * @return 带参数的url
     */
    String getUrl();

    /**
     * @return 不带参数的url
     */
    String getSimpleUrl();


    /**
     * @return 请求方法，GET、POST等
     */
    String getMethod();

    /**
     * @return 请求体
     * GET、HEAD不能有请求体，
     * POST、PUT、PATCH、PROPPATCH、REPORT请求必须要有请求体
     * 其它请求可有可无
     */
    RequestBody getRequestBody();

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

    /**
     * @return 根据以上定义的方法构建一个请求
     */
    default Request buildRequest() {
        return BuildUtil.buildRequest(this);
    }
}
