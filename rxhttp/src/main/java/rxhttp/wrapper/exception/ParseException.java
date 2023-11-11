package rxhttp.wrapper.exception;


import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

import okhttp3.Headers;
import okhttp3.HttpUrl;
import okhttp3.Request;
import okhttp3.Response;

/**
 * User: ljx
 * Date: 2018/10/23
 * Time: 22:29
 */
public class ParseException extends IOException {

    private final String errorCode;

    private final String requestMethod; //请求方法，Get/Post等
    private final HttpUrl httpUrl; //请求Url及查询参数
    private final Headers responseHeaders; //响应头

    public ParseException(@NotNull String code, String message, Response response) {
        super(message);
        errorCode = code;

        Request request = response.request();
        requestMethod = request.method();
        httpUrl = request.url();
        responseHeaders = response.headers();
    }

    public String getErrorCode() {
        return errorCode;
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

    @Nullable
    @Override
    public String getLocalizedMessage() {
        return errorCode;
    }

    @Override
    public String toString() {
        String result = getClass().getName() + ": code is " + errorCode;
        String message = getMessage();
        if (message != null && !message.trim().isEmpty()) {
            result += ", " + message;
        }
        return result;
    }
}
