package httpsender.wrapper.utils;

import android.util.Log;

import httpsender.wrapper.param.Param;
import io.reactivex.annotations.NonNull;
import okhttp3.Request;
import okhttp3.Response;

/**
 * User: ljx
 * Date: 2019/4/1
 * Time: 17:21
 */
public class LogUtil {

    private static boolean isDebug = false;

    public static void setDebug(boolean debug) {
        isDebug = debug;
    }

    //打印Http返回结果
    public static void log(@NonNull Response response, String result) {
        if (!isDebug) return;
        Request request = response.request();
        String builder = "-------------------Method=" +
                request.method() + " Code=" + response.code() + "-------------------" +
                "\nUrl=" + request.url() +
                "\nResult=" + result;
        Log.d("HttpSender", builder);
    }

    public static void log(@NonNull Param param) {
        if (!isDebug) return;
        Log.d("HttpSender", param.toString());
    }
}
