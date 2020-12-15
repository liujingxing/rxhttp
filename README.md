# RxHttp
[ ![Download](https://api.bintray.com/packages/32774707/maven/rxhttp2/images/download.svg) ](https://bintray.com/32774707/maven/rxhttp2/_latestVersion)

A type-safe HTTP client for Android. Written based on OkHttp

[中文文档](https://juejin.im/post/5cfcbbcbe51d455a694f94df)

## 1、Feature

- 支持kotlin协程、RxJava2、RxJava3

- 支持Gson、Xml、ProtoBuf、FastJson等第三方数据解析工具

- 支持在FragmentActivity、Fragment、View、ViewModel及任意类中，自动关闭请求

- 支持全局加解密、添加公共参数及头部、网络缓存，均支持对某个请求单独设置

## 2、usage

1、Adding dependencies and configurations

```java
apply plugin: 'kotlin-kapt'

android {
    defaultConfig {
        javaCompileOptions {
            annotationProcessorOptions {
                arguments = [
                    //必须，告知RxHttp你依赖的okhttp版本，目前已适配 v3.12.0 - v4.7.2版本  (v4.3.0除外)
                    rxhttp_okhttp: okhttp_version，
                    //使用asXxx方法时必须，告知RxHttp你依赖的rxjava版本，可传入rxjava2、rxjava3
                    rxhttp_rxjava: 'rxjava3'，
                    rxhttp_package: 'rxhttp'   //非必须，指定RxHttp相关类的生成路径，即包名
                ]
            }
        }
    }
    //必须，java 8或更高
    compileOptions {
        sourceCompatibility JavaVersion.VERSION_1_8
        targetCompatibility JavaVersion.VERSION_1_8
    }
}
dependencies {
    //以下3个为必须
    kapt 'com.ljx.rxhttp:rxhttp-compiler:2.2.8'
    implementation 'com.ljx.rxhttp:rxhttp:2.2.8'
    implementation "com.squareup.okhttp3:okhttp:{okhttp_version}"

    //以下均为可选
    //管理协程生命周期，页面销毁，关闭请求
    implementation 'com.ljx.rxlife:rxlife-coroutine:2.0.0'

    //rxjava2   (RxJava2/Rxjava3二选一，使用asXxx方法时必须)
    implementation 'io.reactivex.rxjava2:rxjava:2.2.8'
    implementation 'io.reactivex.rxjava2:rxandroid:2.1.1'
    //管理RxJava2生命周期，页面销毁，关闭请求
    implementation 'com.ljx.rxlife2:rxlife-rxjava:2.0.0'

    //rxjava3
    implementation 'io.reactivex.rxjava3:rxjava:3.0.2'
    implementation 'io.reactivex.rxjava3:rxandroid:3.0.0'
    //管理RxJava3生命周期，页面销毁，关闭请求
    implementation 'com.ljx.rxlife3:rxlife-rxjava:3.0.0'

    //非必须，根据自己需求选择 RxHttp默认内置了GsonConverter
    implementation 'com.ljx.rxhttp:converter-jackson:2.2.8'
    implementation 'com.ljx.rxhttp:converter-fastjson:2.2.8'
    implementation 'com.ljx.rxhttp:converter-protobuf:2.2.8'
    implementation 'com.ljx.rxhttp:converter-simplexml:2.2.8'
}
```

2、Initialize the SDK

```java
//开启调式模式，默认false (可选)
RxHttp.setDebug(boolean)
//配置OkHttpClient对象 (可选)
RxHttp.init(OkHttpClient)
```

3、配置默认BaseUrl

```java
public class Url {

    //添加@DefaultDomain注解到BASE_URL上(可选)
    @DefaultDomain
    public static BASE_URL = "https://..."
}
```

4、执行请求

```java
// java 环境
RxHttp.get("/service/...")   //1、选择请求方法，可选get/postFrom等等
    .asClass(Student.class)  //2、使用asXxx方法，确定返回值类型，可自定义
    .subscribe(student -> {  //3、订阅观察者
        //成功回调，在子线程工作
    }, throwable -> {
        //失败回调
    });

// kotlin 环境
RxHttp.get("/service/...")   //1、选择请求方法，可选get/postFrom等等
    .asClass<Student>()      //2、使用asXxx方法，确定返回值类型，可自定义
    .subscribe({ student ->  //3、订阅观察者

    }, { throwable ->

    })

// kotlin 协程环境
val student = RxHttp
    .get("/service/...")     //1、选择请求方法，可选get/postFrom等等
    .toClass<Student>()      //2、使用toXxx方法，确定返回值类型，可自定义
    .await()                 //3、获取返回值，await是挂断方法
```

更多可查看请求时序图

![image](https://github.com/liujingxing/okhttp-RxHttp/blob/master/screen/rxhttp_trilogy.jpg)

## 3、Advanced usage

 1、关闭请求

```java
//在RxJava2中，自动关闭请求
RxHttp.get("/service/...")
    .asString()
    .as(RxLife.as(this))  //Activity销毁，自动关闭请求
    .subscribe(s -> {
        //成功回调
    }, throwable -> {
        //失败回调
    });

//在RxJava3中，自动关闭请求
RxHttp.get("/service/...")
    .asString()
    .to(RxLife.to(this))  //Activity销毁，自动关闭请求
    .subscribe(s -> {
        //成功回调
    }, throwable -> {
        //失败回调
    });

//注意：this可以是LifecycleOwner or View

//在RxJava2/RxJava3中，手动关闭请求
Disposable disposable = RxHttp.get("/service/...")
    .asString()
    .subscribe(s -> {
        //成功回调
    }, throwable -> {
        //失败回调
    });

disposable.dispose(); //在合适的时机关闭请求
```

## 4、ProGuard

`RxHttp v2.2.8`版本起，无需添加混淆规则(内部自带混淆规则)，v2.2.8以下版本，在proguard-rules.pro文件添加以下代码

```bash
# okhttp 4.7.0及以上版本混淆规则
-keepclassmembers class okhttp3.internal.Util {
    public static java.lang.String userAgent;
}

# okhttp 4.7.0以下版本混淆规则
-keepclassmembers class okhttp3.internal.Version{
    # 4.0.0<=version<4.7.0
    public static java.lang.String userAgent;
    # version<4.0.0
    public static java.lang.String userAgent();
}
# okhttp 4.0.0以下版本混淆规则
-keepclassmembers class okhttp3.internal.http.StatusLine{
    public static okhttp3.internal.http.StatusLine parse(java.lang.String);
}
```

## 5、Donations

如果它对你帮助很大，并且你很想支持库的后续开发和维护，那么你可以扫下方二维码随意打赏我，就当是请我喝杯咖啡或是啤酒，开源不易，感激不尽

![image](https://github.com/liujingxing/RxHttp/blob/master/screen/rxhttp_donate.png)

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
