package rxhttp.wrapper.utils;

import android.util.Log;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.Charset;

import io.reactivex.annotations.NonNull;
import okhttp3.FormBody;
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
    public static void log(@NonNull String url, Throwable throwable) {
        if (!isDebug) return;
        throwable.printStackTrace();
        String log = "throwable = " + throwable.toString();
        if (!(throwable instanceof ParseException) && !(throwable instanceof HttpStatusCodeException)) {
            log += "\nurl = " + URLDecoder.decode(url);
        }
        Log.e(TAG, log);
    }

    //打印Http返回的正常结果
    public static void log(@NonNull Response response, String result) {
        if (!isDebug) return;
        Request request = response.request();
        String builder = "------------------- request end Method=" +
            request.method() + " Code=" + response.code() + " -------------------" +
            "\nurl = " + request.url() + getRequestParams(request) +
            "\n\nheaders = " + response.headers() +
            "\nresult = " + result;
        Log.i(TAG, builder);
    }

    //请求前，打印日志
    public static void log(@NonNull Param param) {
        if (!isDebug) return;
        String builder = "------------------- request start Method=" +
            param.getMethod().name() + " " + param.getClass().getSimpleName() +
            " -------------------" +
            "\n\nurl = " + URLDecoder.decode(param.getUrl()) +
            "\n\nheaders = " + param.getHeaders();
        Log.d(TAG, builder);
    }

    public static String getRequestParams(Request request) {
        RequestBody body = request.body();
        if (body == null) return "";
        StringBuilder builder = new StringBuilder();
        if (body instanceof FormBody) {
            builder.append("?");
            FormBody formBody = ((FormBody) body);
            for (int i = 0, size = formBody.size(); i < size; i++) {
                builder.append(i > 0 ? "&" : "");
                builder.append(formBody.name(i)).append("=").append(formBody.value(i));
            }
        } else {
            Buffer buffer = new Buffer();
            try {
                body.writeTo(buffer);
            } catch (IOException e) {
                e.printStackTrace();
            }
            builder.append("\n\nparams = ").append(buffer.readString(Charset.forName("UTF-8")));
        }
        String result = builder.toString();
        return result.length() > 1 ? result : "";
    }
}
