package com.example.httpsender;

import android.arch.lifecycle.LifecycleOwner;
import android.util.Log;

import com.rxjava.rxlife.BaseScope;
import com.rxjava.rxlife.RxLife;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;

/**
 * User: ljx
 * Date: 2019-05-26
 * Time: 15:20
 */
public class Presenter extends BaseScope {

    public Presenter(LifecycleOwner owner) {
        super(owner); //添加生命周期监听
        Observable.interval(1, 1, TimeUnit.SECONDS)
            .as(RxLife.as(this)) //这里的this 为Scope接口对象
            .subscribe(aLong -> {
                Log.e("LJX", "accept aLong=" + aLong);
            });
    }
}
