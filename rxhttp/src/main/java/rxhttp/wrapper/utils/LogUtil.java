package rxhttp.wrapper.utils;

import android.util.Log;

import java.io.IOException;
import java.net.URLDecoder;
import java.util.List;

import io.reactivex.annotations.NonNull;
import okhttp3.FormBody;
import okhttp3.Headers;
import okhttp3.HttpUrl.Builder;
import okhttp3.MultipartBody;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okio.Buffer;
import rxhttp.wrapper.exception.HttpStatusCodeException;
import rxhttp.wrapper.exception.ParseException;
import rxhttp.wrapper.param.Param;

/**
 * User: ljx
 * Date: 2019/4/1
 * Time: 17:21
 */
public class LogUtil {

    private static final String TAG = "RxHttp";
    private static final String TAG_RXJAVA = "RxJava";

    private static boolean isDebug = false;

    public static void setDebug(boolean debug) {
        isDebug = debug;
    }

    //打印Http请求连接失败异常日志
    public static void log(Throwable throwable) {
        if (!isDebug) return;
        Log.e(TAG_RXJAVA, throwable.toString());
    }

    //打印Http请求连接失败异常日志
    public static void log(@NonNull Param param, Throwable throwable) {
        if (!isDebug) return;
        throwable.printStackTrace();
        StringBuilder builder = new StringBuilder();
        builder.append("throwable = ")
            .append(throwable.toString());
        if (!(throwable instanceof ParseException) && !(throwable instanceof HttpStatusCodeException)) {
            builder.append("\n\nurl = ")
                .append(URLDecoder.decode(param.getUrl()));
        }
        Log.e(TAG, builder.toString());
    }

    //打印Http返回的正常结果
    public static void log(@NonNull Response response, String result) {
        if (!isDebug) return;
        Request request = response.request();
        String builder = "------------------- request end Method=" +
            request.method() + " Code=" + response.code() + " -------------------" +
            "\n\nurl = " + getEncodedUrlAndParams(request) +
            "\n\nresponse headers = " + response.headers() +
            "\nresult = " + result;
        Log.i(TAG, builder);
    }

    //请求前，打印日志
    public static void log(@NonNull Request request) {
        if (!isDebug) return;
        String builder = "------------------- request start Method=" +
            request.method() + " -------------------" +
            request2Str(request);
        Log.d(TAG, builder);
    }

    private static String request2Str(Request request) {
        return "\n\nurl = " + getEncodedUrlAndParams(request)
            + "\n\nrequest headers = " + request.headers();
    }

    public static String getEncodedUrlAndParams(Request request) {
        String result;
        try {
            result = getRequestParams(request);
        } catch (IOException e) {
            e.printStackTrace();
            result = request.url().toString();
        }
        return URLDecoder.decode(result);
    }

    private static String getRequestParams(Request request) throws IOException {
        RequestBody body = request.body();
        Builder urlBuilder = request.url().newBuilder();

        if (body instanceof FormBody) {
            FormBody formBody = ((FormBody) body);
            for (int i = 0, size = formBody.size(); i < size; i++) {
                urlBuilder.addQueryParameter(formBody.name(i), formBody.value(i));
            }
            return urlBuilder.toString();
        }

        if (body instanceof MultipartBody) {
            MultipartBody multipartBody = (MultipartBody) body;
            List<MultipartBody.Part> parts = multipartBody.parts();
            StringBuilder fileBuilder = new StringBuilder();
            for (int i = 0, size = parts.size(); i < size; i++) {
                MultipartBody.Part part = parts.get(i);
                RequestBody requestBody = part.body();
                Headers headers = part.headers();
                if (headers == null || headers.size() == 0) continue;
                String[] split = headers.value(0).split(";");
                String name = null, fileName = null;
                for (String s : split) {
                    if (s.equals("form-data")) continue;
                    String[] keyValue = s.split("=");
                    if (keyValue.length < 2) continue;
                    String value = keyValue[1].substring(1, keyValue[1].length() - 1);
                    if (name == null) {
                        name = value;
                    } else {
                        fileName = value;
                        break;
                    }
                }
                if (name == null) continue;
                if (requestBody.contentLength() < 1024) {
                    Buffer buffer = new Buffer();
                    requestBody.writeTo(buffer);
                    String value = buffer.readUtf8();
                    urlBuilder.addQueryParameter(name, value);
                } else {
                    if (fileBuilder.length() > 0) {
                        fileBuilder.append("&");
                    }
                    fileBuilder.append(name).append("=").append(fileName);
                }
            }
            return urlBuilder.toString() + "\n\nfiles = " + fileBuilder.toString();
        }

        if (body != null) {
            Buffer buffer = new Buffer();
            body.writeTo(buffer);
            return urlBuilder.toString() + "\n\nparams = " + buffer.readUtf8();
        }
        return urlBuilder.toString();
    }
}
