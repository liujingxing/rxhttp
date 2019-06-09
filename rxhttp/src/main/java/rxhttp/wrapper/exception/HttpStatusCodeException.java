package rxhttp.wrapper.exception;

import java.io.IOException;

import io.reactivex.annotations.Nullable;
import okhttp3.Headers;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import rxhttp.wrapper.utils.LogUtil;

/**
 * Http 状态码 小于200或者大于等于300时 (code < 200 || code >= 300)，抛出此异常
 * <p>
 * 可通过{@link #getLocalizedMessage()}方法获取code
 * User: ljx
 * Date: 2019-06-09
 * Time: 09:56
 */
public class HttpStatusCodeException extends IOException {

    private String statusCode; //Http响应状态吗
    private String requestMethod; //请求方法，Get/Post等
    private String requestUrl; //请求Url
    private Headers responseHeaders; //响应头
    private String responseResult; //响应结果

    public HttpStatusCodeException(Response response) {
        super(response.message());
        statusCode = String.valueOf(response.code());
        Request request = response.request();
        requestMethod = request.method();
        requestUrl = request.url().toString();
        responseHeaders = response.headers();
        responseResult = getResponseResult(response);
        LogUtil.log(this);
    }

    private String getResponseResult(Response response) {
        ResponseBody body = response.body();
        if (body == null) return "ResponseBody is null";
        try {
            return body.string();
        } catch (IOException e) {
            return e.getMessage();
        }
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

    public String getResponseResult() {
        return responseResult;
    }

    @Override
    public String toString() {
        return "-------------------" +
            " Method=" + requestMethod +
            " Code=" + statusCode + " HttpStatusCodeException -------------------" +
            "\nUrl = " + requestUrl +
            "\n\nHeaders = " + responseHeaders +
            "\nResult = " + responseResult;
    }
}
