package com.example.httpsender;

import com.example.httpsender.converter.FastJsonConverter;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.X509TrustManager;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import rxhttp.wrapper.annotation.Converter;
import rxhttp.wrapper.callback.IConverter;
import rxhttp.wrapper.param.Method;
import rxhttp.wrapper.param.RxHttp;
import rxhttp.wrapper.ssl.SSLSocketFactoryImpl;
import rxhttp.wrapper.ssl.X509TrustManagerImpl;

/**
 * User: ljx
 * Date: 2019-11-26
 * Time: 20:44
 */
public class RxHttpManager {

    @Converter(name = "FastJsonConverter")
    public static IConverter fastJsonConverter = FastJsonConverter.create();


    public static void init() {
        X509TrustManager trustAllCert = new X509TrustManagerImpl();
        SSLSocketFactory sslSocketFactory = new SSLSocketFactoryImpl(trustAllCert);
        OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .writeTimeout(10, TimeUnit.SECONDS)
            .sslSocketFactory(sslSocketFactory, trustAllCert) //添加信任证书
            .hostnameVerifier((hostname, session) -> true) //忽略host验证
            .followRedirects(false)  //禁制OkHttp的重定向操作，我们自己处理重定向
            .addInterceptor(new RedirectInterceptor())
            .build();

        //RxHttp初始化，自定义OkHttpClient对象,非必须
        RxHttp.init(client, BuildConfig.DEBUG);
        //设置数据解密/解码器
//        RxHttp.setResultDecoder(s -> s);

        //设置默认的转换器
//        RxHttpPlugins.setConverter(FastJsonConverter.create());

        //设置公共参数
        RxHttp.setOnParamAssembly(p -> {
            /*根据不同请求添加不同参数，子线程执行，每次发送请求前都会被回调
            如果希望部分请求不回调这里，发请求前调用Param.setAssemblyEnabled(false)即可
             */
            Method method = p.getMethod();
            if (method.isGet()) { //Get请求

            } else if (method.isPost()) { //Post请求

            }
            return p.add("versionName", "1.0.0")//添加公共参数
                .addHeader("deviceType", "android"); //添加公共请求头
        });
    }

    //处理重定向的拦截器，非必须
    public static class RedirectInterceptor implements Interceptor {

        @Override
        public Response intercept(Chain chain) throws IOException {
            okhttp3.Request request = chain.request();
            Response response = chain.proceed(request);
            int code = response.code();
            if (code == 308) {
                //获取重定向的地址
                String location = response.headers().get("Location");
                //重新构建请求
                Request newRequest = request.newBuilder().url(location).build();
                response.close();
                response = chain.proceed(newRequest);
            }
            return response;
        }
    }
}
