[ ![Download](https://api.bintray.com/packages/32774707/maven/rxhttp/images/download.svg) ](https://bintray.com/32774707/maven/rxhttp/_latestVersion)

# RxHttp
RxHttp是基于OkHttp的二次封装，并于RxJava做到无缝衔接，一条链就能发送任意请求，主要优势如下 :

  ***1. 30秒即可上手，学习成本极低***
  
  ***2. 史上最优雅的处理网络缓存***
  
  ***3. 史上最优雅的处理多个BaseUrl及动态BaseUrl***
  
  ***4. 史上最优雅的对错误统一处理，且不打破Lambda表达式***
  
  ***5. 史上最优雅的实现文件上传/下载及进度的监听，且支持断点下载***
  
  ***6. 支持Gson、Xml、ProtoBuf、FastJson等第三方数据解析工具***
  
  ***7. 支持Get、Post、Put、Delete等任意请求方式，可自定义请求方式***
  
  ***8. 支持在Activity/Fragment/View/ViewModel/任意类中，自动关闭请求***
  
  ***9. 支持统一加解密，且可对单个请求设置是否加解密***
  
  ***10. 支持添加公共参数/头部，且可对单个请求设置是否添加公共参数/头部***



## 上手教程

**30秒上手教程：https://juejin.im/post/5cfcbbcbe51d455a694f94df**

**详细介绍：https://juejin.im/post/5ded221a518825125d14a1d4**

**自动关闭请求用到的RxLife类，详情请查看[RxLife库](https://github.com/liujingxing/RxLife)**

**RxHttp&RxLife 交流群：378530627**

**[常见问题](https://github.com/liujingxing/RxHttp/wiki/%E5%B8%B8%E8%A7%81%E9%97%AE%E9%A2%98)**

**[更新日志](https://github.com/liujingxing/RxHttp/wiki/%E6%9B%B4%E6%96%B0%E6%97%A5%E5%BF%97)**


## Demo演示

![image](https://github.com/liujingxing/RxHttp/blob/master/screen/screenrecorder-2019-11-27_22_56_26.gif)

> 更多功能，请下载Demo体验

**Gradle引用方法**

```java
dependencies {
   implementation 'com.rxjava.rxhttp:rxhttp:1.3.6'
   annotationProcessor 'com.rxjava.rxhttp:rxhttp-compiler:1.3.6' //注解处理器，生成RxHttp类
   implementation 'com.rxjava.rxlife:rxlife:1.1.0'  //页面销毁，关闭请求，非必须

   //Converter 根据自己需求选择  非必须  RxHttp默认内置了GsonConverter
   implementation 'com.rxjava.rxhttp:converter-jackson:1.3.6'
   implementation 'com.rxjava.rxhttp:converter-fastjson:1.3.6'
   implementation 'com.rxjava.rxhttp:converter-protobuf:1.3.6'
   implementation 'com.rxjava.rxhttp:converter-simplexml:1.3.6'
}
```

`注：kotlin用户，请使用kapt替代annotationProcessor`


## API兼容

RxHttp最低要求为API 15，但是由于内部依赖OkHttp 3.14.1版本, 最低要求为API 21。
如果你要的项目要兼容到API 15，请将RxHttp内部的OkHttp剔除，并引入低版本的OkHttp，如下：

```
implementation('com.rxjava.rxhttp:rxhttp:x.x.x') { //xxx为RxHttp最新版本
    exclude group: "com.squareup.okhttp3"
}
implementation 'com.squareup.okhttp3:okhttp:3.12.6' //此版本最低要求 API 9
```

## 准备工作

**RxHttp 要求项目使用Java 8，请在 app 的 build.gradle 添加以下代码**

```java
compileOptions {
    sourceCompatibility JavaVersion.VERSION_1_8
    targetCompatibility JavaVersion.VERSION_1_8
}
```
此时rebuild一下项目，就能看到RxHttp类了


## 请求三部曲
```java
RxHttp.get("/service/...")          //第一步，确定请求方式，可以选择postForm、postJson等方法
    .asString()                     //第二步，使用asXXX系列方法确定返回类型
    .subscribe(s -> {               //第三部, 订阅观察者
        //成功回调
    }, throwable -> {
        //失败回调
    });
```
**任意请求，任意返回数据类型，皆遵循请求三部曲**

**任意请求，任意返回数据类型，皆遵循请求三部曲**

**任意请求，任意返回数据类型，皆遵循请求三部曲**


## get请求
```java
RxHttp.get("/service/...")     
    .add("key", "value")
    .asString()                     
    .subscribe(s -> {               
        //成功回调
    }, throwable -> {
        //失败回调
    });

```

## post Form请求
```java
RxHttp.postForm("/service/...")       //发送表单形式的post请求
    .add("key", "value")
    .asString()
    .subscribe(s -> {
        //成功回调
    }, throwable -> {
        //失败回调
    });
```

## 返回自定义的数据类型
```java
RxHttp.postForm("/service/...")     //发送表单形式的post请求
    .asObject(Student.class)      //返回Student对象
    .subscribe(student -> {
        //成功回调
    }, throwable -> {
        //失败回调
    });


RxHttp.postForm("/service/...")     //发送表单形式的post请求
    .asList(Student.class)        //返回List<Student>集合
    .subscribe(students -> {
        //成功回调
    }, throwable -> {
        //失败回调
    });

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

## 请求开始/结束回调
```java
RxHttp.get("/service/...")
    .asString()
    .observeOn(AndroidSchedulers.mainThread())
    .doOnSubscribe(disposable -> {
        //请求开始，当前在主线程回调
    })
    .doFinally(() -> {
        //请求结束，当前在主线程回调
    })
    .as(RxLife.as(this))  //感知生命周期
    .subscribe(pageList -> {
        //成功回调，当前在主线程回调
    }, (OnError) error -> {
        //失败回调，当前在主线程回调
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

2、rebuild一下项目

3、kotlin用户，要使用kapt依赖注解处理器，
如：kapt 'com.rxjava.rxhttp:rxhttp-compiler:x.x.x’ (x.x.x为具体版本号)

4、kotlin用户，检查Module的build.gradle文件中，有没有导入kapt插件
如：apply plugin: 'kotlin-kapt'

经过以上步骤后还未生成RxHttp类，请联系我。


## Donations
如果它对你帮助很大，并且你很想支持库的后续开发和维护，那么你可以扫下方二维码随意打赏我，就当是请我喝杯咖啡或是啤酒，开源不易，感激不尽

![image](https://github.com/liujingxing/RxHttp/blob/master/screen/donations.jpeg)





