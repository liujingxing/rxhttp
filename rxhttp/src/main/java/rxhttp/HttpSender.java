package rxhttp;

import java.io.IOException;

import io.reactivex.Observable;
import io.reactivex.Scheduler;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Function;
import okhttp3.OkHttpClient;
import okhttp3.Response;
import rxhttp.wrapper.callback.ProgressCallback;
import rxhttp.wrapper.entity.Progress;
import rxhttp.wrapper.param.Param;
import rxhttp.wrapper.param.PostFormParam;
import rxhttp.wrapper.parse.DownloadParser;
import rxhttp.wrapper.parse.Parser;


/**
 * 有公共参数的请求，用此类
 * User: ljx
 * Date: 2017/12/2
 * Time: 11:13
 */
public class HttpSender {

    public static void init(OkHttpClient okHttpClient) {
        init(okHttpClient, false);
    }

    public static void init(OkHttpClient okHttpClient, boolean debug) {
        Sender.setDebug(debug);
        Sender.init(okHttpClient);
    }

    public static OkHttpClient getOkHttpClient() {
        return Sender.getOkHttpClient();
    }

    public static void setDebug(boolean debug) {
        Sender.setDebug(debug);
    }

    public static void setOnParamAssembly(Function<Param, Param> onParamAssembly) {
        Sender.setOnParamAssembly(onParamAssembly);
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
        return Sender.execute(param);
    }

    /**
     * 同步发送一个请求
     * <p>支持任意请求方式，如：Get、Head、Post、Put等
     * <p>亦支持文件上传/下载(无进度回调)
     * {@link DownloadParser} 文件下载(无进度回调)
     * {@link PostFormParam} 文件上传(无进度回调)
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
     * {@link PostFormParam} 文件上传(无进度回调)
     *
     * @param param  请求参数
     * @param parser 数据解析器
     * @param <T>    要转换的目标数据类型
     * @return Observable<T>
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
     * @return Observable<Progress>
     */
    public static Observable<Progress<String>> downloadProgress(@NonNull Param param, final String destPath, long offsetSize, Scheduler scheduler) {
        ObservableDownload observableDownload = new ObservableDownload(param, destPath, offsetSize);
        if (scheduler != null)
            return observableDownload.subscribeOn(scheduler);
        return observableDownload;
    }

    /**
     * 异步发送一个请求,信息上传(支持文件上传,带进度回调)
     * 支持实现了{@link Param}接口的请求
     *
     * @param param  请求参数，必须要重写{@link Param#setProgressCallback(ProgressCallback)}方法
     * @param parser 数据解析器
     * @param <T>    要转换的目标数据类型
     * @return Observable<Progress>
     */
    public static <T> Observable<Progress<T>> uploadProgress(@NonNull Param param, @NonNull Parser<T> parser, Scheduler scheduler) {
        ObservableUpload<T> observableUpload = new ObservableUpload<>(param, parser);
        if (scheduler != null)
            return observableUpload.subscribeOn(scheduler);
        return observableUpload;
    }
}
