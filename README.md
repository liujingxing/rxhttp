[ ![Download](https://api.bintray.com/packages/32774707/maven/rxhttp/images/download.svg) ](https://bintray.com/32774707/maven/rxhttp/_latestVersion)

# RxHttp
RxHttp是基于OkHttp的二次封装，并于RxJava做到无缝衔接，一条链就能发送一个完整的请求。
主要功能如下：
 - 支持Get、Post、Put、Delete等任意请求方式，可自定义请求方式
 - 支持Json、DOM等任意数据解析方法，可自定义数据解析器
 - 支持文件下载/上传，及进度的监听，并且支持断点下载
 - 支持在Activity/Fragment的任意生命周期方法，自动关闭未完成的请求
 - 支持添加公共参数/头部信息，且可动态更改baseUrl
 - 支持请求串行和并行

> 注：RxHttp是通过注解生成的，请使用@DefaultDomain或@Domain注解在baseUrl上，rebuild一下项目，就能看到RxHttp类了。
注解处理器会在编译时检索注解，检索不到，就不会生成RxHttp类。

**30秒上手教程：https://juejin.im/post/5cfcbbcbe51d455a694f94df**

**详细介绍：https://juejin.im/post/5cbd267fe51d456e2b15f623**

**自动关闭请求用到的RxLife类，详情请查看[RxLife库](https://github.com/liujingxing/RxLife)**

**RxHttp&RxLife 交流群：378530627**

## Demo演示

![image](https://github.com/liujingxing/RxHttp/blob/master/screen/screen_record.gif)

> 普通文本请求不便于展示，故这里只演示Bitmap加载及多任务断点下载，更多功能，请下载Demo体验

**Gradle引用方法**

```java
dependencies {
   implementation 'com.rxjava.rxhttp:rxhttp:1.1.6'
   annotationProcessor 'com.rxjava.rxhttp:rxhttp-compiler:1.1.6' //注解处理器，生成RxHttp类
   implementation 'com.rxjava.rxlife:rxlife:1.0.9'  //页面销毁，关闭请求，非必须

   // if you use kotlin
   kapt 'com.rxjava.rxhttp:rxhttp-compiler:1.1.6'
}
```

## 注：前方高能预警

**1、RxHttp 要求项目使用Java 8，请在 app 的 build.gradle 添加以下代码**

```java
compileOptions {
    sourceCompatibility JavaVersion.VERSION_1_8
    targetCompatibility JavaVersion.VERSION_1_8
}
```

**2、为RxJava设置全局异常处理，否则有可能造成程序崩溃**

```java
RxJavaPlugins.setErrorHandler(throwable -> {
    /*
      RxJava2的一个重要的设计理念是：不吃掉任何一个异常,即抛出的异常无人处理，便会导致程序崩溃
      这就会导致一个问题，当RxJava2“downStream”取消订阅后，“upStream”仍有可能抛出异常，
      这时由于已经取消订阅，“downStream”无法处理异常，此时的异常无人处理，便会导致程序崩溃
     */
});
```

**3、API兼容**

RxHttp最低要求为API 15，但是由于内部依赖OkHttp 3.14.1版本, 最低要求为API 21。
如果你要的项目要兼容到API 15，请将RxHttp内部的OkHttp剔除，并引入低版本的OkHttp,如下：

```
implementation('com.rxjava.rxhttp:rxhttp:1.1.5') {
    exclude group: "com.squareup.okhttp3"
}
implementation 'com.squareup.okhttp3:okhttp:3.12.3' //此版本最低要求 API 9
```

## 准备工作

首先，我们需要通过注解生成RxHttp类

```java
public class Url {
    @DefaultDomain() //设置为默认域名
    public static String baseUrl = "http://ip.taobao.com/";
}
```
此时rebuild一下项目，就能看到RxHttp类了

## 请求三部曲
```java
RxHttp.get("http://...")            //第一步，确定请求方式
    .asString()                     //第二步，使用asXXX系列方法确定返回类型
    .subscribe(s -> {               //第三部, 订阅观察者
        //成功回调
    }, throwable -> {
        //失败回调
    });
```

## post
```java
RxHttp.postForm("http://...")       //发送表单形式的post请求
    .asString()
    .subscribe(s -> {
        //成功回调
    }, throwable -> {
        //失败回调
    });

RxHttp.postJson("http://...")       //发送Json字符串形式的post请求
    //省略部分代码

```

## 添加参数
```java
RxHttp.postForm("http://...")                //发送表单形式的post请求
    .add("key", "value")                     //添加参数
    .addHeader("headerKey", "headerValue")   //添加请求头
    .addFile("file", new File("xxx/1.png"))  //添加文件
    .asString()
    .subscribe(s -> {
        //成功回调
    }, throwable -> {
        //失败回调
    });
```

## 返回自定义的数据类型
```java
RxHttp.postForm("http://...")     //发送表单形式的post请求
    .asObject(Student.class)      //返回Student对象
    .subscribe(student -> {
        //成功回调
    }, throwable -> {
        //失败回调
    });


RxHttp.postForm("http://...")     //发送表单形式的post请求
    .asList(Student.class)        //返回List<Student>集合
    .subscribe(students -> {
        //成功回调
    }, throwable -> {
        //失败回调
    });

```

## 文件上传
```java
RxHttp.postForm("http://...")                //发送Form表单形式的Post请求
    .addFile("file", new File("xxx/1.png"))  //添加文件
    .asString()
    .subscribe(s -> {
        //成功回调
    }, throwable -> {
        //失败回调
    });
```

## 文件下载

```java
RxHttp.get("http://...")
    .asDownload("sd/xxx/1.apk") //传入本地路径
    .subscribe(s -> {
        //下载成功,回调文件下载路径
    }, throwable -> {
        //下载失败
    });
```

##  文件上传进度监听
```java
RxHttp.postForm("http://...")
    .add("file1", new File("xxx/1.png"))
    .asUpload(progress -> {
        //上传进度回调,0-100，仅在进度有更新时才会回调,最多回调101次，最后一次回调Http执行结果
        int currentProgress = progress.getProgress(); //当前进度 0-100
        long currentSize = progress.getCurrentSize(); //当前已上传的字节大小
        long totalSize = progress.getTotalSize();     //要上传的总字节大小
    }, AndroidSchedulers.mainThread())     //指定回调(进度/成功/失败)线程,不指定,默认在请求所在线程回调
    .subscribe(s -> {             //这里s为String类型,可通过asUpload(Parser,Progress,Scheduler)方法指定返回类型
        //上传成功
    }, throwable -> {
        //上传失败
    });
```

## 文件下载进度监听
```java
RxHttp.get("http://...")
    .asDownload("sd/xxx/1.apk", progress -> {
        //下载进度回调,0-100，仅在进度有更新时才会回调，最多回调101次，最后一次回调文件存储路径
        int currentProgress = progress.getProgress(); //当前进度 0-100
        long currentSize = progress.getCurrentSize(); //当前已下载的字节大小
        long totalSize = progress.getTotalSize();     //要下载的总字节大小
    }, AndroidSchedulers.mainThread()) //指定回调(进度/成功/失败)线程,不指定,默认在请求所在线程回调
    .subscribe(s -> {                  //s为String类型，这里为文件存储路径
        //下载完成
    }, throwable -> {
        //下载失败
    });
```

## 断点下载、带进度回调
```java
//断点下载，带进度
public void breakpointDownloadAndProgress() {
    String destPath = getExternalCacheDir() + "/" + "Miaobo.apk";
    long length = new File(destPath).length();
    RxHttp.get("http://update.9158.com/miaolive/Miaolive.apk")
        .setRangeHeader(length)                //设置开始下载位置，结束位置默认为文件末尾
        .asDownload(destPath, length, progress -> { //如果需要衔接上次的下载进度，则需要传入上次已下载的字节数length
            //下载进度回调,0-100，仅在进度有更新时才会回调
            int currentProgress = progress.getProgress(); //当前进度 0-100
            long currentSize = progress.getCurrentSize(); //当前已下载的字节大小
            long totalSize = progress.getTotalSize();     //要下载的总字节大小
        }, AndroidSchedulers.mainThread()) //指定回调(进度/成功/失败)线程,不指定,默认在请求所在线程回调
        .subscribe(s -> { //s为String类型
            //下载成功，处理相关逻辑
        }, throwable -> {
            //下载失败，处理相关逻辑
        });
}
```

## 初始化

```java
//设置debug模式，此模式下有日志打印
RxHttp.setDebug(boolean debug)
//非必须,只能初始化一次，第二次将抛出异常
RxHttp.init(OkHttpClient okHttpClient)
//或者，调试模式下会有日志输出
RxHttp.init(OkHttpClient okHttpClient, boolean debug)

```

## 添加公共参数/头部及重新设置url

```java
//建议在Application里设置
RxHttp.setOnParamAssembly(new Function() {
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

## Activity/Fragment/View/ViewModel/任意类生命周期结束时，自动关闭请求

```java
RxHttp.postForm("http://...")    //发送表单形式的post请求
    .asString()
    .as(RxLife.as(this))         //生命周期结束，自动关闭请求
    .subscribe(s -> {
        //成功回调
    }, throwable -> {
        //失败回调
    });

RxHttp.postForm("http://...")       //发送表单形式的post请求
    .asString()
    .as(RxLife.asOnMain(this))      //在主线程回调，并在生命周期结束，自动关闭请求
    .subscribe(s -> {
        //成功回调
    }, throwable -> {
        //失败回调
    });
```


## 常用api介绍
```java
RxHttp.postForm("/service/getIpInfo.php") //发送Form表单形式的Post请求
    .setDomainToUpdate9158IfAbsent()      //手动设置域名，不设置会添加默认域名，此方法是通过@Domain注解生成的
    .tag("RxHttp.get")                    //为单个请求设置tag
    .setUrl("http://...")                 //重新设置url
    .setJsonParams("{"versionName":" 1.0 .0 "}")  //设置Json字符串参数，非Json形式的请求调用此方法没有任何效果
    .setAssemblyEnabled(false)                 //设置是否添加公共参数，默认为true
    .cacheControl(CacheControl.FORCE_NETWORK)  //缓存控制
    .setParam(Param.postForm("http://..."))    //重新设置一个Param对象
    .add(new HashMap<>())                      //通过Map添加参数
    .add("key", "value")                       //添加int类型参数
    .addFile("file1", new File("xxx/1.png"))   //添加文件对象
    .addHeader("headerKey1", "headerValue1")   //添加头部信息
    .subscribeOn(Schedulers.io())  //指定请求线程，不指定默认在IO线程执行
    .asString()                   //使用asXXX系列方法确定返回类型,此时返回Observable对象
    .as(RxLife.asOnMain(this))    //主线程回调，并在页面销毁时，自动关闭未完成的请求
    .subscribe(s -> {    //订阅观察者
        //成功回调
    }, throwable -> {
        //失败回调
    });
```

## 混淆

RxHttp作为开源库，可混淆，也可不混淆，如果不希望被混淆，请在proguard-rules.pro文件添加以下代码

```java
-keep class rxhttp.**{*;}
```

## 小技巧

在这教大家一个小技巧，由于使用RxHttp发送请求都遵循请求三部曲，故我们可以在android studio 设置代码模版,如下

![image](https://github.com/liujingxing/RxHttp/blob/master/screen/templates.png)

如图设置好后，写代码时，输入rp,就会自动生成模版，如下：

![image](https://github.com/liujingxing/RxHttp/blob/master/screen/templates_demo.gif)

## RxHttp类没有自动生成，报红检查步骤

1、检查有没有依赖注解处理器
如：annotationProcessor 'com.rxjava.rxhttp:rxhttp-compiler:x.x.x’ (x.x.x为具体版本号)

2、检查下有没有使用@DefaultDomain、@Domain、@Parser、@Param其中一个注解，且要在依赖注解处理器所在的Module中使用注解

3、rebuild一下项目

4、kotlin用户，要使用kapt依赖注解处理器，
如：kapt 'com.rxjava.rxhttp:rxhttp-compiler:x.x.x’ (x.x.x为具体版本号)

5、kotlin用户，检查Module的build.gradle文件中，有没有导入kapt插件
如：apply plugin: 'kotlin-kapt'

经过以上步骤后还未生成RxHttp类，请联系我。


**[常见问题](https://github.com/liujingxing/RxHttp/wiki/%E5%B8%B8%E8%A7%81%E9%97%AE%E9%A2%98)**

**[更新日志](https://github.com/liujingxing/RxHttp/wiki/%E6%9B%B4%E6%96%B0%E6%97%A5%E5%BF%97)**









