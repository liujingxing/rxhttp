# RxHttp

English | [中文文档](https://github.com/liujingxing/rxhttp/blob/master/README_zh.md)

[![](https://jitpack.io/v/liujingxing/rxhttp.svg)](https://jitpack.io/#liujingxing/rxhttp)

A type-safe HTTP client for Android. Written based on OkHttp


## 1、Feature

- Support kotlin coroutines, RxJava2, RxJava3

- Support Gson, Xml, ProtoBuf, FastJson and other third-party data parsing tools

- Supports automatic closure of requests in FragmentActivity, Fragment, View, ViewModel, and any class

- Support global encryption and decryption, add common parameters and headers, network cache, all support a request set up separately

## 2、usage

1、Adding dependencies and configurations

### Required

Add it to your build.gradle with:
```java
allprojects {
    repositories {
        maven { url "https://jitpack.io" }
    }
}
```
and:

```java
//Must be used when using kapt
apply plugin: 'kotlin-kapt'

android {
    //Java 8 or higher
    compileOptions {
        sourceCompatibility JavaVersion.VERSION_1_8
        targetCompatibility JavaVersion.VERSION_1_8
    }
}

dependencies {
    implementation 'com.github.liujingxing.rxhttp:rxhttp:2.6.2'
    implementation 'com.squareup.okhttp3:okhttp:4.9.1' 
    kapt 'com.github.liujingxing.rxhttp:rxhttp-compiler:2.6.2' //Use the annotationProcessor instead of kapt, if you use Java
 }
```

### Optional
```java
android {
    defaultConfig {
        javaCompileOptions {
            annotationProcessorOptions {
                arguments = [
                    //Pass in RxJava version, can pass in RxJava2, RxJava3
                    rxhttp_rxjava: 'rxjava3',
                    rxhttp_package: 'rxhttp'   //Specifies the RxHttp class package
                ]
            }
        }
    }
}
dependencies {
    implementation 'com.github.liujingxing.rxlife:rxlife-coroutine:2.1.0' //Coroutine, Automatic close request

    //rxjava2   (RxJava2/Rxjava3 select one)
    implementation 'io.reactivex.rxjava2:rxjava:2.2.8'
    implementation 'io.reactivex.rxjava2:rxandroid:2.1.1'
    implementation 'com.github.liujingxing.rxlife:rxlife-rxjava2:2.1.0' //RxJava2, Automatic close request

    //rxjava3
    implementation 'io.reactivex.rxjava3:rxjava:3.0.6'
    implementation 'io.reactivex.rxjava3:rxandroid:3.0.0'
    implementation 'com.github.liujingxing.rxlife:rxlife-rxjava3:2.1.0' //RxJava3, Automatic close request

    implementation 'com.github.liujingxing.rxhttp:converter-fastjson:2.6.2'
    implementation 'com.github.liujingxing.rxhttp:converter-jackson:2.6.2'
    implementation 'com.github.liujingxing.rxhttp:converter-moshi:2.6.2'
    implementation 'com.github.liujingxing.rxhttp:converter-protobuf:2.6.2'
    implementation 'com.github.liujingxing.rxhttp:converter-simplexml:2.6.2'
}
```

**Finally, rebuild the project, which is necessary**

2、Initialize the SDK

This step is optional

```java
RxHttpPlugins.init(OkHttpClient)  
    .setDebug(boolean)  
    .setOnParamAssembly(Function)
    ....
```

3、Configuration BaseUrl

This step is optional

```java
public class Url {

    //Add the @defaultDomain annotation to BASE_URL
    @DefaultDomain
    public static BASE_URL = "https://..."
}
```

4、Perform the requested

```java
// java
RxHttp.get("/service/...")   //1、You can choose get,postFrom,postJson etc
    .addQuery("key", "value")               //add query param
    .addHeader("headerKey", "headerValue")  //add request header
    .asClass(Student.class)  //2、Use the asXxx method to determine the return value type, customizable
    .subscribe(student -> {  //3、Subscribing observer
        //Success callback，Default IO thread
    }, throwable -> {
        //Abnormal callback
    });

// kotlin 
RxHttp.postForm("/service/...")          //post FormBody
    .add("key", "value")                 //add param to body
    .addQuery("key1", "value1")          //add query param
    .addFile("file", File(".../1.png"))  //add file to body
    .asClass<Student>()           
    .subscribe({ student ->       
        //Default IO thread
    }, { throwable ->
        
    })

// kotlin coroutine
val students = RxHttp.postJson("/service/...")  //1、post {application/json; charset=utf-8}
    .toList<Student>()                          //2、Use the toXxx method to determine the return value type, customizable
    .await()                                    //3、Get the return value, await is the suspend method
```

See the request timing diagram for more

![sequence_chart_en.jpg](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/5c25c03999c4458d8cc79212cdfd34d5~tplv-k3u1fbpfcp-watermark.image)

## 3、Advanced usage

 1、Close the request

```java
//In Rxjava2 , Automatic close request
RxHttp.get("/service/...")
    .asString()
    .as(RxLife.as(this))  //The Activity destroys and automatically closes the request
    .subscribe(s -> {
        //Default IO thread
    }, throwable -> {

    });

//In Rxjava3 , Automatic close request
RxHttp.get("/service/...")
    .asString()
    .to(RxLife.to(this))  //The Activity destroys and automatically closes the request
    .subscribe(s -> {
        //Default IO thread
    }, throwable -> {
        
    });


//In RxJava2/RxJava3, close the request manually
Disposable disposable = RxHttp.get("/service/...")
    .asString()
    .subscribe(s -> {
        //Default IO thread
    }, throwable -> {
        
    });

disposable.dispose(); //Close the request at the appropriate time
```

## 4、ProGuard

If you are using RxHttp v2.2.8 or above the shrinking and obfuscation rules are included automatically.
Otherwise you must manually add the options in [rxhttp.pro](https://github.com/liujingxing/rxhttp/blob/master/rxhttp/src/main/resources/META-INF/proguard/rxhttp.pro).

## 5、Donations

If this project helps you a lot and you want to support the project's development and maintenance of this project, feel free to scan the following QR code for donation. Your donation is highly appreciated. Thank you!

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
