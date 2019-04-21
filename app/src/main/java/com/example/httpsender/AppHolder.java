package com.example.httpsender;

import android.app.Application;
import android.util.Log;

import httpsender.HttpSender;
import httpsender.wrapper.param.*;
import io.reactivex.functions.Function;
import io.reactivex.plugins.RxJavaPlugins;

/**
 * User: ljx
 * Date: 2019/3/31
 * Time: 09:11
 */
public class AppHolder extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        httpSenderInit();
    }


    private void httpSenderInit() {
        //HttpSender初始化，自定义OkHttpClient对象,非必须
//        HttpSender.init(new OkHttpClient());

        //设置RxJava 全局异常处理
        RxJavaPlugins.setErrorHandler(throwable -> {
            Log.e("LJX", "setErrorHandler=" + throwable);
            /**
             * RxJava2的一个重要的设计理念是：不吃掉任何一个异常,即抛出的异常无人处理，便会导致程序崩溃
             * 这就会导致一个问题，当RxJava2“downStream”取消订阅后，“upStream”仍有可能抛出异常，
             * 这时由于已经取消订阅，“downStream”无法处理异常，此时的异常无人处理，便会导致程序崩溃
             */
        });
        HttpSender.setDebug(BuildConfig.DEBUG);
        HttpSender.setOnParamAssembly(new Function<Param, Param>() {
            /**
             * <p>在这里可以为所有请求添加公共参数，也可以为url统一添加前缀或者后缀
             * <p>子线程执行，每次发送请求前都会被回调
             *
             * @param p Param
             * @return 修改后的Param对象
             */
            @Override
            public Param apply(Param p) {

                //根据不同请求添加不同参数
                if (p instanceof GetRequest) {

                } else if (p instanceof PostRequest) {

                } else if (p instanceof PutRequest) {

                } else if (p instanceof DeleteRequest) {

                }
                //为url 添加前缀或者后缀  并重新设置url
                //p.setUrl("");
                return p.add("versionName", "1.0.0")//添加公共参数
                        .addHeader("deviceType", "android"); //添加公共请求头
            }
        });
    }
}
