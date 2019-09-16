package rxhttp.wrapper.utils;

import android.util.Log;

import io.reactivex.annotations.NonNull;
import okhttp3.FormBody;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
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

    private static boolean isDebug = false;

    public static void setDebug(boolean debug) {
        isDebug = debug;
    }

    //打印Http请求连接失败异常日志
    public static void log(@NonNull String url, Throwable throwable) {
        if (!isDebug) return;
        throwable.printStackTrace();
        String log = "throwable = " + throwable.toString();
        if (!(throwable instanceof ParseException) && !(throwable instanceof HttpStatusCodeException)) {
            log += "\nurl=" + url;
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
            "\n\nHeaders = " + response.headers() +
            "\nResult = " + result;
        Log.i(TAG, builder);
    }

    public static void log(@NonNull Param param) {
        if (!isDebug) return;
        String requestInfo = "------------------- request start Method=" + param.getMethod().name() +
            " " + param.getClass().getSimpleName() + " -------------------" +
            "  \nurl = " + param.getUrl() +
            "\n\nheaders = " + param.getHeaders();

        Log.d(TAG, requestInfo);
    }

    public static String getRequestParams(Request request) {
        RequestBody body = request.body();
        StringBuilder builder = new StringBuilder("?");
        if (body instanceof FormBody) {
            FormBody formBody = ((FormBody) body);
            for (int i = 0, size = formBody.size(); i < size; i++) {
                builder.append(i > 0 ? "&" : "");
                builder.append(formBody.name(i)).append("=").append(formBody.value(i));
            }
        }
        String result = builder.toString();
        return result.length() > 1 ? result : "";
    }
}
