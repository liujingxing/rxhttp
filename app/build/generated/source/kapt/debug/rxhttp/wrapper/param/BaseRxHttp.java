package rxhttp.wrapper.param;

import android.graphics.Bitmap;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Scheduler;
import io.reactivex.rxjava3.functions.Consumer;
import io.reactivex.rxjava3.plugins.RxJavaPlugins;
import okhttp3.Headers;
import okhttp3.Response;
import rxhttp.IRxHttp;
import rxhttp.wrapper.OkHttpCompat;
import rxhttp.wrapper.annotations.Nullable;
import rxhttp.wrapper.entity.ParameterizedTypeImpl;
import rxhttp.wrapper.entity.Progress;
import rxhttp.wrapper.parse.BitmapParser;
import rxhttp.wrapper.parse.DownloadParser;
import rxhttp.wrapper.parse.OkResponseParser;
import rxhttp.wrapper.parse.Parser;
import rxhttp.wrapper.parse.SimpleParser;
import rxhttp.wrapper.utils.LogUtil;

/**
 * 本类存放asXxx方法，如果依赖了RxJava的话
 * User: ljx
 * Date: 2020/4/11
 * Time: 18:15
 */
public abstract class BaseRxHttp implements IRxHttp {

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

    public abstract <T> Observable<T> asParser(Parser<T> parser);
    
    /**                                                           
     * 监听下载进度时，调用此方法                                              
     *                                                                                                          
     * @param destPath           文件存储路径                                                                         
     * @param observeOnScheduler 控制回调所在线程，传入null，则默认在请求所在线程(子线程)回调                                              
     * @param progressConsumer   进度回调                                                                           
     * @return Observable                                                                                       
     */                                                                                                          
    public abstract Observable<String> asDownload(String destPath,
                                                  @Nullable Scheduler observeOnScheduler,
                                                  Consumer<Progress> progressConsumer);      

    public final <T> Observable<T> asClass(Class<T> type) {
        return asParser(new SimpleParser<>(type));
    }

    public final Observable<String> asString() {
        return asClass(String.class);
    }

    public final Observable<Boolean> asBoolean() {
        return asClass(Boolean.class);
    }

    public final Observable<Byte> asByte() {
        return asClass(Byte.class);
    }

    public final Observable<Short> asShort() {
        return asClass(Short.class);
    }

    public final Observable<Integer> asInteger() {
        return asClass(Integer.class);
    }

    public final Observable<Long> asLong() {
        return asClass(Long.class);
    }

    public final Observable<Float> asFloat() {
        return asClass(Float.class);
    }

    public final Observable<Double> asDouble() {
        return asClass(Double.class);
    }

    public final <K> Observable<Map<K, K>> asMap(Class<K> kType) {
        return asMap(kType, kType);
    }

    public final <K, V> Observable<Map<K, V>> asMap(Class<K> kType, Class<V> vType) {
        Type tTypeMap = ParameterizedTypeImpl.getParameterized(Map.class, kType, vType);
        return asParser(new SimpleParser<Map<K, V>>(tTypeMap));
    }

    public final <T> Observable<List<T>> asList(Class<T> tType) {
        Type tTypeList = ParameterizedTypeImpl.get(List.class, tType);
        return asParser(new SimpleParser<List<T>>(tTypeList));
    }

    public final <T> Observable<Bitmap> asBitmap() {
        return asParser(new BitmapParser());
    }

    public final Observable<Response> asOkResponse() {
        return asParser(new OkResponseParser());
    }

    public final Observable<Headers> asHeaders() {               
        return asOkResponse()                                    
            .map(response -> {                                   
                try {                                            
                    return response.headers();                   
                } finally {                                      
                    OkHttpCompat.closeQuietly(response);  
                }                                                
            });                                                  
    }

    public final Observable<String> asDownload(String destPath) {
        return asDownload(destPath, null, null);
    }

    public final Observable<String> asDownload(String destPath,
                                               Consumer<Progress> progressConsumer) {
        return asDownload(destPath, null, progressConsumer);
    }

}
