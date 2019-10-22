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

    private String mErrorCode;

    private String requestMethod; //请求方法，Get/Post等
    private String requestUrl; //请求Url及参数
    private Headers responseHeaders; //响应头

    @Deprecated
    public ParseException(String message) {
        this("-1", message);
    }

    @Deprecated
    public ParseException(@NonNull String code, String message) {
        super(message);
        mErrorCode = code;
    }

    public ParseException(String message, Response response) {
        this("-1", message, response);
    }

    public ParseException(@NonNull String code, String message, Response response) {
        super(message);
        mErrorCode = code;

        Request request = response.request();
        requestMethod = request.method();
        requestUrl = request.url().toString() + LogUtil.getRequestParams(request);
        responseHeaders = response.headers();
    }

    public String getErrorCode() {
        return mErrorCode;
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
        return mErrorCode;
    }

    @Override
    public String toString() {
        return getClass().getName() + ":" +
            " Method=" + requestMethod +
            " Code=" + mErrorCode +
            "\n\nurl = " + requestUrl +
            "\n\nheaders = " + responseHeaders +
            "\nmessage = " + getMessage();
    }
}
