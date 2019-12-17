package com.example.httpsender;

import android.app.Application;
import android.content.Context;

import androidx.multidex.MultiDex;

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
        RxHttpManager.init(this);
    }

    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

}
