package com.example.httpsender;


import android.app.Application;

import java.io.File;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import rxhttp.RxHttpPlugins;
import rxhttp.wrapper.annotation.Converter;
import rxhttp.wrapper.annotation.OkClient;
import rxhttp.wrapper.callback.IConverter;
import rxhttp.wrapper.converter.FastJsonConverter;
import rxhttp.wrapper.converter.XmlConverter;
import rxhttp.wrapper.cookie.CookieStore;
import rxhttp.wrapper.param.Method;
import rxhttp.wrapper.ssl.HttpsUtils;
import rxhttp.wrapper.ssl.HttpsUtils.SSLParams;

/**
 * 本类所有配置都是非必须的，根据自己需求选择就好
 * User: ljx
 * Date: 2019-11-26
 * Time: 20:44
 */
public class RxHttpManager {

    @Converter(name = "XmlConverter")  //非必须
    public static IConverter xmlConverter = XmlConverter.create();
    @Converter(name = "FastJsonConverter")  //非必须
    public static IConverter fastJsonConverter = FastJsonConverter.create();

    @OkClient(name = "SimpleClient", className = "Simple")  //非必须
    public static OkHttpClient simpleClient = new OkHttpClient.Builder().build();


    public static void init(Application context) {
        File file = new File(context.getExternalCacheDir(), "RxHttpCookie");
        SSLParams sslParams = HttpsUtils.getSslSocketFactory();
        OkHttpClient client = new OkHttpClient.Builder()
            .cookieJar(new CookieStore(file))
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .writeTimeout(10, TimeUnit.SECONDS)
            .sslSocketFactory(sslParams.sSLSocketFactory, sslParams.trustManager) //添加信任证书
            .hostnameVerifier((hostname, session) -> true) //忽略host验证
//            .followRedirects(false)  //禁制OkHttp的重定向操作，我们自己处理重定向
//            .addInterceptor(new RedirectInterceptor())
//            .addInterceptor(new TokenInterceptor())
            .build();

        //设置缓存策略，非必须
//        File cacheFile = new File(context.getExternalCacheDir(), "RxHttpCache");
        //RxHttp初始化，非必须
        RxHttpPlugins.init(client)                     //自定义OkHttpClient对象
            .setDebug(BuildConfig.DEBUG, false, 2)      //调试模式/分段打印/json数据缩进空间
//            .setCache(cacheFile, 1000 * 100, CacheMode.REQUEST_NETWORK_FAILED_READ_CACHE)
//            .setExcludeCacheKeys("time")               //设置一些key，不参与cacheKey的组拼
//            .setResultDecoder(s -> s)                  //设置数据解密/解码器，非必须
//            .setConverter(FastJsonConverter.create())  //设置全局的转换器，非必须
            .setOnParamAssembly(p -> {                 //设置公共参数，非必须
                //1、可根据不同请求添加不同参数，每次发送请求前都会被回调
                //2、如果希望部分请求不回调这里，发请求前调用RxHttp#setAssemblyEnabled(false)即可
                Method method = p.getMethod();
                if (method.isGet()) {
                    p.add("method", "get");
                } else if (method.isPost()) { //Post请求
                    p.add("method", "post");
                }
                return p.add("versionName", "1.0.0")//添加公共参数
                    .add("time", System.currentTimeMillis())
                    .addHeader("deviceType", "android"); //添加公共请求头
            });
    }
}
