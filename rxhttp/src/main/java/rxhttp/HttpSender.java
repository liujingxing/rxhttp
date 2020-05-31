package rxhttp;

import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Dispatcher;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import rxhttp.wrapper.annotations.NonNull;
import rxhttp.wrapper.callback.ProgressCallback;
import rxhttp.wrapper.progress.ProgressInterceptor;
import rxhttp.wrapper.ssl.HttpsUtils;
import rxhttp.wrapper.ssl.HttpsUtils.SSLParams;
import rxhttp.wrapper.utils.LogUtil;


/**
 * 有公共参数的请求，用此类
 * User: ljx
 * Date: 2017/12/2
 * Time: 11:13
 */
public final class HttpSender {

    private static OkHttpClient mOkHttpClient; //只能初始化一次,第二次将抛出异常

    public static void init(OkHttpClient okHttpClient, boolean debug) {
        setDebug(debug);
        init(okHttpClient);
    }

    public static void init(OkHttpClient okHttpClient) {
        if (mOkHttpClient != null)
            throw new IllegalArgumentException("OkHttpClient can only be initialized once");
        mOkHttpClient = okHttpClient;
    }

    //判断是否已经初始化
    public static boolean isInit() {
        return mOkHttpClient != null;
    }

    public static OkHttpClient getOkHttpClient() {
        if (mOkHttpClient == null)
            mOkHttpClient = getDefaultOkHttpClient();
        return mOkHttpClient;
    }

    public static OkHttpClient.Builder newOkClientBuilder() {
        return getOkHttpClient().newBuilder();
    }

    public static void setDebug(boolean debug) {
        LogUtil.setDebug(debug);
    }


    //所有的请求，最终都会调此方法拿到Call对象，然后执行请求
    public static Call newCall(OkHttpClient client, Request request) {
        return client.newCall(request);
    }

    /**
     * 克隆一个OkHttpClient对象,用于监听下载进度
     *
     * @param client           OkHttpClient
     * @param progressCallback 进度回调
     * @return 克隆的OkHttpClient对象
     */
    public static OkHttpClient clone(OkHttpClient client, @NonNull final ProgressCallback progressCallback) {
        //克隆一个OkHttpClient后,增加拦截器,拦截下载进度
        return client.newBuilder()
            .addNetworkInterceptor(new ProgressInterceptor(progressCallback))
            .build();
    }

    /**
     * 连接、读写超时均为10s、添加信任证书并忽略host验证
     *
     * @return 返回默认的OkHttpClient对象
     */
    private static OkHttpClient getDefaultOkHttpClient() {
        SSLParams sslParams = HttpsUtils.getSslSocketFactory();
        return new OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .writeTimeout(10, TimeUnit.SECONDS)
            .sslSocketFactory(sslParams.sSLSocketFactory, sslParams.trustManager) //添加信任证书
            .hostnameVerifier((hostname, session) -> true) //忽略host验证
            .build();
    }

    /**
     * 取消所有请求
     */
    static void cancelAll() {
        final OkHttpClient okHttpClient = mOkHttpClient;
        if (okHttpClient == null) return;
        okHttpClient.dispatcher().cancelAll();
    }


    /**
     * 根据Tag取消请求
     */
    static void cancelTag(Object tag) {
        if (tag == null) return;
        final OkHttpClient okHttpClient = mOkHttpClient;
        if (okHttpClient == null) return;
        Dispatcher dispatcher = okHttpClient.dispatcher();

        for (Call call : dispatcher.queuedCalls()) {
            if (tag.equals(call.request().tag())) {
                call.cancel();
            }
        }

        for (Call call : dispatcher.runningCalls()) {
            if (tag.equals(call.request().tag())) {
                call.cancel();
            }
        }
    }
}
