package httpsender;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.X509TrustManager;

import httpsender.wrapper.callback.ProgressCallback;
import httpsender.wrapper.param.RequestBuilder;
import httpsender.wrapper.progress.ProgressInterceptor;
import io.reactivex.annotations.NonNull;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * User: ljx
 * Date: 2018/1/10
 * Time: 18:36
 */
class Sender {

    private static OkHttpClient mOkHttpClient; //只能初始化一次,第二次将抛出异常

    static void init(OkHttpClient okHttpClient) {
        if (mOkHttpClient != null)
            throw new IllegalArgumentException("OkHttpClient can only be initialized once");
        mOkHttpClient = okHttpClient;
    }

    static OkHttpClient getOkHttpClient() {
        if (mOkHttpClient == null)
            mOkHttpClient = getDefaultOkHttpClient();
        return mOkHttpClient;
    }

    /**
     * 同步执行请求
     *
     * @param builder Request构造器
     * @return Http响应结果
     * @throws IOException 超时、网络异常
     */
    static Response execute(@NonNull RequestBuilder builder) throws IOException {
        Request request = builder.buildRequest();
        return execute(getOkHttpClient(), request);
    }

    /**
     * 同步下载文件，带进度回调
     *
     * @param builder  Request构造器
     * @param callback 进度回调接口
     * @return Http响应结果
     * @throws IOException 超时、网络异常
     */
    static Response download(@NonNull RequestBuilder builder, @NonNull ProgressCallback callback) throws IOException {
        OkHttpClient okHttpClient = clone(callback);
        Request request = builder.buildRequest();
        return execute(okHttpClient, request);
    }

    /**
     * 同步执行请求
     *
     * @param okHttpClient OkHttpClient对象
     * @param request      Request请求对象
     * @return Http响应结果
     * @throws IOException 超时、网络异常
     */
    private static Response execute(@NonNull OkHttpClient okHttpClient, @NonNull Request request) throws IOException {
        return okHttpClient.newCall(request).execute();
    }

    /**
     * 克隆一个OkHttpClient对象,用于监听下载进度
     *
     * @param progressCallback 进度回调
     * @return 克隆的OkHttpClient对象
     */
    private static OkHttpClient clone(@NonNull final ProgressCallback progressCallback) {
        //克隆一个OkHttpClient后,增加拦截器,拦截下载进度
        return getOkHttpClient().newBuilder()
                .addNetworkInterceptor(new ProgressInterceptor(progressCallback))
                .build();
    }


    /**
     * 连接、读写超时均为10s、添加信任证书并忽略host验证
     *
     * @return 返回默认的OkHttpClient对象
     */
    private static OkHttpClient getDefaultOkHttpClient() {
        X509TrustManager trustAllCert = new X509TrustManagerImpl();
        SSLSocketFactory sslSocketFactory = new SSLSocketFactoryImpl(trustAllCert);
        return new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .sslSocketFactory(sslSocketFactory, trustAllCert) //添加信任证书
                .hostnameVerifier((hostname, session) -> true) //忽略host验证
                .build();
    }
}
