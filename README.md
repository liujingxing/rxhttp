# RxHttp

English | [中文文档](https://github.com/liujingxing/rxhttp/blob/master/README_zh.md)

[![](https://jitpack.io/v/liujingxing/rxhttp.svg)](https://jitpack.io/#liujingxing/rxhttp)

A type-safe HTTP client for Android. Written based on OkHttp


![sequence_chart_en.jpg](https://p9-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/2c637845930b49d7a7466ec7b5dcdb77~tplv-k3u1fbpfcp-watermark.image)
```kotlin
//Kotlin + Await             //Kotlin + Flow              //Kotlin + RxJava            //Java + RxJava
RxHttp.get("/server/..")     RxHttp.get("/server/..")     RxHttp.get("/server/..")     RxHttp.get("/server/..")   
    .add("key", "value")         .add("key", "value")         .add("key", "value")         .add("key", "value")
    .toClass<User>()             .toFlow<User>()              .asClass<User>()             .asClass(User.class)
    .awaitResult {               .catch {                     .subscribe({                 .subscribe(user -> {
        //Success                    //Failure                    //Success                    //Success     
    }.onFailure {                }.collect {                  }, {                         }, throwable -> { 
        //Failure                    //Success                    //Failure                    //Failure
    }                            }                            })                           });
```


## 1、Feature

- Support kotlin coroutines, RxJava2, RxJava3

- Support Gson, Xml, ProtoBuf, FastJson and other third-party data parsing tools

- Supports automatic closure of requests in FragmentActivity, Fragment, View, ViewModel, and any class

- Support global encryption and decryption, add common parameters and headers, network cache, all support a request set up separately

## 2、usage

1、Adding dependencies and configurations

### Required

<details>
<summary>1、Add jitpack to your build.gradle</summary>
 
```java
allprojects {
    repositories {
        maven { url "https://jitpack.io" }
    }
}
```
</details>
<details>
<summary>2、Java 8 or higher</summary>
 
```java
android {
    compileOptions {
        sourceCompatibility JavaVersion.VERSION_1_8
        targetCompatibility JavaVersion.VERSION_1_8
    }
}
```
</details>
<details open>
<summary>3、Add RxHttp dependency</summary>
 
```kotlin
plugins {
    // kapt/ksp choose one
    // id 'kotlin-kapt'
    id 'com.google.devtools.ksp' version '1.7.10-1.0.6'
}

//Make IDE aware of generated code if you use ksp
kotlin {
    sourceSets.debug {
        kotlin.srcDir("build/generated/ksp/debug/kotlin")
    }
}
    
dependencies {
    def rxhttp_version = '2.9.5'
    implementation 'com.squareup.okhttp3:okhttp:4.10.0'  
    implementation "com.github.liujingxing.rxhttp:rxhttp:$rxhttp_version"
    // ksp/kapt/annotationProcessor choose one
    ksp "com.github.liujingxing.rxhttp:rxhttp-compiler:$rxhttp_version"
 }
```
</details>

### Optional

### 1、Converter
```kotlin
implementation "com.github.liujingxing.rxhttp:converter-serialization:$rxhttp_version"
implementation "com.github.liujingxing.rxhttp:converter-fastjson:$rxhttp_version"
implementation "com.github.liujingxing.rxhttp:converter-jackson:$rxhttp_version"
implementation "com.github.liujingxing.rxhttp:converter-moshi:$rxhttp_version"
implementation "com.github.liujingxing.rxhttp:converter-protobuf:$rxhttp_version"
implementation "com.github.liujingxing.rxhttp:converter-simplexml:$rxhttp_version"
```

### 2、RxJava
<details open>
<summary>RxHttp + RxJava3</summary>
 
 ```java
implementation 'io.reactivex.rxjava3:rxjava:3.1.5'
implementation 'io.reactivex.rxjava3:rxandroid:3.0.0'
implementation 'com.github.liujingxing.rxlife:rxlife-rxjava3:2.2.2' //RxJava3, Automatic close request
```
 
</details>
<details>
<summary>RxHttp + RxJava2</summary>
 
```java
implementation 'io.reactivex.rxjava2:rxjava:2.2.8'
implementation 'io.reactivex.rxjava2:rxandroid:2.1.1'
implementation 'com.github.liujingxing.rxlife:rxlife-rxjava2:2.2.2' //RxJava2, Automatic close request
```
</details>

<details open>
<summary>ksp passes the RxJava version</summary>
 
```java
ksp {
    arg("rxhttp_rxjava", "3.1.5")
}
```
 
</details>

<details>
<summary>Kapt passes the RxJava version</summary>
 
```java
kapt {
    arguments {
        arg("rxhttp_rxjava", "3.1.5")
    }
}
```
 
</details>
 
<details>
<summary>javaCompileOptions passes the RxJava version</summary>
 
```java
android {
    defaultConfig {
        javaCompileOptions {
            annotationProcessorOptions {
                arguments = [
                    rxhttp_rxjava: '3.1.5', 
                ]
            }
        }
    }
}
```
 
</details>


### 3、set RxHttp class package name

<details open>
<summary>ksp pass package name</summary>
 
```java
ksp {
     arg("rxhttp_package", "rxhttp.xxx")
}
```
    
<details>
<summary>kapt pass package name</summary>
 
```java
kapt {
    arguments {
       arg("rxhttp_package", "rxhttp.xxx") 
    }
}
```
 
</details>
 
<details>
<summary>javaCompileOptions pass package name</summary>
 
```java
android {
    defaultConfig {
        javaCompileOptions {
            annotationProcessorOptions {
                arguments = [
                    rxhttp_package: 'rxhttp.xxx'
                ]
            }
        }
    }
}
```
</details>


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
