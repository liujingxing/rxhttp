package rxhttp.wrapper.param;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;

import java.io.File;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Scheduler;
import io.reactivex.rxjava3.functions.Consumer;
import io.reactivex.rxjava3.plugins.RxJavaPlugins;
import io.reactivex.rxjava3.schedulers.Schedulers;
import okhttp3.Headers;
import okhttp3.Response;
import rxhttp.IRxHttp;
import rxhttp.wrapper.OkHttpCompat;
import rxhttp.wrapper.callback.OutputStreamFactory;
import rxhttp.wrapper.callback.UriFactory;
import rxhttp.wrapper.entity.ParameterizedTypeImpl;
import rxhttp.wrapper.entity.Progress;
import rxhttp.wrapper.parse.BitmapParser;
import rxhttp.wrapper.parse.OkResponseParser;
import rxhttp.wrapper.parse.Parser;
import rxhttp.wrapper.parse.SimpleParser;
import rxhttp.wrapper.parse.StreamParser;
import rxhttp.wrapper.utils.LogUtil;
import rxhttp.wrapper.utils.UriUtil;

/**
 * 本类存放asXxx方法(需要单独依赖RxJava，并告知RxHttp依赖的RxJava版本)
 * 如未生成，请查看 https://github.com/liujingxing/rxhttp/wiki/FAQ
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

    public abstract <T> Observable<T> asParser(Parser<T> parser, Scheduler scheduler, Consumer<Progress> progressConsumer);
    
    public <T> Observable<T> asParser(Parser<T> parser) {
        return asParser(parser, null, null);
    }

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
        return asParser(new SimpleParser<>(tTypeMap));
    }

    public final <T> Observable<List<T>> asList(Class<T> tType) {
        Type tTypeList = ParameterizedTypeImpl.get(List.class, tType);
        return asParser(new SimpleParser<>(tTypeList));
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
    
    public final Observable<String> asDownload(String destPath, Scheduler scheduler,
                                               Consumer<Progress> progressConsumer) {
        return asParser(StreamParser.get(destPath), scheduler, progressConsumer);
    }
    
    public final Observable<Uri> asDownload(Context context, Uri uri) {
        return asDownload(context, uri, null, null);   
    }                                                                  
        
    public final Observable<Uri> asDownload(Context context, Uri uri, Scheduler scheduler,    
                                               Consumer<Progress> progressConsumer) {            
        return asParser(StreamParser.get(context, uri), scheduler, progressConsumer);
    }                                                                                            
    
    public final <T> Observable<T> asDownload(OutputStreamFactory<T> osFactory) {
        return asDownload(osFactory, null, null);             
    } 
                                                                               
    public final <T> Observable<T> asDownload(OutputStreamFactory<T> osFactory, Scheduler scheduler,
                                               Consumer<Progress> progressConsumer) {
        return asParser(new StreamParser<>(osFactory), scheduler, progressConsumer);
    }
    
    public final Observable<String> asAppendDownload(String destPath) {                    
        return asAppendDownload(destPath, null, null);                                     
    }                                                                                      
                                                                                           
    public final Observable<String> asAppendDownload(String destPath, Scheduler scheduler, 
                                                     Consumer<Progress> progressConsumer) {
        long fileLength = new File(destPath).length();                                     
        setRangeHeader(fileLength, -1, true);                                              
        return asParser(StreamParser.get(destPath), scheduler, progressConsumer);          
    }                                                                       
     
    public final Observable<Uri> asAppendDownload(Context context, Uri uri) {                   
        return asAppendDownload(context, uri, null, null);                                      
    }                                                                                           
                                                                                                
    public final Observable<Uri> asAppendDownload(Context context, Uri uri, Scheduler scheduler,
                                                  Consumer<Progress> progressConsumer) {        
        return Observable
            .fromCallable(() -> {
                long length = UriUtil.length(uri, context);
                if (length >= 0) setRangeHeader(length, -1, true);
                return StreamParser.get(context, uri);
            })
            .subscribeOn(Schedulers.io())
            .flatMap(parser -> asParser(parser, scheduler, progressConsumer));        
    }                                                                                           
        
    public final Observable<Uri> asAppendDownload(UriFactory uriFactory) {                   
        return asAppendDownload(uriFactory, null, null);                                     
    }                                                                                        
                                                                                             
    public final Observable<Uri> asAppendDownload(UriFactory uriFactory, Scheduler scheduler,
                                                  Consumer<Progress> progressConsumer) {
        return Observable
            .fromCallable(() -> {
                Uri uri = uriFactory.query();
                StreamParser<Uri> parser;
                if (uri != null) {
                    long length = UriUtil.length(uri, uriFactory.getContext());
                    if (length >= 0)
                        setRangeHeader(length, -1, true);
                    parser = StreamParser.get(uriFactory.getContext(), uri);
                } else {
                    parser = new StreamParser<>(uriFactory);
                }
                return parser;
            })
            .subscribeOn(Schedulers.io())
            .flatMap(parser -> asParser(parser, scheduler, progressConsumer));
    }                                                                                            
        
}
