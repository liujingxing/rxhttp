package rxhttp.wrapper.exception;

import java.io.IOException;

import okhttp3.Headers;
import okhttp3.HttpUrl;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * Http 状态码 code < 200 || code >= 300, 抛出此异常
 * <p>
 * 可通过{@link #getLocalizedMessage()}方法获取code
 * User: ljx
 * Date: 2019-06-09
 * Time: 09:56
 */
public final class HttpStatusCodeException extends IOException {

    private final Protocol protocol; //http协议
    private final int statusCode; //Http响应状态吗
    private final String requestMethod; //请求方法，Get/Post等
    private final HttpUrl httpUrl; //请求Url及查询参数
    private final Headers responseHeaders; //响应头
    private final ResponseBody body;
    private String result;    //返回结果

    public HttpStatusCodeException(Response response) {
        super(response.message());
        protocol = response.protocol();
        statusCode = response.code();
        Request request = response.request();
        requestMethod = request.method();
        httpUrl = request.url();
        responseHeaders = response.headers();
        body = response.body();
    }

    @Override
    public String getLocalizedMessage() {
        return String.valueOf(statusCode);
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getRequestMethod() {
        return requestMethod;
    }

    public String getRequestUrl() {
        return httpUrl.toString();
    }

    public HttpUrl getHttpUrl() {
        return httpUrl;
    }

    public Headers getResponseHeaders() {
        return responseHeaders;
    }

    public ResponseBody getResponseBody() {
        return body;
    }

    public String getResult() throws IOException {
        if (result == null) {
            result = body.string();
        }
        return result;
    }

    @Override
    public String toString() {
        return getClass().getName() + ": " + protocol + " " + statusCode + " " + getMessage();
    }
}
