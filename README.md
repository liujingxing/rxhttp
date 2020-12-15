# RxHttp

English | [中文文档](https://github.com/liujingxing/okhttp-RxHttp/blob/master/README_zh.md)

[ ![Download](https://api.bintray.com/packages/32774707/maven/rxhttp2/images/download.svg) ](https://bintray.com/32774707/maven/rxhttp2/_latestVersion)

A type-safe HTTP client for Android. Written based on OkHttp


## 1、Feature

- Support kotlin coroutines, RxJava2, RxJava3

- Support Gson, Xml, ProtoBuf, FastJson and other third-party data parsing tools

- Supports automatic closure of requests in FragmentActivity, Fragment, View, ViewModel, and any class

- Support global encryption and decryption, add common parameters and headers, network cache, all support a request set up separately

## 2、usage

1、Adding dependencies and configurations

### Required
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
    implementation 'com.ljx.rxhttp:rxhttp:2.5.1'
    implementation 'com.squareup.okhttp3:okhttp:4.9.0' 
    kapt 'com.ljx.rxhttp:rxhttp-compiler:2.5.1' //Use the annotationProcessor instead of kapt, if you use Java
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
                    rxhttp_rxjava: 'rxjava3'，
                    rxhttp_package: 'rxhttp'   //Specifies the RxHttp class package
                ]
            }
        }
    }
}
dependencies {
    implementation 'com.ljx.rxlife:rxlife-coroutine:2.0.1' //Coroutine, Automatic close request

    //rxjava2   (RxJava2/Rxjava3 select one)
    implementation 'io.reactivex.rxjava2:rxjava:2.2.8'
    implementation 'io.reactivex.rxjava2:rxandroid:2.1.1'
    implementation 'com.ljx.rxlife2:rxlife-rxjava:2.0.0' //RxJava2, Automatic close request

    //rxjava3
    implementation 'io.reactivex.rxjava3:rxjava:3.0.6'
    implementation 'io.reactivex.rxjava3:rxandroid:3.0.0'
    implementation 'com.ljx.rxlife3:rxlife-rxjava:3.0.0' //RxJava3, Automatic close request

    implementation 'com.ljx.rxhttp:converter-fastjson:2.5.1'
    implementation 'com.ljx.rxhttp:converter-jackson:2.5.1'
    implementation 'com.ljx.rxhttp:converter-moshi:2.5.1'
    implementation 'com.ljx.rxhttp:converter-protobuf:2.5.1'
    implementation 'com.ljx.rxhttp:converter-simplexml:2.5.1'
}
```

**Finally, rebuild the project, which is necessary**

2、Initialize the SDK

This step is optional

```java
RxHttp.setDebug(boolean)  
RxHttp.init(OkHttpClient)  
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
RxHttp.get("/service/...")   //1、You can choose get/postFrom/postJson and so on
    .addQuery("key", "value")  // add query param
    .addHeader("headerKey", "headerValue")  //add request header
    .asClass(Student.class)  //2、Use the asXxx method to determine the return value type, customizable
    .subscribe(student -> {  //3、Subscribing observer
        //成功回调，在子线程工作
    }, throwable -> {
        //失败回调
    });

// kotlin 
RxHttp.postFrom("/service/...")   //1、You can choose get/postFrom/postJson and so on
    .add("key", "value")                 //add param to body
    .addQuery("key1", "value1")          //add query param
    .addFile("file", File(".../1.png"))  //add file to body
    .asClass<Student>()           //2、Use the asXxx method to determine the return value type, customizable
    .subscribe({ student ->       //3、Subscribing observer

    }, { throwable ->

    })

// kotlin coroutine
val student = RxHttp.get("/service/...")  //1、You can choose get/postFrom/postJson and so on
    .toClass<Student>()                   //2、Use the toXxx method to determine the return value type, customizable
    .await()                              //3、Get the return value, await is the suspend method
```

See the request timing diagram for more

![image](https://github.com/liujingxing/okhttp-RxHttp/blob/master/screen/rxhttp_sequence_chart.jpg)

## 3、Advanced usage

 1、Close the request

```java
//In Rxjava2 , Automatic close request
RxHttp.get("/service/...")
    .asString()
    .as(RxLife.as(this))  //The Activity destroys and automatically closes the request
    .subscribe(s -> {
        //Success callback
    }, throwable -> {
        //Abnormal callback
    });

//In Rxjava3 , Automatic close request
RxHttp.get("/service/...")
    .asString()
    .to(RxLife.to(this))  //The Activity destroys and automatically closes the request
    .subscribe(s -> {
        //Success callback
    }, throwable -> {
        //Abnormal callback
    });


//In RxJava2/RxJava3, close the request manually
Disposable disposable = RxHttp.get("/service/...")
    .asString()
    .subscribe(s -> {
        //Success callback
    }, throwable -> {
        //Abnormal callback
    });

disposable.dispose(); //Close the request at the appropriate time
```

## 4、ProGuard

If you are using RxHttp v2.2.8 or above the shrinking and obfuscation rules are included automatically.
Otherwise you must manually add the options in [rxhttp.pro](https://github.com/liujingxing/okhttp-RxHttp/blob/master/rxhttp/src/main/resources/META-INF/proguard/rxhttp.pro).

## 5、Donations

If this project helps you a lot and you want to support the project's development and maintenance of this project, feel free to scan the following QR code for donation. Your donation is highly appreciated. Thank you!

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
