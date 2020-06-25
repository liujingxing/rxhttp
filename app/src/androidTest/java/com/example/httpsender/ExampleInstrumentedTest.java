package com.example.httpsender;

import android.content.Context;
import android.util.Log;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.Test;
import org.junit.runner.RunWith;

import io.reactivex.rxjava3.disposables.Disposable;
import rxhttp.wrapper.param.RxHttp;


/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {
    @Test
    public void useAppContext() {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        Disposable subscribe = RxHttp.get("https://www.wanandroid.com/article/list/0/json")
            .asString()
            .subscribe(s -> {
                System.out.println(s);
            }, throwable -> {
                Log.e("LJX", "useAppContext");
            });
        
        while (!subscribe.isDisposed()) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}