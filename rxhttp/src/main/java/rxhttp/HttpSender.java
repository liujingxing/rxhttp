package rxhttp;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.X509TrustManager;

import io.reactivex.Observable;
import io.reactivex.Scheduler;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Consumer;
import io.reactivex.plugins.RxJavaPlugins;
import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import rxhttp.wrapper.callback.ProgressCallback;
import rxhttp.wrapper.entity.Progress;
import rxhttp.wrapper.param.FormParam;
import rxhttp.wrapper.param.Param;
import rxhttp.wrapper.parse.DownloadParser;
import rxhttp.wrapper.parse.Parser;
import rxhttp.wrapper.progress.ProgressInterceptor;
import rxhttp.wrapper.ssl.SSLSocketFactoryImpl;
import rxhttp.wrapper.ssl.X509TrustManagerImpl;
import rxhttp.wrapper.utils.LogUtil;


/**
 * 有公共参数的请求，用此类
 * User: ljx
 * Date: 2017/12/2
 * Time: 11:13
 */
public final class HttpSender {

    static {
        Consumer<? super Throwable> errorHandler = RxJavaPlugins.getErrorHandler();
        if (errorHandler == null) {
            /*
            RxJava2的一个重要的设计理念是：不吃掉任何一个异常, 即抛出的异常无人处理，便会导致程序崩溃
            这就会导致一个问题，当RxJava2“downStream”取消订阅后，“upStream”仍有可能抛出异常，
            这时由于已经取消订阅，“downStream”无法处理异常，此时的异常无人处理，便会导致程序崩溃
            */
            RxJavaPlugins.setErrorHandler(LogUtil::log);
        }
    }

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


    /**
     * 同步发送一个请求
     * <p>支持任意请求方式，如：Get、Head、Post、Put等
     *
     * @param param 请求参数
     * @return Response
     * @throws IOException 数据解析异常、网络异常等
     */
    public static Response execute(@NonNull Param param) throws IOException {
        return newCall(param.buildRequest()).execute();
    }

    /**
     * 同步发送一个请求
     * <p>支持任意请求方式，如：Get、Head、Post、Put等
     * <p>亦支持文件上传/下载(无进度回调)
     * {@link DownloadParser} 文件下载(无进度回调)
     * {@link FormParam} 文件上传(无进度回调)
     *
     * @param param  请求参数
     * @param parser 数据解析器
     * @param <T>    要转换的目标数据类型
     * @return T
     * @throws IOException 数据解析异常、网络异常等
     */
    public static <T> T execute(@NonNull Param param, @NonNull Parser<T> parser) throws IOException {
        return parser.onParse(execute(param));
    }

    /**
     * <P>同步发送一个请求
     * <p>支持任意请求方式，如：Get、Head、Post、Put等
     * <p>亦支持文件上传/下载(无进度回调)
     * {@link DownloadParser} 文件下载(无进度回调)
     * {@link FormParam} 文件上传(无进度回调)
     *
     * @param param  请求参数
     * @param parser 数据解析器
     * @param <T>    要转换的目标数据类型
     * @return Observable
     */
    public static <T> Observable<T> syncFrom(@NonNull Param param, @NonNull Parser<T> parser) {
        return new ObservableHttp<>(param, parser);
    }

    /**
     * 异步文件下载，带进度回调
     *
     * @param param      请求参数
     * @param destPath   目标路径
     * @param offsetSize 断点下载时,进度偏移量,仅断点下载时有效
     * @param scheduler  线程调度器
     * @return Observable
     */
    public static Observable<Progress> downloadProgress(@NonNull Param param, final String destPath, long offsetSize, Scheduler scheduler) {
        ObservableDownload observableDownload = new ObservableDownload(param, destPath, offsetSize);
        if (scheduler != null)
            return observableDownload.subscribeOn(scheduler);
        return observableDownload;
    }

    /**
     * 异步发送一个请求,信息上传(支持文件上传,带进度回调)
     * 支持实现了{@link Param}接口的请求
     *
     * @param param     请求参数，必须要重写{@link FormParam#setProgressCallback(ProgressCallback)}方法
     * @param parser    数据解析器
     * @param scheduler 线程调度器
     * @param <T>       要转换的目标数据类型
     * @return Observable
     */
    public static <T> Observable<Progress> uploadProgress(@NonNull Param param, @NonNull Parser<T> parser, Scheduler scheduler) {
        ObservableUpload<T> observableUpload = new ObservableUpload<>(param, parser);
        if (scheduler != null)
            return observableUpload.subscribeOn(scheduler);
        return observableUpload;
    }

    static Call newCall(Request request) {
        return newCall(getOkHttpClient(), request);
    }

    //所有的请求，最终都会调此方法拿到Call对象，然后执行请求
    public static Call newCall(OkHttpClient client, Request request) {
        return client.newCall(request);
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
}
