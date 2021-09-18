package com.example.httpsender.kt

import android.content.Context
import android.net.ConnectivityManager
import com.example.httpsender.AppHolder
import com.example.httpsender.Tip
import com.google.gson.JsonSyntaxException
import kotlinx.coroutines.TimeoutCancellationException
import rxhttp.wrapper.exception.HttpStatusCodeException
import rxhttp.wrapper.exception.ParseException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.util.concurrent.TimeoutException

/**
 * User: ljx
 * Date: 2020-02-07
 * Time: 21:04
 */
fun Throwable.show() {
    errorMsg.show()
}

fun String.show() {
    Tip.show(this)
}

val Throwable.errorCode: Int
    get() =
        when (this) {
            is HttpStatusCodeException -> this.statusCode //Http状态码异常
            is ParseException -> this.errorCode.toIntOrNull() ?: -1     //业务code异常
            else -> -1
        }

val Throwable.errorMsg: String
    get() {
        return if (this is UnknownHostException) { //网络异常
            if (!isNetworkConnected(AppHolder.getInstance()))
                "当前无网络，请检查你的网络设置"
            else
                "网络连接不可用，请稍后重试！"
        } else if (
            this is SocketTimeoutException  //okhttp全局设置超时
            || this is TimeoutException     //rxjava中的timeout方法超时
            || this is TimeoutCancellationException  //协程超时
        ) {
            "连接超时,请稍后再试"
        } else if (this is ConnectException) {
            "网络不给力，请稍候重试！"
        } else if (this is HttpStatusCodeException) {               //请求失败异常
            "Http状态码异常 $message"
        } else if (this is JsonSyntaxException) {  //请求成功，但Json语法异常,导致解析失败
            "数据解析失败,请检查数据是否正确"
        } else if (this is ParseException) {       // ParseException异常表明请求成功，但是数据不正确
            this.message ?: errorCode   //msg为空，显示code
        } else {
            message ?: this.toString()
        }
    }

private fun isNetworkConnected(context: Context): Boolean {
    val mConnectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val mNetworkInfo = mConnectivityManager.activeNetworkInfo
    if (mNetworkInfo != null) {
        return mNetworkInfo.isAvailable
    }
    return false
}
