package rxhttp;

import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Dispatcher;
import okhttp3.OkHttpClient;
import rxhttp.wrapper.ssl.HttpsUtils;
import rxhttp.wrapper.ssl.HttpsUtils.SSLParams;
import rxhttp.wrapper.utils.LogUtil;


/**
 * User: ljx
 * Date: 2017/12/2
 * Time: 11:13
 */
public final class HttpSender {

    private static OkHttpClient mOkHttpClient;

    public static void init(OkHttpClient okHttpClient, boolean debug) {
        LogUtil.setDebug(debug);
        init(okHttpClient);
    }

    public static void init(OkHttpClient okHttpClient) {
        mOkHttpClient = okHttpClient;
    }

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

    //Default OkHttpClient object in RxHttp
    private static OkHttpClient getDefaultOkHttpClient() {
        SSLParams sslParams = HttpsUtils.getSslSocketFactory();
        return new OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .writeTimeout(10, TimeUnit.SECONDS)
            .sslSocketFactory(sslParams.sSLSocketFactory, sslParams.trustManager)
            .hostnameVerifier((hostname, session) -> true)
            .build();
    }

    //Cancel all requests.
    static void cancelAll() {
        final OkHttpClient okHttpClient = mOkHttpClient;
        if (okHttpClient == null) return;
        okHttpClient.dispatcher().cancelAll();
    }


    //Cancel all requests by tag
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
