package com.example.httpsender;

import android.arch.lifecycle.GenericLifecycleObserver;
import android.arch.lifecycle.Lifecycle.Event;
import android.arch.lifecycle.LifecycleOwner;

import com.rxjava.rxlife.Scope;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;

/**
 * User: ljx
 * Date: 2019-05-31
 * Time: 21:50
 */
public class BaseScope implements Scope, GenericLifecycleObserver {

    private CompositeDisposable mDisposables;

    public BaseScope(LifecycleOwner owner) {
        owner.getLifecycle().addObserver(this);
    }

    @Override
    public void onScopeStart(Disposable d) {
        addDisposable(d);
    }

    @Override
    public void onScopeEnd() {

    }

    private void addDisposable(Disposable disposable) {
        CompositeDisposable disposables = mDisposables;
        if (disposables == null) {
            disposables = mDisposables = new CompositeDisposable();
        }
        disposables.add(disposable);
    }

    private void dispose() {
        final CompositeDisposable disposables = mDisposables;
        if (disposables == null) return;
        disposables.dispose();
    }

    @Override
    public void onStateChanged(LifecycleOwner source, Event event) {
        //Activity/Fragment 生命周期回调
        if (event == Event.ON_DESTROY) {  //Activity/Fragment 销毁
            source.getLifecycle().removeObserver(this);
            dispose(); //中断RxJava管道
        }
    }
}
