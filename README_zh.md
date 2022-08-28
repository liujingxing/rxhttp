# RxHttp

[English](https://github.com/liujingxing/rxhttp/blob/master/README.md) | 中文文档

[![](https://jitpack.io/v/liujingxing/rxhttp.svg)](https://jitpack.io/#liujingxing/rxhttp) 
![](https://img.shields.io/badge/API-9+-blue.svg)
[![](https://img.shields.io/badge/change-更新日志-success.svg)](https://github.com/liujingxing/rxhttp/wiki/%E6%9B%B4%E6%96%B0%E6%97%A5%E5%BF%97)
[![](https://img.shields.io/badge/FAQ-常见问题-success.svg)](https://github.com/liujingxing/rxhttp/wiki/FAQ)
[![](https://img.shields.io/badge/掘金-@不怕天黑-blue.svg)](https://juejin.cn/user/272334612601559/posts)
[![](https://img.shields.io/badge/QQ群-378530627-red.svg)](https://jq.qq.com/?_wv=1027&k=E53Hakvv)

***加我微信 ljx-studio 拉你进微信群(备注RxHttp)*** 

# 1、主要优势

  ***1. 30秒即可上手，学习成本极低***

  ***2. 史上最优雅的支持 Kotlin 协程***

  ***3. 史上最优雅的处理多个BaseUrl及动态BaseUrl***

  ***4. 史上最优雅的对错误统一处理，且不打破Lambda表达式***

  ***5. 史上最优雅的文件上传/下载/断点下载/进度监听，已适配Android 10***

  ***6. 支持Gson、Xml、ProtoBuf、FastJson等第三方数据解析工具***

  ***7. 支持Get、Post、Put、Delete等任意请求方式，可自定义请求方式***

  ***8. 支持在Activity/Fragment/View/ViewModel/任意类中，自动关闭请求***

  ***9. 支持全局加解密、添加公共参数及头部、网络缓存，均支持对某个请求单独设置***

# 2、请求三部曲

![](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/96962077a7874293919bc8379b2d45ac~tplv-k3u1fbpfcp-watermark.image)

***代码表示，
[asXxx、toXxx、toFlowXxx方法介绍点这里](https://github.com/liujingxing/rxhttp/wiki/RxJava%E3%80%81Await%E3%80%81Flow-%E5%AF%B9%E5%BA%94%E7%9A%84-asXxx%E3%80%81toXxx%E3%80%81toFlowXxx%E6%96%B9%E6%B3%95%E4%BB%8B%E7%BB%8D)***
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

***RxHttp与Retrofit对比***

| 功能说明 | RxHttp | Retrofit |
| --- | :---: | :---: |
| 版本| v2.9.3| v2.9.0 |
| 状态| 维护中| 维护中 |
| 标准RESTful风格| ✅ | ✅ |
| 学习成本| 低 | 高|
| 扩展性| 高| 高|
| jar包大小| 335k| 125k|
| RxJava| RxJava  ❌<br>RxJava2✅<br>RxJava3✅| RxJava  ✅<br>RxJava2✅<br>RxJava3✅|
| Kotlin协程| ✅ | ✅ |
| Flow流| ✅ | ✅ |
| Converter| Gson✅<br> Jackson✅<br> fastJson✅<br> Moshi✅<br> Protobuf✅<br> simplexml✅<br> kotlinx.serialization✅<br> 自定义✅<br> | Gson✅<br> Jackson✅<br> fastJson✅<br> Moshi✅<br> Protobuf✅<br> simplexml✅<br> kotlinx.serialization✅<br> 自定义✅<br> |
| 关闭请求 | 手动✅<br>自动✅<br>批量✅| 手动✅<br>自动✅<br>批量✅ |
| 文件上传/下载/进度监听| ✅ | ❌需再次封装|
| Android 10分区存储| ✅ | ❌需再次封装|
| 公共参数| ✅| ❌需再次封装 |
| 多域名/动态域名| ✅好用 | ✅一般 |
| 日志打印| ✅|  ✅ |
| Json数据格式化输出| ✅| ❌需再次封装 |
| 业务code统一判断| ✅ | ❌需再次封装|
| 请求缓存| ✅ | ❌需再次封装|
| 全局加解密| ✅ | ❌需再次封装 |
| 部分字段解密 | ✅ | ❌需再次封装 |


# 3、相关文档

30秒上手教程：[30秒上手新一代Http请求神器RxHttp](https://juejin.im/post/5cfcbbcbe51d455a694f94df)

Flow文档：[RxHttp + Flow 三步搞定任意请求](https://juejin.cn/post/7017604875764629540)

Await文档：[RxHttp ，比Retrofit 更优雅的协程体验](https://juejin.im/post/5e77604fe51d4527066eb81a#heading-2)

RxJava及核心Api介绍：[RxHttp 让你眼前一亮的Http请求框架](https://juejin.im/post/5ded221a518825125d14a1d4)

wiki详细文档：https://github.com/liujingxing/rxhttp/wiki  (此文档会持续更新)


自动关闭请求用到的RxLife类，详情请查看[RxLife库](https://github.com/liujingxing/rxlife)


# 4、上手准备

***1、RxHttp依赖有3种方式，选择其中一种就好，
[ksp、kapt、annotationProcessor 如何选择点击这里](https://github.com/liujingxing/rxhttp/wiki/ksp%E3%80%81kapt%E3%80%81annotationProcessor-%E7%94%A8%E6%B3%95%E5%8F%8A%E5%8C%BA%E5%88%AB)***

***2、asXxx方法内部通过RxJava实现，如需使用，需额外依赖RxJava并告知RxHttp你依赖的Rxjava版本***

***3、RxHttp已适配`OkHttp 3.12.0 - v4.10.0`版本(4.3.0除外), 如需兼容21以下，请依赖`OkHttp 3.12.x`，该版本最低要求 API 9***

***4、[Maven依赖点击这里](https://github.com/liujingxing/rxhttp/blob/master/maven_dependency.md)***

## 4.1、必须

<details open>
<summary>annotationProcessor依赖</summary>
 
```java
//1、项目的build.gradle文件
allprojects {
    repositories {
        maven { url "https://jitpack.io" }
    }
}
//2、java 8或更高
android {
    compileOptions {
        sourceCompatibility JavaVersion.VERSION_1_8
        targetCompatibility JavaVersion.VERSION_1_8
    }
}
//3、添加依赖
dependencies {
    def rxhttp_version = '2.9.3'
    implementation 'com.squareup.okhttp3:okhttp:4.10.0'  
    implementation "com.github.liujingxing.rxhttp:rxhttp:$rxhttp_version"
    annotationProcessor "com.github.liujingxing.rxhttp:rxhttp-compiler:$rxhttp_version"
}
```

</details>

<details>
<summary>kapt依赖</summary>
 
```java
//1、项目的build.gradle文件
allprojects {
    repositories {
        maven { url "https://jitpack.io" }
    }
}
//2、java 8或更高
android {
    compileOptions {
        sourceCompatibility JavaVersion.VERSION_1_8
        targetCompatibility JavaVersion.VERSION_1_8
    }
}
//3、添加插件及依赖
plugins {
    id 'kotlin-kapt'
}
 
dependencies {
    def rxhttp_version = '2.9.3'
    implementation 'com.squareup.okhttp3:okhttp:4.10.0'  
    implementation "com.github.liujingxing.rxhttp:rxhttp:$rxhttp_version"
    kapt "com.github.liujingxing.rxhttp:rxhttp-compiler:$rxhttp_version"
}
```

</details>

<details>
<summary>ksp依赖</summary>
 
```java
//1、项目的build.gradle文件
allprojects {
    repositories {
        maven { url "https://jitpack.io" }
    }
}
//2、java 8或更高，及配置sourceSets
android {
    compileOptions {
        sourceCompatibility JavaVersion.VERSION_1_8
        targetCompatibility JavaVersion.VERSION_1_8
    }
    
    applicationVariants.all { variant ->
        sourceSets {
            def name = variant.name
            getByName(name) {  //告知IDE，ksp生成的kotlin代码
                kotlin.srcDir("build/generated/ksp/$name/kotlin")
            }
        }
    }
}
//3、添加插件及依赖
plugins {
    id 'com.google.devtools.ksp' version '1.7.10-1.0.6'
}
 
dependencies {
    def rxhttp_version = '2.9.3'
    implementation 'com.squareup.okhttp3:okhttp:4.10.0'  
    implementation "com.github.liujingxing.rxhttp:rxhttp:$rxhttp_version"
    ksp "com.github.liujingxing.rxhttp:rxhttp-compiler:$rxhttp_version"
}
```
 
</details>




## 4.2、可选

### 4.2.1、配置RxJava

如果你需要结合`asXxx`方法发请求，就需要额外依赖`RxJava`，并且告知`rxhttp`你依赖的`RxJava`版本号

- ***依赖RxJava，RxJava2/RxJava3选其一***

```java
//RxJava3 
implementation 'io.reactivex.rxjava3:rxjava:3.1.5'
implementation 'io.reactivex.rxjava3:rxandroid:3.0.0'
implementation 'com.github.liujingxing.rxlife:rxlife-rxjava3:2.2.2' //管理RxJava3生命周期，页面销毁，关闭请求

//RxJava2
implementation 'io.reactivex.rxjava2:rxjava:2.2.8'
implementation 'io.reactivex.rxjava2:rxandroid:2.1.1'
implementation 'com.github.liujingxing.rxlife:rxlife-rxjava2:2.2.2' //管理RxJava2生命周期，页面销毁，关闭请求
```

- ***通过ksp/kapt/annotationProcessor,其中一种方式传递RxJava版本号***
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
<summary>通过annotationProcessor传递RxJava版本</summary>
 
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


### 4.2.2、配置Converter

```kotlin
//非必须，根据自己需求选择 RxHttp默认内置了GsonConverter
implementation "com.github.liujingxing.rxhttp:converter-serialization:$rxhttp_version"
implementation "com.github.liujingxing.rxhttp:converter-fastjson:$rxhttp_version"
implementation "com.github.liujingxing.rxhttp:converter-jackson:$rxhttp_version"
implementation "com.github.liujingxing.rxhttp:converter-moshi:$rxhttp_version"
implementation "com.github.liujingxing.rxhttp:converter-protobuf:$rxhttp_version"
implementation "com.github.liujingxing.rxhttp:converter-simplexml:$rxhttp_version"
```

### 4.2.3、指定RxHttp相关类的存放目录

如果你有多个module依赖`rxhttp-compiler`(不建议这么做，一般base module依赖就好)，则每个module下都会生成`RxHttp`类，且目录相同，在运行或打包时，就会出现RxHttp类冲突的问题，此时就需要你自定义RxHttp的存放目录，也就是RxHttp类的包名，`ksp/kapt/annotationProcessor`选择其中一种方式就好

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


# 5、混淆

- `RxHttp v2.2.8`及以上版本，无需添加任何混淆规则，将你自己的Bean类Keep下就好
- `RxHttp v2.2.8`以下版本，将[RxHttp 混淆规则](https://github.com/liujingxing/rxhttp/wiki/关于混淆)，添加到自己项目中，并将你自己的Bean类Keep下

# 6、Demo演示
<img src="https://github.com/liujingxing/rxhttp/blob/master/screen/demo.gif" width = "360" height = "640" />


> 更多功能，请[下载apk](https://github.com/liujingxing/rxhttp/blob/master/screen/app-debug.apk)体验

# 7、Donations
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
