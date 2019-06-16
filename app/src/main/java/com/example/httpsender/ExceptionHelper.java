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
    public static <T> String handleNetworkException(T throwable) {
        int stringId = -1;
        if (throwable instanceof UnknownHostException) {
            if (!isNetworkConnected(AppHolder.getInstance())) {
                stringId = R.string.network_error;
            } else {
                stringId = R.string.notify_no_network;
            }
        } else if (throwable instanceof SocketTimeoutException) {
            stringId = R.string.time_out_please_try_again_later;
        } else if (throwable instanceof ConnectException) {
            stringId = R.string.esky_service_exception;
        }
        return stringId == -1 ? null : AppHolder.getInstance().getString(stringId);
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
