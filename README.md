[ ![Download](https://api.bintray.com/packages/32774707/maven/rxhttp2/images/download.svg) ](https://bintray.com/32774707/maven/rxhttp2/_latestVersion)

# RxHttp主要优势

  ***1. 30秒即可上手，学习成本极低***

  ***2. 史上最优雅的支持 Kotlin 协程***

  ***3. 史上最优雅的处理多个BaseUrl及动态BaseUrl***

  ***4. 史上最优雅的对错误统一处理，且不打破Lambda表达式***

  ***5. 史上最优雅的实现文件上传/下载及进度的监听，且支持断点下载***

  ***6. 支持Gson、Xml、ProtoBuf、FastJson等第三方数据解析工具***

  ***7. 支持Get、Post、Put、Delete等任意请求方式，可自定义请求方式***

  ***8. 支持在Activity/Fragment/View/ViewModel/任意类中，自动关闭请求***

  ***9. 支持全局加解密、添加公共参数及头部、网络缓存，均支持对某个请求单独设置***

**Gradle依赖**

```java
//以下三个为必须，其它均为非必须
implementation 'com.ljx.rxhttp:rxhttp:2.2.5'
implementation 'com.squareup.okhttp3:okhttp:4.7.2'  //rxhttp v2.2.2版本起，需要手动依赖okhttp
annotationProcessor 'com.ljx.rxhttp:rxhttp-compiler:2.2.5' //生成RxHttp类，kotlin用户，请使用kapt替代annotationProcessor

implementation 'com.ljx.rxlife:rxlife-coroutine:2.0.0'  //管理协程生命周期，页面销毁，关闭请求
implementation 'com.ljx.rxlife2:rxlife-rxjava:2.0.0'    //管理RxJava2生命周期，页面销毁，关闭请求
implementation 'com.ljx.rxlife3:rxlife-rxjava:3.0.0'    //管理RxJava3生命周期，页面销毁，关闭请求

//Converter 根据自己需求选择 RxHttp默认内置了GsonConverter
implementation 'com.ljx.rxhttp:converter-jackson:2.2.5'
implementation 'com.ljx.rxhttp:converter-fastjson:2.2.5'
implementation 'com.ljx.rxhttp:converter-protobuf:2.2.5'
implementation 'com.ljx.rxhttp:converter-simplexml:2.2.5'
```
[遇到问题，点击这里，99%的问题都能自己解决](https://github.com/liujingxing/okhttp-RxHttp/wiki/FAQ)

***RxHttp&RxLife 交流群：378530627***

# 上手准备(必看)

1、RxHttp 要求项目使用Java 8 或更高版本，请在 app 的 build.gradle 添加以下代码

```java
compileOptions {
    sourceCompatibility JavaVersion.VERSION_1_8
    targetCompatibility JavaVersion.VERSION_1_8
}
```

2、OkHttp兼容问题

RxHttp目前对OkHttp 的 `v3.12.0 - v4.7.x`均已完成适配工作（v4.3.0除外，该版本有一bug，暂时无法适配），
在你依赖okhttp时，需要告诉rxhttp你依赖的okhttp版本号，如下：

```java
defaultConfig {
    javaCompileOptions {
        annotationProcessorOptions {
            //告知RxHttp你依赖的okhttp版本
            arguments = [rxhttp_okhttp: '4.7.2']  //可传入3.12.x至4.7.x任一版本(4.3.0除外)
        }
    }
}
dependencies {
   implementation 'com.squareup.okhttp3:okhttp:4.7.2'
}
```

***注:`OkHttp 3.14.x`以上版本, 最低要求为API 21，如你想要兼容21以下，请依赖`OkHttp 3.12.x`，该版本最低要求 API 9***

3、RxJava兼容问题

RxHttp 2.2.0版本起，内部不在依赖RxJava相关库，采用外挂的方法替代，如你需要结合RxJava发送请求，请单独配置，如下：

```java
defaultConfig {
    javaCompileOptions {
        annotationProcessorOptions {
            //手动告知RxHttp要使用的RxJava版本，才会生成asXxx方法
            arguments = [rxhttp_rxjava: 'rxjava3']  //可传入rxjava2、rxjava3
        }
    }
}
//并自行依赖RxJava相关库，RxJava2/Rxjava3二选一，如下：
dependencies {
   //rxjava2
   implementation 'io.reactivex.rxjava2:rxjava:2.2.8'
   implementation 'io.reactivex.rxjava2:rxandroid:2.1.1'
   implementation 'com.ljx.rxlife2:rxlife-rxjava:2.0.0'    //管理RxJava2生命周期，页面销毁，关闭请求

   //rxjava3
   implementation 'io.reactivex.rxjava3:rxjava:3.0.2'
   implementation 'io.reactivex.rxjava3:rxandroid:3.0.0'
   implementation 'com.ljx.rxlife3:rxlife-rxjava:3.0.0'    //管理RxJava3生命周期，页面销毁，关闭请求
}
```

***注: 如你需要同时指定okhttp及rxjava版本，只需用逗号隔开即可，如下：***
```java
annotationProcessorOptions {
    arguments = [
        rxhttp_okhttp: '4.7.2',
        rxhttp_rxjava: 'rxjava3'
    ]
}
```

最后，***rebuild一下(此步骤是必须的)*** ，就会自动生成RxHttp类，到这，准备工作完毕。


# 上手教程

30秒上手教程：https://juejin.im/post/5cfcbbcbe51d455a694f94df

掘金详细文档：https://juejin.im/post/5ded221a518825125d14a1d4

wiki详细文档：https://github.com/liujingxing/okhttp-RxHttp/wiki  (此文档会持续更新)

协程文档：[RxHttp ，比Retrofit 更优雅的协程体验](https://juejin.im/post/5e77604fe51d4527066eb81a#heading-2)

自动关闭请求用到的RxLife类，详情请查看[RxLife库](https://github.com/liujingxing/RxLife)

[更新日志](https://github.com/liujingxing/okhttp-RxHttp/wiki/%E6%9B%B4%E6%96%B0%E6%97%A5%E5%BF%97) &nbsp;&nbsp;&nbsp;&nbsp;
[已知问题](https://github.com/liujingxing/okhttp-RxHttp/wiki/%E5%B7%B2%E7%9F%A5%E9%97%AE%E9%A2%98) &nbsp;&nbsp;&nbsp;&nbsp;
[Java工程依赖注意事项](https://github.com/liujingxing/okhttp-RxHttp/wiki/Java%E5%B7%A5%E7%A8%8B%E4%BE%9D%E8%B5%96)


# 混淆

RxHttp作为开源库，可混淆，也可不混淆，如果不希望被混淆，请在proguard-rules.pro文件添加以下代码

```bash
# RxHttp
-keep class rxhttp.**{*;}
# OkHttp
-keep class okhttp3.**{*;}
-keep class okio.**{*;}
```

# 小技巧

在这教大家一个小技巧，由于使用RxHttp发送请求都遵循请求三部曲，故我们可以在android studio 设置代码模版,如下

![image](https://github.com/liujingxing/RxHttp/blob/master/screen/templates.png)

如图设置好后，写代码时，输入rp,就会自动生成模版，如下：

![image](https://github.com/liujingxing/RxHttp/blob/master/screen/templates_demo.gif)


# Demo演示
<img src="https://github.com/liujingxing/RxHttp/blob/master/screen/screenrecorder-2019-11-27_22_56_26.gif" width = "240" height = "520" />

> 更多功能，请下载Demo体验

## Donations
如果它对你帮助很大，并且你很想支持库的后续开发和维护，那么你可以扫下方二维码随意打赏我，就当是请我喝杯咖啡或是啤酒，开源不易，感激不尽

![image](https://github.com/liujingxing/RxHttp/blob/master/screen/donations.jpeg)


# Licenses
```
Copyright 2019 liujingxing

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
