package rxhttp.wrapper.exception;

import java.io.IOException;

import io.reactivex.annotations.Nullable;
import okhttp3.Headers;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Http 状态码 小于200或者大于等于300时 (code < 200 || code >= 300),或者ResponseBody等于null，抛出此异常
 * <p>
 * 可通过{@link #getLocalizedMessage()}方法获取code
 * User: ljx
 * Date: 2019-06-09
 * Time: 09:56
 */
public final class HttpStatusCodeException extends IOException {

    private String statusCode; //Http响应状态吗
    private String requestMethod; //请求方法，Get/Post等
    private String requestUrl; //请求Url
    private Headers responseHeaders; //响应头

    HttpStatusCodeException(Response response) {
        super(response.message());
        statusCode = String.valueOf(response.code());
        Request request = response.request();
        requestMethod = request.method();
        requestUrl = request.url().toString();
        responseHeaders = response.headers();
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

    @Override
    public String toString() {
        return getClass().getName() + ":" +
            " Method=" + requestMethod +
            " Code=" + statusCode +
            "\n\nHeaders = " + responseHeaders +
            "\nMessage = " + getMessage();
    }
}
