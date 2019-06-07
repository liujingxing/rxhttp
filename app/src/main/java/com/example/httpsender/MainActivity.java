package com.example.httpsender;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;

import com.rxjava.rxlife.RxLife;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import okhttp3.Response;
import rxhttp.wrapper.entity.Progress;
import rxhttp.wrapper.param.RxHttp;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }


    public void bitmap(View view) {
        String imageUrl = "http://img2.shelinkme.cn/d3/photos/0/017/022/755_org.jpg@!normal_400_400?1558517697888";
        RxHttp.get(imageUrl) //Get请求
            .asBitmap()  //这里返回Observable<Response> 对象
            .as(RxLife.asOnMain(this))  //感知生命周期，并在主线程回调
            .subscribe(s -> {
                ImageView ivHead = findViewById(R.id.iv_head);
                ivHead.setImageBitmap(s);
                //成功回调
            }, (OnError) throwable -> {
                //失败回调
            });
    }

    //发送Get请求
    public void sendGet(View view) {
        RxHttp.get("/service/getIpInfo.php") //Get请求
            .add("ip", "63.223.108.42") //添加参数
            .addHeader("accept", "*/*") //添加请求头
            .addHeader("connection", "Keep-Alive")
            .addHeader("user-agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)")
            .asString()  //这里返回Observable<Response> 对象
            .as(RxLife.asOnMain(this))  //感知生命周期，并在主线程回调
            .subscribe(s -> {
                //成功回调
            }, (OnError) throwable -> {
                //失败回调
            });
    }

    //发送Post请求
    private void sendPost() {
        RxHttp.postForm("/service/getIpInfo.php")
            .add("ip", "63.223.108.42")//添加参数
            .addHeader("accept", "*/*") //添加请求头
            .addHeader("connection", "Keep-Alive")
            .addHeader("user-agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)")
            .asObject(Response.class)  //这里返回Observable<Response>对象
            .as(RxLife.asOnMain(this))  //感知生命周期，并在主线程回调
            .subscribe(response -> {
                //成功回调
            }, (OnError) throwable -> {
                //失败回调
            });
    }

    //文件下载，不带进度
    public void download(View view) {
        String destPath = getExternalCacheDir() + "/" + System.currentTimeMillis() + ".apk";
        RxHttp.get("/miaolive/Miaolive.apk")
            .setDomainToUpdateIfAbsent() //使用指定的域名
            .download(destPath) //注意这里使用DownloadParser解析器，并传入本地路径
            .as(RxLife.asOnMain(this))  //感知生命周期，并在主线程回调
            .subscribe(s -> {
                //下载成功,回调文件下载路径
            }, (OnError) throwable -> {
                //下载失败
            });
    }

    //文件下载，带进度
    public void downloadAndProgress(View view) {
        //文件存储路径
        String destPath = getExternalCacheDir() + "/" + System.currentTimeMillis() + ".apk";
        RxHttp.get("/miaolive/Miaolive.apk")
            .setDomainToUpdateIfAbsent()//使用指定的域名
            .downloadProgress(destPath) //注:如果需要监听下载进度，使用downloadProgress操作符
            .observeOn(AndroidSchedulers.mainThread())
            .doOnNext(progress -> {
                //下载进度回调,0-100，仅在进度有更新时才会回调，最多回调101次，最后一次回调文件存储路径
                int currentProgress = progress.getProgress(); //当前进度 0-100
                long currentSize = progress.getCurrentSize(); //当前已下载的字节大小
                long totalSize = progress.getTotalSize();     //要下载的总字节大小
                String filePath = progress.getResult(); //文件存储路径，最后一次回调才有内容
            })
            .filter(Progress::isCompleted)//下载完成，才继续往下走
            .map(Progress::getResult) //到这，说明下载完成，返回下载目标路径
            .as(RxLife.as(this)) //感知生命周期
            .subscribe(s -> {//s为String类型，这里为文件存储路径
                //下载完成，处理相关逻辑
            }, (OnError) throwable -> {
                //下载失败，处理相关逻辑
            });
    }

    //断点下载
    public void breakpointDownload(View view) {
        String destPath = getExternalCacheDir() + "/" + "Miaobo.apk";
        long length = new File(destPath).length();
        RxHttp.get("/miaolive/Miaolive.apk")
            .setDomainToUpdateIfAbsent() //使用指定的域名
            .setRangeHeader(length)  //设置开始下载位置，结束位置默认为文件末尾
            .download(destPath) //注意这里使用DownloadParser解析器，并传入本地路径
            .as(RxLife.asOnMain(this))  //感知生命周期，并在主线程回调
            .subscribe(s -> {
                //下载成功,回调文件下载路径
            }, (OnError) throwable -> {
                //下载失败
            });
    }

    //断点下载，带进度
    public void breakpointDownloadAndProgress(View view) {
        String destPath = getExternalCacheDir() + "/" + "Miaobo.apk";
        long length = new File(destPath).length();
        RxHttp.get("/miaolive/Miaolive.apk")
            .setDomainToUpdateIfAbsent()//使用指定的域名
            .setRangeHeader(length)  //设置开始下载位置，结束位置默认为文件末尾
            .downloadProgress(destPath, length)  //如果需要衔接上次的下载进度，则需要传入上次已下载的字节数
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
            }, (OnError) throwable -> {
                //下载失败，处理相关逻辑
            });
    }


    //文件上传，不带进度
    private void upload() {
        RxHttp.postForm("http://...") //发送Form表单形式的Post请求
            .add("file1", new File("xxx/1.png"))
            .add("file2", new File("xxx/2.png"))
            .asString() //from操作符，是异步操作
            .as(RxLife.asOnMain(this))  //感知生命周期，并在主线程回调
            .subscribe(s -> {
                //成功回调
            }, (OnError) throwable -> {
                //失败回调
            });
    }

    //上传文件，带进度
    private void uploadAndProgress() {
        String url = "http://www.......";
        RxHttp.postForm(url) //发送Form表单形式的Post请求
            .add("file1", new File("xxx/1.png"))
            .add("file2", new File("xxx/2.png"))
            .add("key1", "value1")//添加参数，非必须
            .add("key2", "value2")//添加参数，非必须
            .addHeader("versionCode", "100")//添加请求头,非必须
            .uploadProgress() //注:如果需要监听上传进度，使用upload操作符
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
            }, (OnError) throwable -> {
                //上传失败，处理相关逻辑
            });
    }

    //多任务下砸
    public void multitaskDownload(View view) {
        List<Observable<String>> downList = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            String destPath = getExternalCacheDir() + "/" + i + ".apk";
            Observable<String> down = RxHttp.get("/miaolive/Miaolive.apk")
                .setDomainToUpdateIfAbsent() //使用指定的域名
                .downloadProgress(destPath)//注意这里使用DownloadParser解析器，并传入本地路径
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(stringProgress -> {
                    //单个下载任务进度回调
                })
                .filter(Progress::isCompleted)
                .map(Progress::getResult);
            downList.add(down);
        }

        //开始多任务下载
        Observable.merge(downList)
            .as(RxLife.as(this))
            .subscribe(s -> {
                //单个任务下载完成
            }, throwable -> {
                //下载出错
            }, () -> {
                //所有任务下载完成
            });
    }
}
