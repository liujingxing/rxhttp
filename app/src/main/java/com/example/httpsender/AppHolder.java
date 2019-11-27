package com.example.httpsender;

import android.app.Application;

/**
 * User: ljx
 * Date: 2019/3/31
 * Time: 09:11
 */
public class AppHolder extends Application {

    private static AppHolder instance;

    public static AppHolder getInstance() {
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        RxHttpManager.init();
    }
}
