# HttpSender
OkHttp+RxJava 超好用的Http请求框架，支持任意Http请求方式，如：Get、Post、Head、Put等；也支持任意数据解析方法，如：Json、DOM解析等；并且可以很优雅的实现上传/下载进度的监听

Gradle引用方法

    dependencies {
       implementation 'com.http.wrapper:httpsender:1.0.1'
    }
Get请求
  
        String url = "http://ip.taobao.com/service/getIpInfo.php";
        Param param = Param.get(url) //这里get,代表Get请求
                .add("ip", "63.223.108.42")//添加参数
                .addHeader("accept", "*/*") //添加请求头
                .addHeader("connection", "Keep-Alive")
                .addHeader("user-agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
        Disposable disposable = HttpSender
                .from(param, new DataParser<Address>() {}) //from操作符，是异步操作
                .observeOn(AndroidSchedulers.mainThread()) //主线程回调
                .subscribe(address -> {
                    //accept方法参数类型由上面DataParser传入的泛型类型决定
                    //走到这里说明Http请求成功，并且数据正确
                }, throwable -> {
                    //Http请求出现异常，有可能是网络异常，数据异常等
                });
        
Post请求

        String url = "http://ip.taobao.com/service/getIpInfo.php";
        Param param = Param.postForm(url) //这里get,代表Get请求
                .add("ip", "63.223.108.42")//添加参数
                .addHeader("accept", "*/*") //添加请求头
                .addHeader("connection", "Keep-Alive")
                .addHeader("user-agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
        Disposable disposable = HttpSender
                .from(param, new DataParser<Address>() {}) //from操作符，是异步操作
                .observeOn(AndroidSchedulers.mainThread()) //主线程回调
                .subscribe(address -> {
                    //accept方法参数类型由上面DataParser传入的泛型类型决定
                    //走到这里说明Http请求成功，并且数据正确
                }, throwable -> {
                    //Http请求出现异常，有可能是网络异常，数据异常等
                });
上传文件

        String url = "http://www.......";
        Param param = Param.postForm(url) //发送Form表单形式的Post请求
                .add("file1", new File("xxx/1.png"))
                .add("file2", new File("xxx/2.png"))
                .add("key1", "value1")//添加参数，非必须
                .add("key2", "value2")//添加参数，非必须
                .addHeader("versionCode", "100"); //添加请求头,非必须
        Disposable disposable = HttpSender
                .from(param, new SimpleParser<String>() {}) //注:如果需要监听上传进度，使用upload操作符
                .subscribe(s -> { //s为String类型，由SimpleParser类里面的泛型决定的
                    //上传成功，处理相关逻辑
                }, throwable -> {
                    //上传失败，处理相关逻辑
                });
                

上传文件进度监听

        String url = "http://www.......";
        Param param = Param.postForm(url) //发送Form表单形式的Post请求
                .add("file1", new File("xxx/1.png"))
                .add("file2", new File("xxx/2.png"))
                .add("key1", "value1")//添加参数，非必须
                .add("key2", "value2")//添加参数，非必须
                .addHeader("versionCode", "100"); //添加请求头,非必须
        Disposable disposable = HttpSender
                .upload(param, new SimpleParser<String>() {}) //注:如果需要监听上传进度，使用upload操作符
                .observeOn(AndroidSchedulers.mainThread()) //主线程回调
                .doOnNext(progress -> {
                    //上传进度回调,0-100，仅在进度有更新时才会回调
                    int currentProgress = progress.getProgress(); //当前进度 0-100
                    long currentSize = progress.getCurrentSize(); //当前已上传的字节大小
                    long totalSize = progress.getTotalSize();     //要上传的总字节大小
                })
                .filter(Progress::isCompleted)//过滤事件，上传完成，才继续往下走
                .map(Progress::getResult) //到这，说明下载完成，拿到Http返回结果并继续往下走
                .subscribe(s -> { //s为String类型，由SimpleParser类里面的泛型决定的
                    //上传成功，处理相关逻辑
                }, throwable -> {
                    //上传失败，处理相关逻辑
                });
                
                
下载文件

        String url = "http://update.9158.com/miaolive/Miaolive.apk";
        String destPath = getExternalCacheDir() + "/" + System.currentTimeMillis() + ".apk";
        Param param = Param.get(url); //这里get,代表Get请求
        Disposable disposable = HttpSender
                .from(param, new DownloadParser(destPath) {}) //这里使用DownloadParser解析器
                .observeOn(AndroidSchedulers.mainThread()) //主线程回调
                .subscribe(s -> { //s为String类型
                    //下载成功，处理相关逻辑
                }, throwable -> {
                    //下载失败，处理相关逻辑
                });

下载文件进度监听

        String url = "http://update.9158.com/miaolive/Miaolive.apk";
        String destPath = getExternalCacheDir() + "/" + System.currentTimeMillis() + ".apk";
        Param param = Param.get(url); //这里get,代表Get请求
        Disposable disposable = HttpSender
                .download(param, destPath) //下载进度监听，使用download操作符
                .observeOn(AndroidSchedulers.mainThread()) //主线程回调
                .doOnNext(progress -> {
                    //下载进度回调,0-100，仅在进度有更新时才会回调
                    int currentProgress = progress.getProgress(); //当前进度 0-100
                    long currentSize = progress.getCurrentSize(); //当前已下载的字节大小
                    long totalSize = progress.getTotalSize();     //要下载的总字节大小
                })
                .filter(Progress::isCompleted)//过滤事件，下载完成，才继续往下走
                .map(Progress::getResult) //到这，说明下载完成，拿到Http返回结果并继续往下走
                .subscribe(s -> { //s为String类型
                    //下载成功，处理相关逻辑
                }, throwable -> {
                    //下载失败，处理相关逻辑
                });
