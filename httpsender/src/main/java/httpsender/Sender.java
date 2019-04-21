package httpsender;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.X509TrustManager;

import httpsender.wrapper.callback.ProgressCallback;
import httpsender.wrapper.param.Param;
import httpsender.wrapper.progress.ProgressInterceptor;
import httpsender.wrapper.utils.LogUtil;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Function;
import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Response;

/**
 * User: ljx
 * Date: 2018/1/10
 * Time: 18:36
 */
class Sender {

    private static Function<Param, Param> mOnParamAssembly;

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

    static void setDebug(boolean debug) {
        LogUtil.setDebug(debug);
    }

    static void setOnParamAssembly(Function<Param, Param> onParamAssembly) {
        mOnParamAssembly = onParamAssembly;
    }

    /**
     * 同步执行请求
     *
     * @param param 请求参数
     * @return Http响应结果
     * @throws IOException 超时、网络异常
     */
    static Response execute(@NonNull Param param) throws IOException {
        return newCall(param).execute();
    }

    static Call newCall(Param param) throws IOException {
        return newCall(getOkHttpClient(), param);
    }


    static Call newCall(OkHttpClient client, Param param) throws IOException {
        param = onAssembly(param);
        LogUtil.log(param);
        return client.newCall(param.buildRequest());
    }

    /**
     * 克隆一个OkHttpClient对象,用于监听下载进度
     *
     * @param progressCallback 进度回调
     * @return 克隆的OkHttpClient对象
     */
    static OkHttpClient clone(@NonNull final ProgressCallback progressCallback) {
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


    /**
     * <P>对Param参数添加一层装饰,可以在该层做一些与业务相关工作，
     * <P>例如：添加公共参数/请求头信息
     *
     * @param p 参数
     * @return 装饰后的参数
     */
    private static Param onAssembly(Param p) throws IOException {
        Function<Param, Param> f = mOnParamAssembly;
        if (f == null) return p;
        if (p == null || !p.isAssemblyEnabled()) return p;
        try {
            return f.apply(p);
        } catch (Exception e) {
            throw new IOException(e);
        }
    }
}
