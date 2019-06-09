package com.example.httpsender;


import android.text.TextUtils;

import io.reactivex.functions.Consumer;
import rxhttp.wrapper.exception.HttpStatusCodeException;
import rxhttp.wrapper.exception.ParseException;


/**
 * RxJava 错误回调 ,加入网络异常处理
 * User: ljx
 * Date: 2019/04/29
 * Time: 11:15
 */
public interface OnError extends Consumer<Throwable> {

    @Override
    default void accept(Throwable throwable) throws Exception {
        ExceptionHelper.handleNetworkException(throwable);
        if (throwable instanceof ParseException) {
            String errorCode = throwable.getLocalizedMessage();
            if (!TextUtils.isEmpty(errorCode)) { //errorCode不为空，显示服务器返回的错误提示
                String errorMsg = throwable.getMessage();
                Tip.show(TextUtils.isEmpty(errorMsg) ? errorCode : errorMsg); //errorMsg为空，显示errorCode
            }
        } else if (throwable instanceof HttpStatusCodeException) {
            String code = throwable.getLocalizedMessage();
            if ("416".equals(code)) {
                Tip.show("请求范围不符合要求");
            }
        }
        onError(throwable);
    }

    void onError(Throwable throwable) throws Exception;
}
