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
        String errorMsg = ExceptionHelper.handleNetworkException(throwable);
        if (throwable instanceof ParseException) {
            String errorCode = throwable.getLocalizedMessage();
            if ("-1".equals(errorCode)) {
                errorMsg = "数据解析失败,请稍后再试";
            } else {
                errorMsg = throwable.getMessage();
                if (TextUtils.isEmpty(errorMsg)) errorMsg = errorCode;//errorMsg为空，显示errorCode
            }
        } else if (throwable instanceof HttpStatusCodeException) {
            String code = throwable.getLocalizedMessage();
            if ("416".equals(code)) {
                errorMsg = "请求范围不符合要求";
            }
        }
        boolean isConsume = onError(throwable, errorMsg);
        if (!isConsume && !TextUtils.isEmpty(errorMsg))
            Tip.show(errorMsg);

    }

    //返回事件是否被消费
    boolean onError(Throwable throwable, String errorMsg) throws Exception;
}
