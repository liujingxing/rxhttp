package httpsender;

import java.io.IOException;

import httpsender.wrapper.callback.ProgressCallback;
import httpsender.wrapper.entity.Progress;
import httpsender.wrapper.param.Param;
import httpsender.wrapper.param.PostFormParam;
import httpsender.wrapper.parse.DownloadParser;
import httpsender.wrapper.parse.Parser;
import httpsender.wrapper.parse.StringParser;
import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import okhttp3.OkHttpClient;
import okhttp3.Response;


/**
 * 有公共参数的请求，用此类
 * User: ljx
 * Date: 2017/12/2
 * Time: 11:13
 */
public class HttpSender extends Sender {

    private static Function<Param, Param> mOnParamAssembly;

    public static void init(OkHttpClient okHttpClient) {
        Sender.init(okHttpClient);
    }

    public static OkHttpClient getOkHttpClient() {
        return Sender.getOkHttpClient();
    }

    public static void setOnParamAssembly(Function<Param, Param> onParamAssembly) {
        mOnParamAssembly = onParamAssembly;
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
        return Sender.execute(onAssembly(param));
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
     * <P>异步发送一个请求，在{@link Schedulers#io()} 执行
     * <P>仅支持Get请求
     * <P>不支持文件上传/下载
     *
     * @param url 目标url
     * @return Observable<String>
     */
    public static Observable<String> fromGet(@NonNull String url) {
        return from(Param.get(url));
    }

    /**
     * <P>异步发送一个请求，在{@link Schedulers#io()} 执行
     * <p>支持任意请求方式，如：Get、Head、Post、Put等
     * <P>不支持文件上传/下载
     *
     * @param param 请求参数
     * @return Observable<String>
     */
    public static Observable<String> from(@NonNull Param param) {
        return from(param, StringParser.get());
    }

    /**
     * <P>异步发送一个请求，在{@link Schedulers#io()} 执行
     * <P>仅支持Get请求及文件下载(无进度回调)
     * {@link DownloadParser} 文件下载(无进度回调)
     *
     * @param url    目标url
     * @param parser 数据解析器
     * @param <T>    要转换的目标数据类型
     * @return Observable<T>
     * @see #download(Param, String) 下载带进度回调
     */
    public static <T> Observable<T> fromGet(@NonNull String url, @NonNull Parser<T> parser) {
        return from(Param.get(url), parser);
    }

    /**
     * <P>异步发送一个请求，在{@link Schedulers#io()} 执行
     * <p>支持任意请求方式，如：Get、Head、Post、Put等
     * <p>亦支持文件上传/下载(无进度回调)
     * {@link DownloadParser} 文件下载(无进度回调)
     * {@link PostFormParam} 文件上传(无进度回调)
     *
     * @param param  请求参数
     * @param parser 数据解析器
     * @param <T>    要转换的目标数据类型
     * @return Observable<T>
     * @see #download(Param, String) 下载带进度回调
     * @see #upload(Param, Parser) 上传带进度回调
     */
    public static <T> Observable<T> from(@NonNull Param param, @NonNull Parser<T> parser) {
        return syncFrom(param, parser).subscribeOn(Schedulers.io());
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
     * @see #download(Param, String) 下载带进度回调
     * @see #upload(Param, Parser) 上传带进度回调
     */
    public static <T> Observable<T> syncFrom(@NonNull Param param, @NonNull Parser<T> parser) {
        return Observable.fromCallable(() -> execute(param, parser));
    }

    /**
     * 异步文件下载，带进度回调
     *
     * @param param    请求参数
     * @param destPath 目标路径
     * @return Observable<Progress>
     */
    public static Observable<Progress<String>> download(@NonNull Param param, final String destPath) {
        return Observable.create((ObservableOnSubscribe<Progress<String>>) emitter -> {
            Response response = Sender.download(onAssembly(param), (progress, currentSize, totalSize) -> {
                if (emitter.isDisposed()) return;
                //这里最多回调100次,仅在进度有更新时,才会回调
                emitter.onNext(new Progress<>(progress, currentSize, totalSize));
            });
            String filePath = new DownloadParser(destPath).onParse(response);
            if (emitter.isDisposed()) return;
            emitter.onNext(new Progress<>(filePath)); //最后一次回调文件下载路径
            emitter.onComplete();
        }).subscribeOn(Schedulers.io());
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
    public static <T> Observable<Progress<T>> upload(@NonNull Param param, @NonNull Parser<T> parser) {
        return Observable.create((ObservableOnSubscribe<Progress<T>>) emitter -> {
            param.setProgressCallback((progress, currentSize, totalSize) -> {
                if (emitter.isDisposed()) return;
                //这里最多回调100次,仅在进度有更新时,才会回调
                emitter.onNext(new Progress<>(progress, currentSize, totalSize));
            });
            Response response = execute(param);
            T t = parser.onParse(response);
            if (emitter.isDisposed()) return;
            emitter.onNext(new Progress<>(t)); //最后一次回调Http执行结果
            emitter.onComplete();
        }).subscribeOn(Schedulers.io());
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
