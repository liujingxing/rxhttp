package rxhttp.wrapper.exception;

import java.io.IOException;

import okhttp3.Headers;
import okhttp3.Request;
import okhttp3.Response;
import rxhttp.wrapper.annotations.Nullable;
import rxhttp.wrapper.utils.LogUtil;

/**
 * Http 状态码 小于200或者大于等于300时,或者ResponseBody等于null，抛出此异常
 * <p>
 * 可通过{@link #getLocalizedMessage()}方法获取code
 * User: ljx
 * Date: 2019-06-09
 * Time: 09:56
 */
public final class HttpStatusCodeException extends IOException {

    private String statusCode; //Http响应状态吗
    private String result;    //返回结果
    private String requestMethod; //请求方法，Get/Post等
    private String requestUrl; //请求Url及参数
    private Headers responseHeaders; //响应头

    public HttpStatusCodeException(Response response) {
        this(response, null);
    }

    public HttpStatusCodeException(Response response, String result) {
        super(response.message());
        statusCode = String.valueOf(response.code());
        Request request = response.request();
        requestMethod = request.method();
        requestUrl = LogUtil.getEncodedUrlAndParams(request);
        responseHeaders = response.headers();
        this.result = result;
    }

    @Nullable
    @Override
    public String getLocalizedMessage() {
        return statusCode;
    }

    public String getStatusCode() {
        return statusCode;
    }

    public String getRequestMethod() {
        return requestMethod;
    }

    public String getRequestUrl() {
        return requestUrl;
    }

    public Headers getResponseHeaders() {
        return responseHeaders;
    }

    public String getResult() {
        return result;
    }

    @Override
    public String toString() {
        return getClass().getName() + ":" +
            " Method=" + requestMethod +
            " Code=" + statusCode +
            "\nmessage = " + getMessage() +
            "\n\n" + requestUrl +
            "\n\n" + responseHeaders +
            "\n" + result;
    }
}
