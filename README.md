# RxHttp
RxHttp是基于OkHttp的二次封装，并于RxJava做到无缝衔接，一条链就能发送一个完整的请求。
主要功能如下：
 - 支持Get、Post、Put、Delete等任意请求方式，可自定义请求方式
 - 支持Json、DOM等任意数据解析方法，可自定义数据解析器
 - 支持文件下载/上传，及进度的监听，并且支持断点下载
 - 支持在Activity/Fragment的任意生命周期方法，自动关闭未完成的请求
 - 支持添加公共参数/头部信息，且可动态更改baseUrl
 - 支持请求串行和并行



注：RxHttp是通过注解生成的，请使用@DefaultDomain或@Domain注解在baseUrl上，rebuild一下项目，就能看到RxHttp类了。
注解处理器会在编译时检索注解，检索不到，就不会生成RxHttp类。

博客地址：https://juejin.im/post/5cbd267fe51d456e2b15f623

Gradle引用方法

    dependencies {
       implementation 'com.rxjava.rxhttp:rxhttp:1.0.2'
       //注解处理器，生成RxHttp类，即可一条链发送请求
       annotationProcessor 'com.rxjava.rxhttp:rxhttp-compiler:1.0.2'
       //管理RxJava及生命周期，Activity/Fragment 销毁，自动关闭未完成的请求
       implementation 'com.rxjava.rxlife:rxlife:1.0.4'
    }

Usage

```java
RxHttp.get("http://...")                //第一步，确定请求方式
        .fromSimpleParser(String.class) //  第二步，确定解析器
        .subscribe(s -> {               //第三部  订阅观察者
            //成功回调
        }, throwable -> {
            //失败回调
        });
```
```java
RxHttp.postForm("http://...") //传入url,确定请求方式  这里postForm代表发送表单形式的Post请求
        .setUrl("http://")    //重新设置url
        .setAssemblyEnabled(false)  //设置是否添加公共参数，默认为true
        .add("key", "value")  //添加请求参数,可添加n个
        .add("key1", "value1") 
        .add("file1", new File("xxx/1.png")) //添加文件，可添加n个
        .add("file2", new File("xxx/2.png"))
        .addHeader("headerKey", "HeaderValue") //添加头部信息，可添加n个
        .addHeader("headerKey1", "HeaderValue1")
        //使用SimpleParser解析器，将数据解析成指定对象，这里解析成Integer对象
        .fromSimpleParser(Integer.class)   
        //感知Activity/Fragment 生命周期，页面销毁时，自动关闭请求
        .as(RxLife.asOnMain(this)) 
        .subscribe(i -> {  //订阅观察者，开始发送请求  这里i为Integer类型
            //成功回调
        }, throwable -> {
            //失败回调
        });
```

### Get请求
```java
RxHttp.get("http://ip.taobao.com/service/getIpInfo.php") //Get请求
        .add("ip", "63.223.108.42")//添加参数
        .addHeader("accept", "*/*") //添加请求头
        .addHeader("connection", "Keep-Alive")
        .addHeader("user-agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)")
        .fromSimpleParser(Response.class)  //这里返回Observable<Response> 对象
        .as(RxLife.asOnMain(this))  //感知生命周期，并在主线程回调
        .subscribe(response -> {
            //成功回调
        }, throwable -> {
            //失败回调
        });
```
### Post请求
```java
RxHttp.postForm("http://ip.taobao.com/service/getIpInfo.php")
        .add("ip", "63.223.108.42")//添加参数
        .addHeader("accept", "*/*") //添加请求头
        .addHeader("connection", "Keep-Alive")
        .addHeader("user-agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)")
        .fromSimpleParser(Response.class)  //这里返回Observable<Response>对象
        .as(RxLife.asOnMain(this))  //感知生命周期，并在主线程回调
        .subscribe(response -> {
            //成功回调
        }, throwable -> {
            //失败回调
        });
```

可以发现，在这里Get跟Post请求代码几乎一样，只有第一行代码不同。
### 文件上传
```java
RxHttp.postForm("http://...") //发送Form表单形式的Post请求
        .add("file1", new File("xxx/1.png"))
        .add("file2", new File("xxx/2.png"))
        .fromSimpleParser(String.class) //from操作符，是异步操作
        .as(RxLife.asOnMain(this))  //感知生命周期，并在主线程回调
        .subscribe(s -> { 
            //成功回调
        }, throwable -> {
            //失败回调
        });
```
### 文件下载
```java
//文件存储路径
String destPath = getExternalCacheDir() + "/" + System.currentTimeMillis() + ".apk";
RxHttp.get("http://update.9158.com/miaolive/Miaolive.apk")
        .download(destPath) //传入本地路径
        .as(RxLife.asOnMain(this))  //感知生命周期，并在主线程回调
        .subscribe(s -> {
            //下载成功,回调文件下载路径
        }, throwable -> {
            //下载失败
        });
```

### 文件下载进度监听
```java
//文件存储路径
String destPath = getExternalCacheDir() + "/" + System.currentTimeMillis() + ".apk";
RxHttp.get("http://update.9158.com/miaolive/Miaolive.apk")
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
        }, throwable -> {
            //下载失败，处理相关逻辑
        });
```
###  文件上传进度监听
```java
RxHttp.postForm("http://www.......") //发送Form表单形式的Post请求
        .add("file1", new File("xxx/1.png"))
        .add("file2", new File("xxx/2.png"))
        .add("key1", "value1")//添加参数，非必须
        .add("key2", "value2")//添加参数，非必须
        .addHeader("versionCode", "100") //添加请求头,非必须
        .uploadProgress() //注:如果需要监听上传进度，使用uploadProgress操作符
        .observeOn(AndroidSchedulers.mainThread()) //主线程回调
        .doOnNext(progress -> {
            //上传进度回调,0-100，仅在进度有更新时才会回调,最多回调101次，最后一次回调Http执行结果
            int currentProgress = progress.getProgress(); //当前进度 0-100
            long currentSize = progress.getCurrentSize(); //当前已上传的字节大小
            long totalSize = progress.getTotalSize();     //要上传的总字节大小
            String result = progress.getResult(); //Http执行结果，最后一次回调才有内容
        })
        .filter(Progress::isCompleted)//过滤事件，上传完成，才继续往下走
        .map(Progress::getResult) //到这，说明上传完成，拿到Http返回结果并继续往下走
        .as(RxLife.as(this))  //感知生命周期
        .subscribe(s -> { //s为String类型，由SimpleParser类里面的泛型决定的
            //上传成功，处理相关逻辑
        }, throwable -> {
            //上传失败，处理相关逻辑
        });
```



1.0.2

 - 增加@DefaultDomain注解，通过该注解，可以设置baseUrl;









