package rxhttp.wrapper.param;


import okhttp3.Headers;
import okhttp3.HttpUrl;
import okhttp3.Request;
import okhttp3.RequestBody;

/**
 * 用于构建一个{@link Request}
 * User: ljx
 * Date: 2019/1/19
 * Time: 17:24
 */
public interface IRequest {

    /**
     * @return 带参数的url 仅{@link NoBodyParam}请求会将参数以 ?key=value... 的形式拼接在url后面
     */
    default String getUrl() {
        return getSimpleUrl();
    }

    /**
     * @return 不带参数的url
     */
    String getSimpleUrl();

    /**
     * @return HttpUrl
     */
    HttpUrl getHttpUrl();

    /**
     * @return 请求方法，GET、POST等
     */
    Method getMethod();

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
     * @return 根据以上定义的方法构建一个请求
     */
    Request buildRequest();
}
