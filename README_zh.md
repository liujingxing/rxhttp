# RxHttp

[English](https://github.com/liujingxing/rxhttp/blob/master/README.md) | 中文文档

[![](https://jitpack.io/v/liujingxing/rxhttp.svg)](https://jitpack.io/#liujingxing/rxhttp) 

***RxHttp&RxLife 交流群：378530627   &nbsp;&nbsp;&nbsp;&nbsp;  微信群先加我个人微信：ljx-studio*** 

# 主要优势

  ***1. 30秒即可上手，学习成本极低***

  ***2. 史上最优雅的支持 Kotlin 协程***

  ***3. 史上最优雅的处理多个BaseUrl及动态BaseUrl***

  ***4. 史上最优雅的对错误统一处理，且不打破Lambda表达式***

  ***5. 史上最优雅的文件上传/下载/断点下载/进度监听，已适配Android 10***

  ***6. 支持Gson、Xml、ProtoBuf、FastJson等第三方数据解析工具***

  ***7. 支持Get、Post、Put、Delete等任意请求方式，可自定义请求方式***

  ***8. 支持在Activity/Fragment/View/ViewModel/任意类中，自动关闭请求***

  ***9. 支持全局加解密、添加公共参数及头部、网络缓存，均支持对某个请求单独设置***

# 请求三部曲

![](https://p6-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/0863323afac54cb9965aa0175d5c30b1~tplv-k3u1fbpfcp-watermark.image)

# 上手教程

30秒上手教程：[30秒上手新一代Http请求神器RxHttp](https://juejin.im/post/5cfcbbcbe51d455a694f94df)

协程文档：[RxHttp ，比Retrofit 更优雅的协程体验](https://juejin.im/post/5e77604fe51d4527066eb81a#heading-2)

掘金详细文档：[RxHttp 让你眼前一亮的Http请求框架](https://juejin.im/post/5ded221a518825125d14a1d4)

wiki详细文档：https://github.com/liujingxing/rxhttp/wiki  (此文档会持续更新)


自动关闭请求用到的RxLife类，详情请查看[RxLife库](https://github.com/liujingxing/rxlife)

[更新日志](https://github.com/liujingxing/rxhttp/wiki/%E6%9B%B4%E6%96%B0%E6%97%A5%E5%BF%97)  &nbsp;&nbsp;&nbsp;&nbsp; 
[遇到问题，点击这里，99%的问题都能自己解决](https://github.com/liujingxing/rxhttp/wiki/FAQ)


# 上手准备

***[Maven依赖点击这里](https://github.com/liujingxing/rxhttp/blob/master/maven_dependency.md)***

***1、RxHttp目前已适配`OkHttp 3.12.0 - 4.9.1`版本(4.3.0版本除外), 如你想要兼容21以下，请依赖`OkHttp 3.12.x`，该版本最低要求 API 9***

***2、asXxx方法内部是通过RxJava实现的，而RxHttp 2.2.0版本起，内部已剔除RxJava，如需使用，请自行依赖RxJava并告知RxHttp依赖的Rxjava版本***


## 必须

将`jitpack`添加到项目的`build.gradle`文件中，如下：
```java
allprojects {
    repositories {
        maven { url "https://jitpack.io" }
    }
}
```
`注：RxHttp 2.6.0版本起，已全面从JCenter迁移至jitpack`

```java
//使用kapt依赖rxhttp-compiler时必须
apply plugin: 'kotlin-kapt'

android {
    //必须，java 8或更高
    compileOptions {
        sourceCompatibility JavaVersion.VERSION_1_8
        targetCompatibility JavaVersion.VERSION_1_8
    }
}

dependencies {
    implementation 'com.github.liujingxing.rxhttp:rxhttp:2.6.1'
    implementation 'com.squareup.okhttp3:okhttp:4.9.1' //rxhttp v2.2.2版本起，需要手动依赖okhttp
    kapt 'com.github.liujingxing.rxhttp:rxhttp-compiler:2.6.1' //生成RxHttp类，纯Java项目，请使用annotationProcessor代替kapt
 }
```

## 可选
```java
android {
    defaultConfig {
        javaCompileOptions {
            annotationProcessorOptions {
                arguments = [
                    //使用asXxx方法时必须，告知RxHttp你依赖的rxjava版本，可传入rxjava2、rxjava3
                    rxhttp_rxjava: 'rxjava3', 
                    rxhttp_package: 'rxhttp'   //非必须，指定RxHttp类包名
                ]
            }
        }
    }
}
dependencies {
    implementation 'com.github.liujingxing.rxlife:rxlife-coroutine:2.1.0' //管理协程生命周期，页面销毁，关闭请求
    
    //rxjava2   (RxJava2/Rxjava3二选一，使用asXxx方法时必须)
    implementation 'io.reactivex.rxjava2:rxjava:2.2.8'
    implementation 'io.reactivex.rxjava2:rxandroid:2.1.1'
    implementation 'com.github.liujingxing.rxlife:rxlife-rxjava2:2.1.0' //管理RxJava2生命周期，页面销毁，关闭请求

    //rxjava3
    implementation 'io.reactivex.rxjava3:rxjava:3.0.6'
    implementation 'io.reactivex.rxjava3:rxandroid:3.0.0'
    implementation 'com.github.liujingxing.rxlife:rxlife-rxjava3:2.1.0' //管理RxJava3生命周期，页面销毁，关闭请求

    //非必须，根据自己需求选择 RxHttp默认内置了GsonConverter
    implementation 'com.github.liujingxing.rxhttp:converter-fastjson:2.6.1'
    implementation 'com.github.liujingxing.rxhttp:converter-jackson:2.6.1'
    implementation 'com.github.liujingxing.rxhttp:converter-moshi:2.6.1'
    implementation 'com.github.liujingxing.rxhttp:converter-protobuf:2.6.1'
    implementation 'com.github.liujingxing.rxhttp:converter-simplexml:2.6.1'
}
```

最后，***rebuild一下(此步骤是必须的)*** ，就会自动生成RxHttp类


# 混淆

`RxHttp v2.2.8`版本起，无需添加任何混淆规则(内部自带混淆规则)，v2.2.8以下版本，请[查看混淆规则](https://github.com/liujingxing/rxhttp/wiki/关于混淆),并添加到自己项目中

# 友情链接

[开源阅读 3.0](https://github.com/gedoor/legado)

`注：如果你的项目用到了RxHttp，想要在这里展示，请联系我。`

# Demo演示
<img src="https://github.com/liujingxing/rxhttp/blob/master/screen/screenrecorder-2019-11-27_22_56_26.gif" width = "240" height = "520" />

> 更多功能，请下载Demo体验

## Donations
如果它对你帮助很大，并且你很想支持库的后续开发和维护，那么你可以扫下方二维码随意打赏我，就当是请我喝杯咖啡或是啤酒，开源不易，感激不尽

![rxhttp_donate.png](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/aafa7d05cfda4b2ea2a092bba8ebc1a0~tplv-k3u1fbpfcp-watermark.image)


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
