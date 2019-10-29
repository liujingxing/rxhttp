package rxhttp.wrapper.exception;


import java.io.IOException;

import io.reactivex.annotations.NonNull;
import io.reactivex.annotations.Nullable;
import okhttp3.Headers;
import okhttp3.Request;
import okhttp3.Response;
import rxhttp.wrapper.utils.LogUtil;

/**
 * User: ljx
 * Date: 2018/10/23
 * Time: 22:29
 */
public class ParseException extends IOException {

    private String errorCode;

    private String requestMethod; //请求方法，Get/Post等
    private String requestUrl; //请求Url及参数
    private Headers responseHeaders; //响应头
    private String requestResult; //请求结果

    @Deprecated
    public ParseException(String message, Response response) {
        this("-1", message, response, null);
    }

    @Deprecated
    public ParseException(String message, Response response, String result) {
        this("-1", message, response, result);
    }

    public ParseException(@NonNull String code, String message, Response response) {
        this(code, message, response, null);
    }

    public ParseException(@NonNull String code, String message, Response response, String result) {
        super(message);
        errorCode = code;
        requestResult = result;

        Request request = response.request();
        requestMethod = request.method();
        requestUrl = request.url().toString() + LogUtil.getRequestParams(request);
        responseHeaders = response.headers();
    }

    public String getRequestResult() {
        return requestResult;
    }

    public String getErrorCode() {
        return errorCode;
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

    @Nullable
    @Override
    public String getLocalizedMessage() {
        return errorCode;
    }

    @Override
    public String toString() {
        return getClass().getName() + ":" +
            " Method=" + requestMethod +
            " Code=" + errorCode +
            "\n\nurl = " + requestUrl +
            "\n\nheaders = " + responseHeaders +
            "\nmessage = " + getMessage();
    }
}
