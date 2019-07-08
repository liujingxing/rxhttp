package rxhttp.wrapper.utils;

import android.util.Log;

import io.reactivex.annotations.NonNull;
import okhttp3.Request;
import okhttp3.Response;
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
        Log.e(TAG, "url=" + url + "\n throwable=" + throwable.toString());
    }

    //打印Http返回的正常结果
    public static void log(@NonNull Response response, String result) {
        if (!isDebug) return;
        Request request = response.request();
        String builder = "------------------- request end Method=" +
            request.method() + " Code=" + response.code() + " -------------------" +
            "\nUrl = " + request.url() +
            "\n\nHeaders = " + response.headers() +
            "\nResult = " + result;
        Log.i(TAG, builder);
    }

    public static void log(@NonNull Param param) {
        if (!isDebug) return;
        String requestInfo = "------------------- request start " +
            param.getClass().getSimpleName() + " -------------------" +
            "  \nurl = " + param.getSimpleUrl() + "?" +
            BuildUtil.toKeyValue(param.getParams()) +
            "\n\nheaders = " + param.getHeaders();

        Log.d(TAG, requestInfo);
    }
}
