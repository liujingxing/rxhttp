package com.example.httpsender;

import android.os.Bundle;
import android.view.View;

import java.io.File;

import androidx.appcompat.app.AppCompatActivity;
import httpsender.Function;
import httpsender.HttpSender;
import httpsender.wrapper.entity.Progress;
import httpsender.wrapper.param.*;
import httpsender.wrapper.parse.SimpleParser;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.plugins.RxJavaPlugins;


public class MainActivity extends AppCompatActivity {

    //管理HttpSender请求的生命周期，也是RxJava观察者的生命周期
    private CompositeDisposable mCompositeDisposable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        httpSenderInit();
    }

    public void onClick(View view) {
        sendGet();
    }

    //HttpSender初始化
    private void httpSenderInit() {
        //HttpSender初始化，自定义OkHttpClient对象,非必须
//        HttpSender.init(new OkHttpClient());

        //设置RxJava 全局异常处理
        RxJavaPlugins.setErrorHandler(throwable -> {
            /**
             * RxJava2的一个重要的设计理念是：不吃掉任何一个异常,即抛出的异常无人处理，便会导致程序崩溃
             * 这就会导致一个问题，当RxJava2“downStream”取消订阅后，“upStream”仍有可能抛出异常，
             * 这时由于已经取消订阅，“downStream”无法处理异常，此时的异常无人处理，便会导致程序崩溃
             */
        });

        HttpSender.setOnParamAssembly(new Function() {
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

    //发送Get请求
    private void sendGet() {
        String url = "http://ip.taobao.com/service/getIpInfo.php";
        Param param = Param.get(url) //这里get,代表Get请求
                .add("ip", "63.223.108.42")//添加参数
                .addHeader("accept", "*/*") //添加请求头
                .addHeader("connection", "Keep-Alive")
                .addHeader("user-agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
        Disposable disposable = HttpSender
                .from(param, new DataParser<Address>() {}) //from操作符，是异步操作
                .observeOn(AndroidSchedulers.mainThread()) //主线程回调
                .subscribe(address -> {
                    //accept方法参数类型由上面DataParser传入的泛型类型决定
                    //走到这里说明Http请求成功，并且数据正确
                }, throwable -> {
                    //Http请求出现异常，有可能是网络异常，数据异常等
                });
        addDisposable(disposable);//处理RxJava生命周期
    }

    //发送Post请求
    private void sendPost() {
        String url = "http://ip.taobao.com/service/getIpInfo.php";
        Param param = Param.postForm(url) //这里get,代表Get请求
                .add("ip", "63.223.108.42")//添加参数
                .addHeader("accept", "*/*") //添加请求头
                .addHeader("connection", "Keep-Alive")
                .addHeader("user-agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
        Disposable disposable = HttpSender
                .from(param, new DataParser<Address>() {}) //from操作符，是异步操作
                .observeOn(AndroidSchedulers.mainThread()) //主线程回调
                .subscribe(address -> {
                    //accept方法参数类型由上面DataParser传入的泛型类型决定
                    //走到这里说明Http请求成功，并且数据正确
                }, throwable -> {
                    //Http请求出现异常，有可能是网络异常，数据异常等
                });
        addDisposable(disposable);//处理RxJava生命周期
    }

    //下载文件，带进度
    private void download() {
        String url = "http://update.9158.com/miaolive/Miaolive.apk";
        String destPath = getExternalCacheDir() + "/" + System.currentTimeMillis() + ".apk";
        Param param = Param.get(url); //这里get,代表Get请求
        Disposable disposable = HttpSender
                .download(param, destPath) //这里泛型只需要传入Data类的泛型即可，不需要传Data<Book>
                .observeOn(AndroidSchedulers.mainThread()) //主线程回调
                .doOnNext(progress -> {
                    //下载进度回调,0-100，仅在进度有更新时才会回调
                    int currentProgress = progress.getProgress(); //当前进度 0-100
                    long currentSize = progress.getCurrentSize(); //当前已下载的字节大小
                    long totalSize = progress.getTotalSize();     //要下载的总字节大小
                })
                .filter(Progress::isCompleted)//过滤事件，下载完成，才继续往下走
                .map(Progress::getResult) //到这，说明下载完成，拿到Http返回结果并继续往下走
                .subscribe(s -> { //s为String类型
                    //下载成功，处理相关逻辑
                }, throwable -> {
                    //下载失败，处理相关逻辑
                });
        addDisposable(disposable);//处理RxJava生命周期
    }

    //上传文件，带进度
    private void upload() {
        String url = "http://www.......";
        Param param = Param.postForm(url) //发送Form表单形式的Post请求
                .add("file1", new File("xxx/1.png"))
                .add("file2", new File("xxx/2.png"))
                .add("key1", "value1")//添加参数，非必须
                .add("key2", "value2")//添加参数，非必须
                .addHeader("versionCode", "100"); //添加请求头,非必须
        Disposable disposable = HttpSender
                .upload(param, new SimpleParser<String>() {}) //注:如果需要监听上传进度，使用upload操作符
                .observeOn(AndroidSchedulers.mainThread()) //主线程回调
                .doOnNext(progress -> {
                    //上传进度回调,0-100，仅在进度有更新时才会回调
                    int currentProgress = progress.getProgress(); //当前进度 0-100
                    long currentSize = progress.getCurrentSize(); //当前已上传的字节大小
                    long totalSize = progress.getTotalSize();     //要上传的总字节大小
                })
                .filter(Progress::isCompleted)//过滤事件，上传完成，才继续往下走
                .map(Progress::getResult) //到这，说明下载完成，拿到Http返回结果并继续往下走
                .subscribe(s -> { //s为String类型，由SimpleParser类里面的泛型决定的
                    //上传成功，处理相关逻辑
                }, throwable -> {
                    //上传失败，处理相关逻辑
                });
        addDisposable(disposable); //处理RxJava生命周期
    }


    //处理RxJava生命周期
    private void addDisposable(@NonNull Disposable d) {
        if (mCompositeDisposable == null) {
            mCompositeDisposable = new CompositeDisposable();
        }
        mCompositeDisposable.add(d);
    }

    private void clearDisposable() {
        if (mCompositeDisposable != null) {
            mCompositeDisposable.clear();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        clearDisposable();
    }
}
