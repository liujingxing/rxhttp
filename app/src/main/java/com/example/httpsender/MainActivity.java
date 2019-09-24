package com.example.httpsender;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.example.httpsender.databinding.MainActivityBinding;
import com.example.httpsender.entity.Article;
import com.google.gson.Gson;
import com.rxjava.rxlife.RxLife;

import java.io.File;

import io.reactivex.android.schedulers.AndroidSchedulers;
import rxhttp.wrapper.param.RxHttp;

public class MainActivity extends AppCompatActivity {

    private MainActivityBinding mBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.main_activity);
    }


    public void bitmap(View view) {
        String imageUrl = "http://img2.shelinkme.cn/d3/photos/0/017/022/755_org.jpg@!normal_400_400?1558517697888";
        RxHttp.get(imageUrl) //Get请求
            .asBitmap()  //这里返回Observable<Response> 对象
            .as(RxLife.asOnMain(this))  //感知生命周期，并在主线程回调
            .subscribe(bitmap -> {
                mBinding.tvResult.setBackground(new BitmapDrawable(bitmap));
                //成功回调
            }, (OnError) error -> {
                mBinding.tvResult.setText(error.getErrorMsg());
                //失败回调
                error.show("图片加载失败,请稍后再试!");
            });
    }

    //发送Get请求，获取文章列表
    public void sendGet(View view) {
        RxHttp.get("/article/list/0/json")
            .asResponsePageList(Article.class)
            .as(RxLife.asOnMain(this))  //感知生命周期，并在主线程回调
            .subscribe(pageList -> {
                mBinding.tvResult.setText(new Gson().toJson(pageList));
                //成功回调
            }, (OnError) error -> {
                mBinding.tvResult.setText(error.getErrorMsg());
                //失败回调
                error.show("发送失败,请稍后再试!");
            });
    }

    //发送Post请求,根据关键字查询文章
    public void sendPost(View view) {
        RxHttp.postForm("/article/query/0/json")
            .add("k", "性能优化")
            .asResponsePageList(Article.class)
            .as(RxLife.asOnMain(this))  //感知生命周期，并在主线程回调
            .subscribe(pageList -> {
                mBinding.tvResult.setText(new Gson().toJson(pageList));
                //成功回调
            }, (OnError) error -> {
                mBinding.tvResult.setText(error.getErrorMsg());
                //失败回调
                error.show("发送失败,请稍后再试!");
            });
    }

    //文件下载，不带进度
    public void download(View view) {
        String destPath = getExternalCacheDir() + "/" + System.currentTimeMillis() + ".apk";
        RxHttp.get("/miaolive/Miaolive.apk")
            .setDomainToUpdateIfAbsent() //使用指定的域名
            .asDownload(destPath)
            .as(RxLife.asOnMain(this))  //感知生命周期，并在主线程回调
            .subscribe(s -> {
                //下载成功,回调文件下载路径
            }, (OnError) error -> {
                //下载失败
                error.show("下载失败,请稍后再试!");
            });
    }

    //文件下载，带进度
    public void downloadAndProgress(View view) {
        //文件存储路径
        String destPath = getExternalCacheDir() + "/" + System.currentTimeMillis() + ".apk";
        RxHttp.get("/miaolive/Miaolive.apk")
            .setDomainToUpdateIfAbsent()//使用指定的域名
            .asDownload(destPath, progress -> {
                //下载进度回调,0-100，仅在进度有更新时才会回调，最多回调101次，最后一次回调文件存储路径
                int currentProgress = progress.getProgress(); //当前进度 0-100
                long currentSize = progress.getCurrentSize(); //当前已下载的字节大小
                long totalSize = progress.getTotalSize();     //要下载的总字节大小
                mBinding.tvResult.append("\n" + progress.toString());
            }, AndroidSchedulers.mainThread()) //指定回调(进度/成功/失败)线程,不指定,默认在请求所在线程回调
            .as(RxLife.as(this)) //感知生命周期
            .subscribe(s -> {
                //下载完成，处理相关逻辑
                mBinding.tvResult.append("\n下载成功 : " + s);
            }, (OnError) error -> {
                mBinding.tvResult.append("\n" + error.getErrorMsg());
                //下载失败，处理相关逻辑
                error.show("下载失败,请稍后再试!");
            });
    }

    //断点下载
    public void breakpointDownload(View view) {
        String destPath = getExternalCacheDir() + "/" + "Miaobo.apk";
        long length = new File(destPath).length();
        RxHttp.get("/miaolive/Miaolive.apk")
            .setDomainToUpdateIfAbsent() //使用指定的域名
            .setRangeHeader(length)  //设置开始下载位置，结束位置默认为文件末尾
            .asDownload(destPath) //注意这里使用DownloadParser解析器，并传入本地路径
            .as(RxLife.asOnMain(this))  //感知生命周期，并在主线程回调
            .subscribe(s -> {
                //下载成功,回调文件下载路径
            }, (OnError) error -> {
                //下载失败
                error.show("下载失败,请稍后再试!");
            });
    }

    //断点下载，带进度
    public void breakpointDownloadAndProgress(View view) {
        String destPath = getExternalCacheDir() + "/" + "Miaobo.apk";
        long length = new File(destPath).length();
        RxHttp.get("/miaolive/Miaolive.apk")
            .setDomainToUpdateIfAbsent()//使用指定的域名
            .setRangeHeader(length)  //设置开始下载位置，结束位置默认为文件末尾
            .asDownload(destPath, length, progress -> { //如果需要衔接上次的下载进度，则需要传入上次已下载的字节数length
                //下载进度回调,0-100，仅在进度有更新时才会回调
                int currentProgress = progress.getProgress(); //当前进度 0-100
                long currentSize = progress.getCurrentSize(); //当前已下载的字节大小
                long totalSize = progress.getTotalSize();     //要下载的总字节大小
                mBinding.tvResult.append("\n" + progress.toString());
            }, AndroidSchedulers.mainThread()) //指定回调(进度/成功/失败)线程,不指定,默认在请求所在线程回调
            .as(RxLife.as(this))              //加入感知生命周期的观察者
            .subscribe(s -> {
                //下载成功
                mBinding.tvResult.append("\n下载成功 : " + s);
            }, (OnError) error -> {
                //下载失败
                mBinding.tvResult.append("\n" + error.getErrorMsg());
                error.show("下载失败,请稍后再试!");
            });
    }


    //文件上传，不带进度
    public void upload(View v) {
        RxHttp.postForm("http://t.xinhuo.com/index.php/Api/Pic/uploadPic")
            .addFile("uploaded_file", new File(Environment.getExternalStorageDirectory(), "1.jpg"))
            .asString() //from操作符，是异步操作
            .as(RxLife.asOnMain(this))  //感知生命周期，并在主线程回调
            .subscribe(s -> {
                mBinding.tvResult.append("\n");
                mBinding.tvResult.append(s);
                //成功回调
            }, (OnError) error -> {
                mBinding.tvResult.append("\n");
                mBinding.tvResult.append(error.getErrorMsg());
                //失败回调
                error.show("上传失败,请稍后再试!");
            });
    }

    //上传文件，带进度
    public void uploadAndProgress(View v) {
        RxHttp.postForm("http://t.xinhuo.com/index.php/Api/Pic/uploadPic")
            .addFile("uploaded_file", new File(Environment.getExternalStorageDirectory(), "1.jpg"))
            .asUpload(progress -> {
                //上传进度回调,0-100，仅在进度有更新时才会回调
                int currentProgress = progress.getProgress(); //当前进度 0-100
                long currentSize = progress.getCurrentSize(); //当前已上传的字节大小
                long totalSize = progress.getTotalSize();     //要上传的总字节大小
                mBinding.tvResult.append("\n" + progress.toString());
            }, AndroidSchedulers.mainThread()) //指定回调(进度/成功/失败)线程,不指定,默认在请求所在线程回调
            .as(RxLife.as(this))               //加入感知生命周期的观察者
            .subscribe(s -> {
                //上传成功
                mBinding.tvResult.append("\n上传成功 : " + s);
            }, (OnError) error -> {
                //上传失败
                mBinding.tvResult.append("\n" + error.getErrorMsg());
                error.show("上传失败,请稍后再试!");
            });
    }

    //多任务下载
    public void multitaskDownload(View view) {
        startActivity(new Intent(this, DownloadMultiActivity.class));
    }

    public void clearLog(View view) {
        mBinding.tvResult.setText("");
        mBinding.tvResult.setBackgroundColor(Color.TRANSPARENT);
    }
}
