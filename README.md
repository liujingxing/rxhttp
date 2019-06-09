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

详细介绍：https://juejin.im/post/5cbd267fe51d456e2b15f623

Gradle引用方法

```java
    dependencies {
       implementation 'com.rxjava.rxhttp:rxhttp:1.0.8'
       //注解处理器，生成RxHttp类，即可一条链发送请求
       annotationProcessor 'com.rxjava.rxhttp:rxhttp-compiler:1.0.8'
       //管理RxJava及生命周期，Activity/Fragment 销毁，自动关闭未完成的请求
       implementation 'com.rxjava.rxlife:rxlife:1.0.6'
    }
```

#### 注：

1、kotlin用户，需要使用kapt代替annotationProcessor，如下：

```java
    dependencies {
       implementation 'com.rxjava.rxhttp:rxhttp:1.0.8'
       //注解处理器，生成RxHttp类，即可一条链发送请求
       kapt 'com.rxjava.rxhttp:rxhttp-compiler:1.0.8'
       //管理RxJava及生命周期，Activity/Fragment 销毁，自动关闭未完成的请求
       implementation 'com.rxjava.rxlife:rxlife:1.0.6'
    }
```

2、RxHttp 要求项目使用Java 8，请在 app 的 build.gradle 添加以下代码

```java
    compileOptions {
            sourceCompatibility JavaVersion.VERSION_1_8
            targetCompatibility JavaVersion.VERSION_1_8
    }
```

Usage

首先，我们需要通过注解生成RxHttp类

```java
public class Url {
    @DefaultDomain() //设置为默认域名
    public static String baseUrl = "http://ip.taobao.com/";
}
```
此时rebuild一下项目，就能看到RxHttp类了

### 添加公共参数/头部及重新设置url

```java
//建议在Application里设置
HttpSender.setOnParamAssembly(new Function() {
    @Override
    public Param apply(Param p) {
        if (p instanceof GetRequest) {//根据不同请求添加不同参数
        } else if (p instanceof PostRequest) {
        } else if (p instanceof PutRequest) {
        } else if (p instanceof DeleteRequest) {
        }
        //可以通过 p.getSimpleUrl() 拿到url更改后，重新设置
        //p.setUrl("");
        return p.add("versionName", "1.0.0")//添加公共参数
                .addHeader("deviceType", "android"); //添加公共请求头
    }
});
```
### 请求三部曲
```java
  RxHttp.get("http://...")              //第一步，确定请求方式
        .asString()                     //第二步，使用asXXX系列方法确定返回类型
        .subscribe(s -> {               //第三部, 订阅观察者
            //成功回调
        }, throwable -> {
            //失败回调
        });
```

### api介绍
```java
  RxHttp.postForm("/service/getIpInfo.php")       //发送Form表单形式的Post请求
        .setDomainToUpdate9158IfAbsent()  //手动设置域名，不设置会添加默认域名，此方法是通过@Domain注解生成的
        .tag("RxHttp.get")          //为单个请求设置tag
        .setUrl("http://...")       //重新设置url
        .setJsonParams("{"versionName":"1.0.0"}") //设置Json字符串参数，非Json形式的请求调用此方法没有任何效果
        .setAssemblyEnabled(false)  //设置是否添加公共参数，默认为true
        .cacheControl(CacheControl.FORCE_NETWORK)  //缓存控制
        .setParam(Param.postForm("http://..."))    //重新设置一个Param对象
        .add(new HashMap<>())   //通过Map添加参数
        .add("int", 1)          //添加int类型参数
        .add("float", 1.28838F) //添加float类型参数
        .add("double", 1.28838) //添加double类型参数
        .add("key1", "value1")  //添加String类型参数
        .add("key2", "value2", false) //根据最后的boolean字段判断是否添加参数
        .add("file1", new File("xxx/1.png"))            //添加文件对象
        .addHeader("headerKey1", "headerValue1")        //添加头部信息
        .addHeader("headerKey2", "headerValue2", false)//根据最后的boolean字段判断是否添加头部信息
        .asString()            //使用asXXX系列方法确定返回类型,此时返回Observable对象
        //感知生命周期，并在主线程回调，当Activity/Fragment销毁时，自动关闭未完成的请求
        .as(RxLife.asOnMain(this))
        .subscribe(s -> {    //订阅观察者
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
        .asObject(Response.class)  //这里返回Observable<Response> 对象
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
        .asObject(Response.class)  //这里返回Observable<Response>对象
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
        .asString() //from操作符，是异步操作
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
        .asDownload(destPath) //传入本地路径
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
        .asDownloadProgress(destPath) //注:如果需要监听下载进度，使用downloadProgress操作符
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
        .asUploadProgress() //注:如果需要监听上传进度，使用uploadProgress操作符
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

### 断点下载、带进度回调
```java
//断点下载，带进度
public void breakpointDownloadAndProgress() {
    String destPath = getExternalCacheDir() + "/" + "Miaobo.apk";
    File file = new File(destPath);
    long length = file.length();
    RxHttp.get("http://update.9158.com/miaolive/Miaolive.apk")
            .setRangeHeader(length)  //设置开始下载位置，结束位置默认为文件末尾
            .asDownloadProgress(destPath, length)  //如果需要衔接上次的下载进度，则需要传入上次已下载的字节数
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
```

### 多任务下载
```java
List<Observable<String>> downList = new ArrayList<>();
for (int i = 0; i < 3; i++) {
    String destPath = getExternalCacheDir() + "/" + i + ".apk";
    String url = "http://update.9158.com/miaolive/Miaolive.apk"
    Observable<String> down = RxHttp.get(url)
            .asDownloadProgress(destPath)//注意这里使用DownloadParser解析器，并传入本地路径
            .observeOn(AndroidSchedulers.mainThread())
            .doOnNext(progress -> {
                //单个下载任务进度回调
            })
            .filter(Progress::isCompleted)//过滤事件，下载完成，才继续往下走
            .map(Progress::getResult);//到这，说明下载完成，拿到Http返回结果并继续往下走
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

```

RxHttp&RxLife 交流群：378530627

### 更新日志

1.0.8

 - 增加Http请求连接失败时的异常信息打印

 — 修复1.0.7版本中，一处由注解的生成的方法错误问题

1.0.7

 - RxHttp类增加一系列'subscribeOnXXX'方法，通过该系列方法，指定请求在某个线程执行

 - 增加BitmapParser解析器，通过该解析器，可直接拿到Bitmap对象，详情查看asBitmap方法

 - RxHttp类增加一系列'asXXX'方法，替代'fromXXX'/'downloadXXX'/'uploadXXX'方法，被替代的方法标记为过时，将在未来的版本删除

 - 增加HttpStatusCodeException异常类，可在OnError回调中捕获该异常

 - OkHttp 更新至3.14.1，RxJava更新至2.2.8版本，RxAndroid 更新至2.1.1版本

 - HttpSender中一些方法标记为过时，这些方法将在未来的版本中删除，请尽快使用新方法替代

1.0.5

 - 增加一系列'addFile'方法，支持同一个key添加多个文件

 - PostFormParam增加setUploadMaxLength方法，以限制文件上传最大长度

1.0.4

 - RxHttp类增加setRangeHeader、downloadProgress(String,Long)方法，以更好的支持断点下载

1.0.3

 - RxHttp增加 setJsonParams(String) 方法，Json形式的请求直接调用此方法传入Json字符串参数

1.0.2

 - 增加@DefaultDomain注解，通过该注解，可以设置baseUrl;









