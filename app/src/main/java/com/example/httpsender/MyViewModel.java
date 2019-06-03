package com.example.httpsender;

import android.util.Log;

import com.rxjava.rxlife.RxLife;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;

/**
 * User: ljx
 * Date: 2019-05-31
 * Time: 21:50
 */
public class MyViewModel extends ScopeViewModel {

    public MyViewModel() {
        Observable.interval(1, 1, TimeUnit.SECONDS)
            .as(RxLife.asOnMain(this))
            .subscribe(aLong -> {
                Log.e("LJX", "MyViewModel aLong=" + aLong);
            });
    }

}
