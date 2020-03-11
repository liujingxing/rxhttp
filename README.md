[ ![Download](https://api.bintray.com/packages/32774707/maven/rxhttp/images/download.svg) ](https://bintray.com/32774707/maven/rxhttp/_latestVersion)

# RxHttp主要优势

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

**Gradle依赖**

```java
dependencies {

   implementation 'com.rxjava.rxhttp:rxhttp:0.0.8' //必须
   annotationProcessor 'com.rxjava.rxhttp:rxhttp-compiler:0.0.8' //注解处理器，生成RxHttp类,必须
   implementation 'io.reactivex.rxjava2:rxandroid:2.1.1'  //切换主线程，Android工程必须

   implementation 'com.rxjava.rxlife:rxlife:1.1.0'  //页面销毁，关闭请求，非必须
}
```

`注：`

- kotlin版本主要增加了对协程的支持，目前处于内测阶段

- kotlin用户，请使用kapt替代annotationProcessor

***RxHttp&RxLife 交流群：378530627***

# 改动

较主分支v1.4.4改动如下：

- 新增一系列`suspend <T> awaitXxx(): T` 方法，用于对协程的支持，通过该系列方法，可直接拿到http返回值

- 新增：对于之前要传`Classs<T>`参数的asXxx方法，新增了与之对应同名的无参方法，如：`T asObject<T>()`

- 修改`setRangeHeader(long,long)`方法签名为`setRangeHeader(long,long,boolean)`，其中第三个参数代表在带进度断点下载时，是否需要衔接上次的下载进度，默认为false

- 删除`asDownload(String,long,Consumer,Scheduler)`方法，但保留了`asDownload(String,Consumer,Scheduler)`方法，删除该方法后，带进度断点下载时，如果需要衔接上次的下载进度，可通过`setRangeHeader(long,long,boolean)`方法的第三个参数进行配置

- 修改：对RxHttp子类的命名方式由之前的`RxHttp$Xxx`，改为`RxHttp_Xxx`，因为kotlin不允许用`$`符号命名

- 修改：`rxhttp-compiler`库全部用kotlin改写，`rxhttp`库对常用类、关键类用kotlin改写



# 协程的优势

协程最大的优势：

- 能以看起来同步的代码，写出异步的逻辑



这使得我们可以非常优雅的实现多任务的并行/串行，比如多请求的并行/串行



# RxHttp协程使用



RxHttp kotlin版本之前，我们知道有请求三部曲，如下：

```java
RxHttp.get("/service/...") //第一步，确定请求方式，可以选择postForm、postJson等方法
    .asString()            //第二步，使用asXXX系列方法确定返回类型
    .subscribe(s -> {      //第三步, 订阅观察者
        //成功回调
    }, throwable -> {
        //失败回调
    });
```

而协程发送请求，只需要两步即可，如下：

```java
val resultStr = RxHttp.get("/service/...") //第一步，确定请求方式，可以选择postForm、postJson等方法
                    .awaitString() //第二步，使用awaitXxx系列方法直接拿到返回值

```

可以看到，相较于请求三部曲，我们只需要将第二部中的`asXxx`方法替换为`awaitXxx`方法即可，通过awaitXxx方法，便可直接拿到请求返回值。



下面给大家罗列下RxHttp都提供了哪些awaitXxx方法

```java
suspend awaitString()
suspend awaitBoolean()
suspend awaitByte()
suspend awaitShort()
suspend awaitInt()
suspend awaitLong()
suspend awaitFloat()
suspend awaitDouble()
suspend awaitBitmap()
suspend awaitHeaders()
suspend awaitOkResponse()    //以上方法与asXxx方法一一对应
suspend <T> await<T>()       //对应asObject(Class<T>),直接返回T对象
suspend <T> awaitList<T>()   //对应asList(Class<T>),直接返回List<T>对象
suspend <K,V> awaitMap<K,V>() //对应asMap(Class<K>,Class<V>),直接返回Map<K,V>对象
suspend <T> await(Parser<T>)  //对应asParser(Parser<T>)方法，直接返回T对象

//以下三个方法用于对文件上传/下载进度的监听，后面会详细介绍
suspend awaitDownload(String, CoroutineScope? = null,(Progress<String>) -> Unit)
suspend awaitUpload<T>(CoroutineScope? = null，(Progress<T>) -> Unit)
suspend awaitUpload<T>(Parser<T>, CoroutineScope? = null，(Progress<T>) -> Unit)
```

#### ***划重点***

以上awaitXxx方法，大家可以重点记住`await<T>()`方法即可，该方法是万能的，可拿到任意数据类型，大多数awaitXxx都是基于此方法实现的



到这，有人有疑问了，`awaitXxx`方法，都是直接拿到返回值的，那有异常怎么处理？手写try catch语句捕获？当然不是。眼尖的同学可能注意到了，`awaitXxx`方法前面都有一个`suspend`关键字修饰，被`suspend`关键字修饰的方法被称为挂断方法，而挂断方法只能运行在协程或者另一个挂断方法中（更多关于挂断方法的知识，请自行百度，这里不过多介绍）。



## 协程发送单个请求

直接上代码，看看如何启动协程，调用awaitXxx方法发送请求，如下：

```java
class MainActivity : AppCompatActivity() {

    //启动协程，发送请求
    fun sendRequest() {
        RxHttpScope(this)  //此this为LifecycleOwner接口对象，用于页面销毁时，自动关闭协程及请求
            .launch({
                //当前运行在协程中，且在主线程运行
                val student = getStudent()
                //拿到相关信息后，便可直接更新UI，如：
                tvName.text = student.name
            }, {
                //出现异常，就会到这里，这里的it为Throwable类型
                it.show("发送失败,请稍后再试!")  //show方法是在Demo中扩展的方法
            })
    }

    //注意这是挂断方法
    suspend fun getStudent(): Student {
        return RxHttp.get("/service/...")
            .add("key", "value")
            .addHeader("headKey", "headValue")
            .await() //由于方法指明了返回值为Student类型，故直接写await()，否则需要写 await<Student>()
    }

}
```

`注:RxHttpScope类是我自己封装的一个类，主要作用是启动协程/自动关闭协程/异常捕获，目前该类并未封装到RxHttp内部，如有需要，可在Demo中获取`



## 协程串行多个请求

上面发送单个请求，其实还看不出协程的魅力所在，因为它还没有RxHttp直接发送一个请求优雅，但遇到多个请求串行情况时，RxHttp就显得有些乏力。



直接来看看通过协程如何解决这个问题，如下：

```java
class MainActivity : AppCompatActivity() {
    //启动协程，发送请求
    fun sendRequest() {
        RxHttpScope(this) //此this为LifecycleOwner接口对象，用于页面销毁时，自动关闭协程及请求
            .launch({
                //当前运行在协程中，且在主线程运行
                val student = getStudent()
                val personList = getFamilyPersons(student.id) //通过学生Id，查询家庭成员信息
                //拿到相关信息后，便可直接更新UI，如：
                tvName.text = student.name
            }, {
                //出现异常，就会到这里，这里的it为Throwable类型
                it.show("发送失败,请稍后再试!") //show方法是在Demo中扩展的方法
            })
    }

    //挂断方法，获取学生信息
    suspend fun getStudent(): Student {
        return RxHttp.get("/service/...")
            .add("key", "value")
            .addHeader("headKey", "headValue")
            .await() //由于方法指明了返回值为Student类型，故直接写await()，否则需要写 await<Student>()
    }

    //挂断方法，获取家庭成员信息
    suspend fun getFamilyPersons(studentId: Int): List<Person> {
        return RxHttp.get("/service/...")
            .add("studentId", "studentId")
            .await() //由于方法指明了返回值为List<Person>类型，故直接写await()，否则需要写 await<List<Person>>()
    }
}
```

我们重点看下协程代码块，首先通过第一个请求拿到Student对象，随后拿到studentId，发送第二个请求获取学习家庭成员信息，拿到后，便可以直接更新UI，怎么样，是不是看起来同步的代码，写出了异步的逻辑。



串行请求中，只要其中一个请求出现异常，协程便会关闭（也会关闭未完成的请求），停止执行剩下的代码，接着走异常回调



## 协程并行多个请求

请求并行，在现实开发中，也是家常便饭，在一个Activity中，我们往往需要拿到多种数据来展示给用户，而这些数据，都是不同接口下发的。



如我们有这样一个页面，顶部是横向滚动的Banner条，Banner条下面展示学习列表，此时就有两个接口，一个获取Banner条列表，一个获取学习列表，它们两个互不依赖，便可以并行执行，如下：

```java
class MainActivity : AppCompatActivity() {
    //启动协程，发送请求
    fun sendRequest() {
        RxHttpScope(this) //此this为LifecycleOwner接口对象，用于页面销毁时，自动关闭协程及请求
            .launch({
                //当前运行在协程中，且在主线程运行
                val asyncBanner = async { getBanners() }    //这里返回Deferred<List<Banner>>对象
                val asyncPersons = async { getStudents() }  //这里返回Deferred<List<Student>>对象
                val banners = asyncBanner.await()           //这里返回List<Banner>对象
                val students = asyncPersons.await()         //这里返回List<Student>对象
                //开始更新UI

            }, {
                //出现异常，就会到这里，这里的it为Throwable类型
                it.show("发送失败,请稍后再试!") //show方法是在Demo中扩展的方法
            })
    }

    //挂断方法，获取学生信息
    suspend fun getBanners(): List<Banner> {
        return RxHttp.get("/service/...")
            .add("key", "value")
            .addHeader("headKey", "headValue")
            .await() //由于方法指明了返回值为Student类型，故这里直接调用await()，否则需要写 .await<Student>()
    }

    //挂断方法，获取家庭成员信息
    suspend fun getStudents(): List<Student> {
        return RxHttp.get("/service/...")
            .add("key", "value")
            .await() //由于方法指明了返回值为List<Person>类型，故这里直接调用await()，否则需要写 .await<List<Person>>()
    }
}
```

老规矩，重点看协程代码块，在该协程中，我们通过async方法，又开启了两个协程，此时这两个协程就并行发送请求，随后拿到`Deferred<T>`对象，调用其`await()`方法，最终拿到Banner列表及Student列表，最后便可以直接更新UI。



并行跟串行一样，如果其中一个请求出现了异常，协程便会自动关闭（也会关闭为完成的请求），停止执行剩下的代码，接着走异常回调。



## 协程文件上传/下载

### 文件上传

```java
class MainActivity : AppCompatActivity() {

    //启动协程，发送请求
    fun sendRequest() {
        RxHttpScope(this) //此this为LifecycleOwner接口对象，用于页面销毁时，自动关闭协程及请求
            .launch({
                //当前运行在协程中，且在主线程运行，故下面传this，让进度回调在主线程执行
                val result = uploadFile(this) {
                    //这里最多回调100此，进在进度有更新时才回调
                    val progress = it.progress       //当前上传进度
                    val currentSize = it.currentSize //当前已上传的size
                    val totalSize = it.totalSize     //要上传的总size
                    //当前在主线程，可直接更新UI
                }
                //请求介绍，当前在主线程，可直接更新UI
            }, {
                //出现异常，就会到这里，这里的it为Throwable类型
                it.show("发送失败,请稍后再试!") //show方法是在Demo中扩展的方法
            })
    }

    /**
     * @param coroutine CoroutineScope对象，用于切换进度回调线程，默认为null，即在子线程回调进度
     * @param progress 进度回调
     */
    suspend fun uploadFile(
        coroutine: CoroutineScope? = null,
        progress: (Progress<String>) -> Unit): String {
        return RxHttp.postForm("/service/...")
            .add("key", "value")
            .addFile("file", File(".../1.phg"))
            .addFile("file1", File(".../2.phg"))
            .awaitUpload(coroutine, progress)
    }
}
```



### 文件下载

```java
class MainActivity : AppCompatActivity() {

    //启动协程，发送请求
    fun sendRequest() {
        RxHttpScope(this) //此this为LifecycleOwner接口对象，用于页面销毁时，自动关闭协程及请求
            .launch({
                //当前运行在协程中，且在主线程运行，故下面传this，让进度回调在主线程执行
                val result = downloadFile(".../1.pak", this) {
                    //这里最多回调100此，进在进度有更新时才回调
                    val progress = it.progress       //当前上传进度
                    val currentSize = it.currentSize //当前已上传的size
                    val totalSize = it.totalSize     //要上传的总size
                    //当前在主线程，可直接更新UI
                }
                //当前在主线程，可直接更新UI
            }, {
                //出现异常，就会到这里，这里的it为Throwable类型
                it.show("发送失败,请稍后再试!") //show方法是在Demo中扩展的方法
            })
    }

    /**
     * @param destPath 存储路径，传入本地文件目录
     * @param coroutine CoroutineScope对象，用于切换进度回调线程，默认为null，即在子线程回调进度
     * @param progress 进度回调
     */
    suspend fun downloadFile(
        destPath: String,
        coroutine: CoroutineScope? = null,
        progress: (Progress<String>) -> Unit): String {
        return RxHttp.postForm("/service/...")
            .add("key", "value")
            .awaitDownload(destPath, coroutine, progress)
    }
}
```

可以看到，带进度文件上传/下载大同小异，区别有两个

- 文件下载需要多个本地文件路径，用于存在下载的文件

- 文件上传有返回值，我们可以通过`awaitUpload<T>`指定返回类型，而下载返回的就是下载成功后本地存储路径。



## awaitXxx和asXxx怎么选？



这两类方法，都可以非常方便的获取任意数据类型，区别在于，前者依赖协程，我们得单独开启协程取执行它，而后者我们只需要订阅回调即可，但是协程在处理请求串行/并行，是非常优雅的，因此，我的习惯是

- 对于单请求，我更喜欢asXxx方法

- 对于请求串行/并行，我更喜欢使用协程+awaitXxx方法



## 小彩蛋

前面，我说了，对于之前要传`Classs<T>`参数的asXxx方法，新增了与之对应同名的无参方法，如：`T asObject<T>()`，那这些方法该怎么用？如下：

```java
class MainActivity : AppCompatActivity() {

    fun requestStudent() {
        postForm("/article/query/0/json")
            .add("k", null)
            .asObject<Student>()
            .lifeOnMain(this) //感知生命周期，并在主线程回调
            .subscribe({
                val student = it //这里拿到Student对象
            }, {
                //失败回调
            })
    }

    fun requestStudents() {
        postForm("/article/query/0/json")
            .add("k", null)
            .asObject<List<Student>>()
            .lifeOnMain(this) //感知生命周期，并在主线程回调
            .subscribe({
                val students = it //这里拿到List<Student>对象
            }, {
                //失败回调
            })
    }

    fun getStudents(view: View) {
        postForm("/article/query/0/json")
            .add("k", null)
            .asResponse<List<Student>>()    //自定义的asXxx也支持
            .lifeOnMain(this) //感知生命周期，并在主线程回调
            .subscribe({

            }, {

            })
    }
}
```

更多`asXxx<T>()`方法等你发现

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
