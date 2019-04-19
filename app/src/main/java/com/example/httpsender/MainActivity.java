package com.example.httpsender;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.rxjava.rxlife.RxLife;

import java.io.File;

import httpsender.HttpSender;
import httpsender.wrapper.entity.Progress;
import httpsender.wrapper.param.Param;
import httpsender.wrapper.param.Params;
import httpsender.wrapper.parse.SimpleParser;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }


    public void sendGet(View view) {
        sendGet();
    }

    //断点续传下载文件
    public void download(View view) {
        download();
    }

    //发送Get请求
    private void sendGet() {
        String url = "http://ip.taobao.com/service/getIpInfo.php";
        Param param = Param.get(url) //这里get,代表Get请求
                .setAssemblyEnabled(false) //设置是否添加公共参数，默认为true
                .add("ip", "63.223.108.42")//添加参数
                .addHeader("accept", "*/*") //添加请求头
                .addHeader("connection", "Keep-Alive")
                .addHeader("user-agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
        Disposable disposable = HttpSender
                .from(param, new DataParser<Address>() {}) //from操作符，是异步操作
                .observeOn(AndroidSchedulers.mainThread()) //主线程回调
                .as(RxLife.as(this)) //加入感知生命周期的观察者
                .subscribe(address -> {
                    //accept方法参数类型由上面DataParser传入的泛型类型决定
                    //走到这里说明Http请求成功，并且数据正确
                }, throwable -> {
                    //Http请求出现异常，有可能是网络异常，数据异常等
                });
    }

    //使用注解处理器生成的Params类,发送Get请求
    private void sendGetByParams() {
        String url = "service/getIpInfo.php";
        Disposable disposable = Params.get(url) //这里get,代表Get请求
                .setDomainTotaobaoIfAbsent()
                .setAssemblyEnabled(false) //设置是否添加公共参数，默认为true
                .add("ip", "63.223.108.42")//添加参数
                .addHeader("accept", "*/*") //添加请求头
                .addHeader("connection", "Keep-Alive")
                .addHeader("user-agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)")
                .fromDataParser(Address.class) //from操作符，是异步操作
                .as(RxLife.asOnMain(this)) //加入感知生命周期的观察者
                .subscribe(address -> {
                    //accept方法参数类型由上面DataParser传入的泛型类型决定
                    //走到这里说明Http请求成功，并且数据正确
                }, throwable -> {
                    //Http请求出现异常，有可能是网络异常，数据异常等
                });
    }

    //使用注解处理器生成的Params类,发送Post请求
    private void sendPostByParams() {
        String url = "/service/getIpInfo.php";
        Disposable disposable = Params.postForm(url) //这里get,代表Get请求
                .setDomainTotaobaoIfAbsent()
                .add("ip", "63.223.108.42")//添加参数
                .addHeader("accept", "*/*") //添加请求头
                .addHeader("connection", "Keep-Alive")
                .addHeader("user-agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)")
                .fromDataParser(Address.class) //from操作符，是异步操作
                .as(RxLife.asOnMain(this)) //加入感知生命周期的观察者
                .subscribe(address -> {
                    //accept方法参数类型由上面DataParser传入的泛型类型决定
                    //走到这里说明Http请求成功，并且数据正确
                }, throwable -> {
                    //Http请求出现异常，有可能是网络异常，数据异常等
                });
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
                .as(RxLife.as(this)) //加入感知生命周期的观察者
                .subscribe(address -> {
                    //accept方法参数类型由上面DataParser传入的泛型类型决定
                    //走到这里说明Http请求成功，并且数据正确
                }, throwable -> {
                    //Http请求出现异常，有可能是网络异常，数据异常等
                });
    }

    //断点续传，下载文件，带进度
    private void download() {
        String url = "http://update.9158.com/miaolive/Miaolive.apk";
        String destPath = getExternalCacheDir() + "/" + "Miaobo.apk";
        File file = new File(destPath);
        long length = file.length();
        Disposable disposable = Params.get(url)
                //如果文件存在,则添加 RANGE 头信息 ，以支持断点下载
                .addHeader("RANGE", "bytes=" + length + "-", length > 0)
                .download(destPath)
                .map(progress -> {
                    if (length > 0) {//增加上次已经下载好的字节数
                        progress.addCurrentSize(length);
                        progress.addTotalSize(length);
                        progress.updateProgress();
                    }
                    return progress;
                })
                .observeOn(AndroidSchedulers.mainThread()) //主线程回调
                .doOnNext(progress -> {
                    //下载进度回调,0-100，仅在进度有更新时才会回调
                    int currentProgress = progress.getProgress(); //当前进度 0-100
                    long currentSize = progress.getCurrentSize(); //当前已下载的字节大小
                    long totalSize = progress.getTotalSize();     //要下载的总字节大小
                })
                .filter(Progress::isCompleted)//过滤事件，下载完成，才继续往下走
                .map(Progress::getResult) //到这，说明下载完成，拿到Http返回结果并继续往下走
                .as(RxLife.as(this)) //加入感知生命周期的观察者
                .subscribe(s -> { //s为String类型
                    //下载成功，处理相关逻辑
                }, throwable -> {
                    //下载失败，处理相关逻辑
                });
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
                .as(RxLife.as(this)) //加入感知生命周期的观察者
                .subscribe(s -> { //s为String类型，由SimpleParser类里面的泛型决定的
                    //上传成功，处理相关逻辑
                }, throwable -> {
                    //上传失败，处理相关逻辑
                });
    }
}
