package com.example.httpsender;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import java.io.File;

import androidx.appcompat.app.AppCompatActivity;
import httpsender.HttpSender;
import httpsender.wrapper.entity.Progress;
import httpsender.wrapper.param.Param;
import httpsender.wrapper.parse.DownloadParser;
import httpsender.wrapper.parse.SimpleParser;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.functions.Predicate;
import io.reactivex.plugins.RxJavaPlugins;

public class MainActivity extends AppCompatActivity {
    public static final String url  = "http://update.9158.com/miaolive/Miaolive.apk";
    public static final String url1 = "http://update.9158.com/miaolive/Miaolive.apk?";
    public static final String url2 = "http://update.9158.com/miaolive/Miaolive.apk?version=100";
    Disposable subscribe;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        String url = "http://www.baidu.com";
        Param param = Param.postForm(url)  //FormParam 表明发送表单形式的Post请求,后续会继续介绍
                .add("versionCode", 100)
                .add("versionName", "1.0.0")
                .addHeader("deviceType", "android")
                .add("file", new File("xxx/1.png"))//new 一个File对象并传入文件路径即可上传文件
                .add("file1", new File("xxx/2.png"));
        Disposable disposable = HttpSender
                .from(param, new SimpleParser<Update>() {}) //注意:这里使用from操作符
                .observeOn(AndroidSchedulers.mainThread())  //主线程回调
                .subscribe(new Consumer<Update>() {
                    @Override
                    public void accept(Update update) throws Exception {
                        //这里拿到Http执行结果，默认为String类
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        //出现错误
                    }
                });

    }

    //HttpSender初始化
    private void HttpSenderInit() {
//        HttpSender.init(new OkHttpClient());//自定义OkHttpClient对象
        RxJavaPlugins.setErrorHandler(throwable -> {
        });
        HttpSender.setOnParamAssembly(p -> {
            //这里添加公共参数
            return p;
        });
    }

    public void onClick(View view) {
        sendGet();
    }

    private void sendGet() {
        String url = "http://ip.taobao.com/service/getIpInfo.php";
        Param param = Param.postForm(url)
                .add("ip", "63.223.108.42")
                .addHeader("accept", "*/*")
                .addHeader("connection", "Keep-Alive")
                .addHeader("user-agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
        Disposable subscribe = HttpSender.from(param, new SimpleParser<Response<String>>() {})
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(s -> {
                    Log.e("LJX", "accept s=" + s.getData());
                }, throwable -> {
                    Log.e("LJX", "accept throwable=" + throwable.getMessage());
                });
    }

    //下载文件，不带进度
    private void download() {
        String url = "http://update.9158.com/miaolive/Miaolive.apk";
        Param param = Param.get(url); //这里可添加参数及请求头信息
        //文件存储路径
        String destPath = getExternalCacheDir() + "/" + System.currentTimeMillis() + ".apk";
        Disposable disposable = HttpSender
                .from(param, new DownloadParser(destPath)) //注意这里使用DownloadParser解析器，并传入本地路径
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(s -> {
                    //下载成功，处理相关逻辑
                }, throwable -> {
                    //下载失败，处理相关逻辑
                });
    }

    //下载文件，带进度
    private void downloadAndProgress() {
        String url = "http://update.9158.com/miaolive/Miaolive.apk";
        //文件存储路径
        String destPath = getExternalCacheDir() + "/" + System.currentTimeMillis() + ".apk";
        Param param = Param.get(url); //这里可添加参数及请求头信息
        Disposable disposable = HttpSender
                .download(param, destPath) //注意这里使用download操作符
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(progress -> {
                    //下载进度更新,0-100，仅在进度有更新时才会回调
                    int currentProgress = progress.getProgress(); //当前进度 0-100
                    long currentSize = progress.getCurrentSize(); //当前已下载的字节大小
                    long totalSize = progress.getTotalSize();     //要下载的总字节大小
                })
                .filter(Progress::isCompleted)//下载完成，才继续往下走
                .map(Progress::getResult) //到这，说明下载完成，返回下载目标路径
                .subscribe(s -> {
                    //下载完成，处理相关逻辑
                }, throwable -> {
                    //下载失败，处理相关逻辑
                });
    }

    //上传文件，带进度
    private void uploadAndProgress() {
        String url = "http://www.baidu.com";
        Param param = Param.postForm(url)  //FormParam 表明发送表单形式的Post请求,后续会继续介绍
                .add("versionCode", 100)
                .add("versionName", "1.0.0")
                .addHeader("deviceType", "android")
                .add("file", new File("xxx/1.png"))//new 一个File对象并传入文件路径即可上传文件
                .add("file1", new File("xxx/2.png"));
        Disposable disposable = HttpSender
                //注意这里使用upload操作符
                .upload(param, new SimpleParser<Update>() {})
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(new Consumer<Progress<Update>>() {
                    @Override
                    public void accept(Progress<Update> progress) throws Exception {
                        //上传进度更新,0-100，仅在进度有更新时才会回调
                        int currentProgress = progress.getProgress(); //当前进度 0-100
                        long currentSize = progress.getCurrentSize(); //当前已上传的字节大小
                        long totalSize = progress.getTotalSize();     //要上传的总字节大小
                    }
                })
                .filter(new Predicate<Progress<Update>>() {
                    @Override
                    public boolean test(Progress<Update> progress) throws Exception {
                        //过滤事件，上传完成，才继续往下走
                        return progress.isCompleted();
                    }
                })
                .map(new Function<Progress<Update>, Update>() {
                    @Override
                    public Update apply(Progress<Update> progress) throws Exception {
                        //返回Http执行结果
                        return progress.getResult();
                    }
                })
                .subscribe(new Consumer<Update>() {
                    @Override
                    public void accept(Update update) throws Exception {
                        //上传成功
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        //上传失败
                    }
                });
    }


    //上传文件，带进度
    private void uploadAndProgress1() {
        String url = "http://www.baidu.com";
        Param param = Param.postForm(url)  //FormParam 表明发送表单形式的Post请求,后续会继续介绍
                .add("versionCode", 100)
                .add("versionName", "1.0.0")
                .addHeader("deviceType", "android")
                .add("file", new File("xxx/1.png"))//new 一个File对象并传入文件路径即可上传文件
                .add("file1", new File("xxx/2.png"));
        //过滤事件，上传完成，才继续往下走
        //返回Http执行结果
        Disposable disposable = HttpSender
                //注意这里使用upload操作符
                .upload(param, new SimpleParser<Update>() {})
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(progress -> {
                    //上传进度更新,0-100，仅在进度有更新时才会回调
                    int currentProgress = progress.getProgress(); //当前进度 0-100
                    long currentSize = progress.getCurrentSize(); //当前已上传的字节大小
                    long totalSize = progress.getTotalSize();     //要上传的总字节大小
                })
                .filter(Progress::isCompleted) //过滤事件，上传完成，才继续往下走
                .map(Progress::getResult)  //返回Http执行结果
                .subscribe(update -> {
                    //上传成功
                }, throwable -> {
                    //上传失败
                });
    }

}
