package com.example.httpsender.fragment;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

import com.example.httpsender.DownloadMultiActivity;
import com.example.httpsender.OnError;
import com.example.httpsender.R;
import com.example.httpsender.databinding.RxjavaFragmentBinding;
import com.example.httpsender.entity.Article;
import com.example.httpsender.entity.Location;
import com.example.httpsender.entity.Name;
import com.example.httpsender.entity.NewsDataXml;
import com.google.gson.Gson;
import com.rxjava.rxlife.RxLife;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import rxhttp.wrapper.param.RxHttp;
import rxhttp.wrapper.param.RxSimpleHttp;

/**
 * 使用RxJava+OkHttp发请求
 * User: ljx
 * Date: 2020/4/24
 * Time: 18:16
 */
public class RxJavaFragment extends Fragment implements OnClickListener {

    private RxjavaFragmentBinding mBinding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.rxjava_fragment, container, false);
        mBinding.setClick(this);
        return mBinding.getRoot();
    }

    @SuppressWarnings("deprecation")
    public void bitmap(View view) {
        String imageUrl = "http://img2.shelinkme.cn/d3/photos/0/017/022/755_org.jpg@!normal_400_400?1558517697888";
        RxHttp.get(imageUrl) //Get请求
            .asBitmap()  //这里返回Observable<Bitmap> 对象
            .to(RxLife.toMain(this))  //感知生命周期，并在主线程回调
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
            .setSimpleClient()
            .asResponsePageList(Article.class)
            .to(RxLife.toMain(this))  //感知生命周期，并在主线程回调
            .subscribe(pageList -> {
                mBinding.tvResult.setText(new Gson().toJson(pageList));
                //成功回调
            }, (OnError) error -> {
                mBinding.tvResult.setText(error.getErrorMsg());
                //失败回调
                error.show("发送失败,请稍后再试!");
            });
    }

    //发送Post表单请求,根据关键字查询文章
    public void sendPostForm(View view) {
        RxHttp.postForm("/article/query/0/json")
            .add("k", "性能优化")
            .asResponsePageList(Article.class)
            .to(RxLife.toMain(this))  //感知生命周期，并在主线程回调
            .subscribe(pageList -> {
                mBinding.tvResult.setText(new Gson().toJson(pageList));
                //成功回调
            }, (OnError) error -> {
                mBinding.tvResult.setText(error.getErrorMsg());
                //失败回调
                error.show("发送失败,请稍后再试!");
            });
    }

    //发送Post Json请求，此接口不通，仅用于调试参数
    public void sendPostJson(View view) {
        //发送以下User对象
        /*
           {
               "name": "张三",
               "sex": 1,
               "height": 180,
               "weight": 70,
               "interest": [
                   "羽毛球",
                   "游泳"
               ],
               "location": {
                   "latitude": 30.7866,
                   "longitude": 120.6788
               },
               "address": {
                   "street": "科技园路.",
                   "city": "江苏苏州",
                   "country": "中国"
               }
           }
         */
        List<String> interestList = new ArrayList<>();//爱好
        interestList.add("羽毛球");
        interestList.add("游泳");
        String address = "{\"street\":\"科技园路.\",\"city\":\"江苏苏州\",\"country\":\"中国\"}";

        RxHttp.postJson("/article/list/0/json")
            .add("name", "张三")
            .add("sex", 1)
            .addAll("{\"height\":180,\"weight\":70}") //通过addAll系列方法添加多个参数
            .add("interest", interestList) //添加数组对象
            .add("location", new Location(120.6788, 30.7866))  //添加位置对象
            .addJsonElement("address", address) //通过字符串添加一个对象
            .asString()
            .to(RxLife.toMain(this))  //感知生命周期，并在主线程回调
            .subscribe(s -> {
                mBinding.tvResult.setText(s);
                //成功回调
            }, (OnError) error -> {
                mBinding.tvResult.setText(error.getErrorMsg());
                //失败回调
                error.show("发送失败,请稍后再试!");
            });
    }


    //发送Post JsonArray请求，此接口不通，仅用于调试参数
    public void sendPostJsonArray(View view) {
        //发送以下Json数组
        /*
           [
               {
                   "name": "张三"
               },
               {
                   "name": "李四"
               },
               {
                   "name": "王五"
               },
               {
                   "name": "赵六"
               },
               {
                   "name": "杨七"
               }
           ]
         */
        List<Name> names = new ArrayList<>();
        names.add(new Name("赵六"));
        names.add(new Name("杨七"));
        RxHttp.postJsonArray("/article/list/0/json")
            .add("name", "张三")
            .add(new Name("李四"))
            .addJsonElement("{\"name\":\"王五\"}")
            .addAll(names)
            .asString()
            .to(RxLife.toMain(this))
            .subscribe(s -> {
                mBinding.tvResult.setText(s);
            }, (OnError) error -> {
                mBinding.tvResult.setText(error.getErrorMsg());
                //失败回调
                error.show("发送失败,请稍后再试!");
            });
    }

    //使用XmlConverter解析数据，此接口返回数据太多，会有点慢
    public void xmlConverter(View view) {
        RxHttp.get("http://webservices.nextbus.com/service/publicXMLFeed?command=routeConfig&a=sf-muni")
            .setXmlConverter()
            .asClass(NewsDataXml.class)
            .to(RxLife.toMain(this))  //感知生命周期，并在主线程回调
            .subscribe(dataXml -> {
                mBinding.tvResult.setText(new Gson().toJson(dataXml));
                //成功回调
            }, (OnError) error -> {
                mBinding.tvResult.setText(error.getErrorMsg());
                //失败回调
                error.show("发送失败,请稍后再试!");
            });
    }

    //使用XmlConverter解析数据
    public void fastJsonConverter(View view) {
        RxHttp.get("/article/list/0/json")
            .setFastJsonConverter()
            .asResponsePageList(Article.class)
            .to(RxLife.toMain(this))  //感知生命周期，并在主线程回调
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
        String destPath = requireContext().getExternalCacheDir() + "/" + System.currentTimeMillis() + ".apk";
        RxSimpleHttp.get("/miaolive/Miaolive.apk")
            .asDownload(destPath)
            .to(RxLife.toMain(this))  //感知生命周期，并在主线程回调
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
        String destPath = requireContext().getExternalCacheDir() + "/" + System.currentTimeMillis() + ".apk";
        RxSimpleHttp.get("/miaolive/Miaolive.apk")
            //第二个参数指定回调(进度/成功/失败)线程,不指定,默认在请求所在线程回调
            .asDownload(destPath, AndroidSchedulers.mainThread(), progress -> {
                //下载进度回调,0-100，仅在进度有更新时才会回调，最多回调101次，最后一次回调文件存储路径
                int currentProgress = progress.getProgress(); //当前进度 0-100
                long currentSize = progress.getCurrentSize(); //当前已下载的字节大小
                long totalSize = progress.getTotalSize();     //要下载的总字节大小
                mBinding.tvResult.append("\n" + progress.toString());
            })
            .to(RxLife.to(this)) //感知生命周期
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
        String destPath = requireContext().getExternalCacheDir() + "/" + "Miaobo.apk";
        long length = new File(destPath).length();
        RxHttp.get("/miaolive/Miaolive.apk")
            .setDomainToUpdateIfAbsent() //使用指定的域名
            .setRangeHeader(length)  //设置开始下载位置，结束位置默认为文件末尾
            .asDownload(destPath) //注意这里使用DownloadParser解析器，并传入本地路径
            .to(RxLife.toMain(this))  //感知生命周期，并在主线程回调
            .subscribe(s -> {
                //下载成功,回调文件下载路径
            }, (OnError) error -> {
                //下载失败
                error.show("下载失败,请稍后再试!");
            });
    }

    //断点下载，带进度
    public void breakpointDownloadAndProgress(View view) {
        String destPath = requireContext().getExternalCacheDir() + "/" + "Miaobo.apk";
        long length = new File(destPath).length();
        RxHttp.get("/miaolive/Miaolive.apk")
            .setDomainToUpdateIfAbsent()//使用指定的域名
            .setRangeHeader(length, -1, true)  //设置开始下载位置，结束位置默认为文件末尾，最后一个参数代表是否需要衔接上次的下载进度
            //指定回调(进度/成功/失败)线程,不指定,默认在请求所在线程回调
            .asDownload(destPath, AndroidSchedulers.mainThread(), progress -> {
                //下载进度回调,0-100，仅在进度有更新时才会回调
                int currentProgress = progress.getProgress(); //当前进度 0-100
                long currentSize = progress.getCurrentSize(); //当前已下载的字节大小
                long totalSize = progress.getTotalSize();     //要下载的总字节大小
                mBinding.tvResult.append("\n" + progress.toString());
            })
            .to(RxLife.to(this))              //加入感知生命周期的观察者
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
            .to(RxLife.toMain(this))  //感知生命周期，并在主线程回调
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
            .upload(progress -> {
                //上传进度回调,0-100，仅在进度有更新时才会回调
                int currentProgress = progress.getProgress(); //当前进度 0-100
                long currentSize = progress.getCurrentSize(); //当前已上传的字节大小
                long totalSize = progress.getTotalSize();     //要上传的总字节大小
                mBinding.tvResult.append("\n" + progress.toString());
            }, AndroidSchedulers.mainThread()) //指定回调(进度/成功/失败)线程,不指定,默认在请求所在线程回调
            .asString()
            .to(RxLife.to(this))               //加入感知生命周期的观察者
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
        startActivity(new Intent(requireContext(), DownloadMultiActivity.class));
    }

    public void clearLog(View view) {
        mBinding.tvResult.setText("");
        mBinding.tvResult.setBackgroundColor(Color.TRANSPARENT);
    }




    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bitmap:
                bitmap(v);
                break;
            case R.id.sendGet:
                sendGet(v);
                break;
            case R.id.sendPostForm:
                sendPostForm(v);
                break;
            case R.id.sendPostJson:
                sendPostJson(v);
                break;
            case R.id.sendPostJsonArray:
                sendPostJsonArray(v);
                break;
            case R.id.xmlConverter:
                xmlConverter(v);
                break;
            case R.id.fastJsonConverter:
                fastJsonConverter(v);
                break;
            case R.id.download:
                download(v);
                break;
            case R.id.downloadAndProgress:
                downloadAndProgress(v);
                break;
            case R.id.breakpointDownload:
                breakpointDownload(v);
                break;
            case R.id.breakpointDownloadAndProgress:
                breakpointDownloadAndProgress(v);
                break;
            case R.id.upload:
                upload(v);
                break;
            case R.id.uploadAndProgress:
                uploadAndProgress(v);
                break;
            case R.id.multitaskDownload:
                multitaskDownload(v);
                break;
            case R.id.bt_clear:
                clearLog(v);
                break;
        }
    }

}
