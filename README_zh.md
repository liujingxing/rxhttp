# RxHttp

[English](https://github.com/liujingxing/rxhttp/blob/master/README.md) | 中文文档

[![](https://jitpack.io/v/liujingxing/rxhttp.svg)](https://jitpack.io/#liujingxing/rxhttp) 
![](https://img.shields.io/badge/API-9+-blue.svg)
[![](https://img.shields.io/badge/change-更新日志-success.svg)](https://github.com/liujingxing/rxhttp/wiki/%E6%9B%B4%E6%96%B0%E6%97%A5%E5%BF%97)
[![](https://img.shields.io/badge/FAQ-常见问题-success.svg)](https://github.com/liujingxing/rxhttp/wiki/FAQ)
[![](https://img.shields.io/badge/掘金-@不怕天黑-blue.svg)](https://juejin.cn/user/272334612601559/posts)
[![](https://img.shields.io/badge/QQ群-378530627-red.svg)](https://jq.qq.com/?_wv=1027&k=E53Hakvv)

***加我微信 ljx-studio 拉你进微信群(备注RxHttp)*** 

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

![](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/96962077a7874293919bc8379b2d45ac~tplv-k3u1fbpfcp-watermark.image)

***代码表示***
```kotlin
//Kotlin + Await             //Kotlin + Flow              //Kotlin + RxJava            //Java + RxJava
RxHttp.get("/server/..")     RxHttp.get("/server/..")     RxHttp.get("/server/..")     RxHttp.get("/server/..")   
    .add("key", "value")         .add("key", "value")         .add("key", "value")         .add("key", "value")
    .toClass<User>()             .toFlow<User>()              .asClass<User>()             .asClass(User.class)
    .awaitResult {               .catch {                     .subscribe({                 .subscribe(user -> {
        //成功回调                     //异常回调                    //成功回调                     //成功回调     
    }.onFailure {                }.collect {                  }, {                         }, throwable -> { 
        //异常回调                     //成功回调                    //异常回调                     //异常回调
    }                            }                            })                           });
```

# 上手教程

30秒上手教程：[30秒上手新一代Http请求神器RxHttp](https://juejin.im/post/5cfcbbcbe51d455a694f94df)

Flow文档：[RxHttp + Flow 三步搞定任意请求](https://juejin.cn/post/7017604875764629540)

Await文档：[RxHttp ，比Retrofit 更优雅的协程体验](https://juejin.im/post/5e77604fe51d4527066eb81a#heading-2)

RxJava及核心Api介绍：[RxHttp 让你眼前一亮的Http请求框架](https://juejin.im/post/5ded221a518825125d14a1d4)

wiki详细文档：https://github.com/liujingxing/rxhttp/wiki  (此文档会持续更新)


自动关闭请求用到的RxLife类，详情请查看[RxLife库](https://github.com/liujingxing/rxlife)


# 上手准备

***[Maven依赖点击这里](https://github.com/liujingxing/rxhttp/blob/master/maven_dependency.md)***

***1、RxHttp目前已适配`OkHttp 3.12.0 - 4.9.3`版本(4.3.0版本除外), 如你想要兼容21以下，请依赖`OkHttp 3.12.x`，该版本最低要求 API 9***

***2、asXxx方法内部是通过RxJava实现的，而RxHttp 2.2.0版本起，内部已剔除RxJava，如需使用，请自行依赖RxJava并告知RxHttp依赖的Rxjava版本***


## 必须

<details>
<summary>1、配置jitpack到项目的build.gradle文件中</summary>
 
```java
allprojects {
    repositories {
        maven { url "https://jitpack.io" }
    }
}
```
</details>

<details>
<summary>2、配置java 8或更高</summary>
 
```java
android {
    //必须，java 8或更高
    compileOptions {
        sourceCompatibility JavaVersion.VERSION_1_8
        targetCompatibility JavaVersion.VERSION_1_8
    }
}
```
</details>

<details open>
<summary>3、添加RxHttp依赖</summary>
 
```kotlin
//annotationProcessor无需依赖额外插件
plugins {
    // kapt/ksp 选其一
    // id 'kotlin-kapt'
    id 'com.google.devtools.ksp' version '1.7.10-1.0.6'
}

//让IDE知道ksp生成的kotlin代码(仅使用ksp时才需要)
kotlin {
    sourceSets.debug {
        //如果通过productFlavors配置了多渠道，则配置 /../ksp/xxxDebug/kotlin
        kotlin.srcDir("build/generated/ksp/debug/kotlin")
    }
}

dependencies {
    def rxhttp_version = '2.9.5'
    implementation 'com.squareup.okhttp3:okhttp:4.10.0'  
    implementation "com.github.liujingxing.rxhttp:rxhttp:$rxhttp_version"
    // ksp/kapt/annotationProcessor 选其一
    ksp "com.github.liujingxing.rxhttp:rxhttp-compiler:$rxhttp_version"
 }
```
</details>

[ksp、kapt、annotationProcessor 如何选择点击这里](https://github.com/liujingxing/rxhttp/wiki/ksp%E3%80%81kapt%E3%80%81annotationProcessor-%E7%94%A8%E6%B3%95%E5%8F%8A%E5%8C%BA%E5%88%AB)

## 可选

### 1、配置Converter

```kotlin
//非必须，根据自己需求选择 RxHttp默认内置了GsonConverter
implementation "com.github.liujingxing.rxhttp:converter-serialization:$rxhttp_version"
implementation "com.github.liujingxing.rxhttp:converter-fastjson:$rxhttp_version"
implementation "com.github.liujingxing.rxhttp:converter-jackson:$rxhttp_version"
implementation "com.github.liujingxing.rxhttp:converter-moshi:$rxhttp_version"
implementation "com.github.liujingxing.rxhttp:converter-protobuf:$rxhttp_version"
implementation "com.github.liujingxing.rxhttp:converter-simplexml:$rxhttp_version"
```

### 2、配置RxJava

<details open>
<summary>RxHttp + RxJava3</summary>
 
 ```java
implementation 'io.reactivex.rxjava3:rxjava:3.1.5'
implementation 'io.reactivex.rxjava3:rxandroid:3.0.0'
implementation 'com.github.liujingxing.rxlife:rxlife-rxjava3:2.2.2' //管理RxJava3生命周期，页面销毁，关闭请求
```
 
</details>

<details>
<summary>RxHttp + RxJava2</summary>
 
```java
implementation 'io.reactivex.rxjava2:rxjava:2.2.8'
implementation 'io.reactivex.rxjava2:rxandroid:2.1.1'
implementation 'com.github.liujingxing.rxlife:rxlife-rxjava2:2.2.2' //管理RxJava2生命周期，页面销毁，关闭请求
```
</details>


<details open>
<summary>通过ksp传递RxJava版本</summary>
 
```java
ksp {
    arg("rxhttp_rxjava", "3.1.5")
}
```
 
</details>

<details>
<summary>通过kapt传递RxJava版本</summary>
 
```java
kapt {
    arguments {
        arg("rxhttp_rxjava", "3.1.5")
    }
}
```
 
</details>
 
<details>
<summary>通过javaCompileOptions传递RxJava版本</summary>
 
```java
android {
    defaultConfig {
        javaCompileOptions {
            annotationProcessorOptions {
                arguments = [
                    //使用asXxx方法时必须，传入你依赖的RxJava版本
                    rxhttp_rxjava: '3.1.5', 
                ]
            }
        }
    }
}
```
 
</details>


### 3、指定RxHttp相关类包名

<details open>
<summary>通过ksp指定RxHttp相关类包名</summary>
 
```java
ksp {
    arg("rxhttp_package", "rxhttp")  //指定RxHttp类包名，可随意指定 
}
```
 
</details>

<details>
<summary>通过kapt指定RxHttp相关类包名</summary>
 
```java
 
kapt {
    arguments {
        arg("rxhttp_package", "rxhttp")  //指定RxHttp类包名，可随意指定
    }
}
```
 
</details>
 
<details>
<summary>通过javaCompileOptions指定RxHttp相关类包名</summary>
 
```java
android {
    defaultConfig {
        javaCompileOptions {
            annotationProcessorOptions {
                arguments = [
                    rxhttp_package: 'rxhttp',  //指定RxHttp类包名，可随意指定
                ]
            }
        }
    }
}
```
</details>


最后，***rebuild一下(此步骤是必须的)*** ，就会自动生成RxHttp类


# 混淆

`RxHttp v2.2.8`版本起，无需添加任何混淆规则(内部自带混淆规则)，v2.2.8以下版本，请[查看混淆规则](https://github.com/liujingxing/rxhttp/wiki/关于混淆),并添加到自己项目中

# Demo演示
<img src="https://github.com/liujingxing/rxhttp/blob/master/screen/demo.gif" width = "360" height = "640" />


> 更多功能，请[下载apk](https://github.com/liujingxing/rxhttp/blob/master/screen/app-debug.apk)体验

## Donations
如果它对你帮助很大，并且你很想支持库的后续开发和维护，那么你可以扫下方二维码随意打赏我，就当是请我喝杯咖啡或是啤酒，开源不易，感激不尽

![donations.png](https://p1-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/fa6d3941c2c944e59831640fa0ece60d~tplv-k3u1fbpfcp-watermark.image?)


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
