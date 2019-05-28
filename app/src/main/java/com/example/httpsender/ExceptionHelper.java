package com.example.httpsender;


import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;


import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

/**
 * 异常处理帮助类
 * User: ljx
 * Date: 2019/04/29
 * Time: 11:15
 */
public class ExceptionHelper {

    //处理网络异常
    public static <T> boolean handleNetworkException(T throwable) {
        if (throwable instanceof UnknownHostException) {
            if (!isNetworkConnected(AppHolder.getInstance())) {
                Tip.show(R.string.network_error);
            } else {
                Tip.show(R.string.notify_no_network);
            }
            return true;
        } else if (throwable instanceof SocketTimeoutException) {
            Tip.show(R.string.time_out_please_try_again_later);
            return true;
        } else if (throwable instanceof ConnectException) {
            Tip.show(R.string.esky_service_exception);
            return true;
        }
        return false;
    }

    public static boolean isNetworkConnected(Context context) {
        if (context != null) {
            ConnectivityManager mConnectivityManager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo mNetworkInfo = mConnectivityManager.getActiveNetworkInfo();
            if (mNetworkInfo != null) {
                return mNetworkInfo.isAvailable();
            }
        }

        return false;
    }
}
