package com.example.httpsender;

import android.arch.lifecycle.ViewModel;

import com.rxjava.rxlife.Scope;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;

/**
 * User: ljx
 * Date: 2019-05-31
 * Time: 21:50
 */
public class ScopeViewModel extends ViewModel implements Scope {

    private CompositeDisposable mDisposables;

    @Override
    public void onScopeStart(Disposable d) {
        addDisposable(d);//订阅事件时回调
    }

    @Override
    public void onScopeEnd() {
        //事件正常结束时回调
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
    protected void onCleared() {
        super.onCleared(); //Activity/Fragment 销毁时回调
        dispose(); //中断RxJava管道
    }
}
