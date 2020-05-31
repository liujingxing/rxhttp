package com.example.httpsender;


import android.app.Application;

import java.io.File;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import rxhttp.RxHttpPlugins;
import rxhttp.wrapper.annotation.Converter;
import rxhttp.wrapper.annotation.OkClient;
import rxhttp.wrapper.cahce.CacheMode;
import rxhttp.wrapper.callback.IConverter;
import rxhttp.wrapper.converter.FastJsonConverter;
import rxhttp.wrapper.converter.XmlConverter;
import rxhttp.wrapper.cookie.CookieStore;
import rxhttp.wrapper.param.Method;
import rxhttp.wrapper.param.RxHttp;
import rxhttp.wrapper.ssl.HttpsUtils;
import rxhttp.wrapper.ssl.HttpsUtils.SSLParams;

/**
 * User: ljx
 * Date: 2019-11-26
 * Time: 20:44
 */
public class RxHttpManager {

    @Converter(name = "XmlConverter")
    public static IConverter xmlConverter = XmlConverter.create();
    @Converter(name = "FastJsonConverter", className = "Simple")
    public static IConverter fastJsonConverter = FastJsonConverter.create();

    @OkClient(name = "SimpleClient", className = "Simple")
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
        //RxHttp初始化，自定义OkHttpClient对象，非必须
        RxHttp.init(client, BuildConfig.DEBUG);

        //设置缓存策略，非必须
        File cacheFile = new File(context.getExternalCacheDir(), "RxHttpCache");
        RxHttpPlugins.setCache(cacheFile, 1000 * 100, CacheMode.REQUEST_NETWORK_FAILED_READ_CACHE);
        RxHttpPlugins.setExcludeCacheKeys("time"); //设置一些key，不参与cacheKey的组拼

        //设置数据解密/解码器，非必须
//        RxHttp.setResultDecoder(s -> s);

        //设置全局的转换器，非必须
//        RxHttp.setConverter(FastJsonConverter.create());

        //设置公共参数，非必须
        RxHttp.setOnParamAssembly(p -> {
            /*根据不同请求添加不同参数，子线程执行，每次发送请求前都会被回调
            如果希望部分请求不回调这里，发请求前调用Param.setAssemblyEnabled(false)即可
             */
            Method method = p.getMethod();
            if (method.isGet()) { //Get请求

            } else if (method.isPost()) { //Post请求

            }
            return p.add("versionName", "1.0.0")//添加公共参数
                .add("time", System.currentTimeMillis())
                .addHeader("deviceType", "android"); //添加公共请求头
        });
    }
}
