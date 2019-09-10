package com.example.httpsender;

import android.app.Application;

import io.reactivex.functions.Function;
import rxhttp.wrapper.param.Param;
import rxhttp.wrapper.param.RxHttp;

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
        initRxHttp();
    }


    private void initRxHttp() {
        //HttpSender初始化，自定义OkHttpClient对象,非必须
//        RxHttp.init(new OkHttpClient());
//        RxHttp.setOnConverter(s -> s); //设置数据转换器,可用于数据解密

        Function<Param, Param> onParamAssembly = p -> {
            //根据不同请求添加不同参数，子线程执行，每次发送请求前都会被回调
            //为url 添加前缀或者后缀  并重新设置url
            //p.setUrl("");
            return p.add("versionName", "1.0.0")//添加公共参数
                .addHeader("deviceType", "android"); //添加公共请求头
        };
        RxHttp.setDebug(BuildConfig.DEBUG);
        RxHttp.setOnParamAssembly(onParamAssembly);//设置公共参数回调
    }
}
