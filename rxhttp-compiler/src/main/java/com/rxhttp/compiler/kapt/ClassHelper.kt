package com.rxhttp.compiler.kapt

import com.rxhttp.compiler.common.getObservableClass
import com.rxhttp.compiler.getClassPath
import com.rxhttp.compiler.isDependenceRxJava
import com.rxhttp.compiler.rxHttpPackage
import java.io.BufferedWriter
import javax.annotation.processing.Filer


/**
 * User: ljx
 * Date: 2020/3/31
 * Time: 23:36
 */
class ClassHelper(private val isAndroidPlatform: Boolean) {

    private fun isAndroid(s: String) = if (isAndroidPlatform) s else ""

    fun generatorStaticClass(filer: Filer) {
        generatorBaseRxHttp(filer)
        generatorRxHttpAbstractBodyParam(filer)
        generatorRxHttpBodyParam(filer)
        generatorRxHttpFormParam(filer)
        generatorRxHttpNoBodyParam(filer)
        generatorRxHttpJsonParam(filer)
        generatorRxHttpJsonArrayParam(filer)
        if (isDependenceRxJava()) {
            getObservableClass().forEach { (t, u) ->
                generatorClass(filer,t,u)
            }
        }
    }

    private fun generatorBaseRxHttp(filer: Filer) {
        if (!isDependenceRxJava()) {
            generatorClass(
                filer, "BaseRxHttp", """
                package $rxHttpPackage;

                import rxhttp.wrapper.CallFactory;
                import rxhttp.wrapper.coroutines.RangeHeader;

                /**
                 * 本类存放asXxx方法(需要单独依赖RxJava，并告知RxHttp依赖的RxJava版本)
                 * 如未生成，请查看 https://github.com/liujingxing/rxhttp/wiki/FAQ
                 * User: ljx
                 * Date: 2020/4/11
                 * Time: 18:15
                 */
                public abstract class BaseRxHttp implements CallFactory, RangeHeader {

                    
                }
            """.trimIndent())
        } else {
            generatorClass(filer, "BaseRxHttp", """
            package $rxHttpPackage;
            ${isAndroid("""
            import android.content.Context;
            import android.graphics.Bitmap;
            import android.net.Uri;
            """)}
            import java.lang.reflect.Type;
            import java.util.List;
            import java.util.Map;

            import ${getClassPath("Observable")};
            import ${getClassPath("Scheduler")};
            import ${getClassPath("Consumer")};
            import ${getClassPath("RxJavaPlugins")};
            import ${getClassPath("Schedulers")};
            import okhttp3.Headers;
            import okhttp3.Response;
            import rxhttp.wrapper.CallFactory;
            import rxhttp.wrapper.OkHttpCompat;
            import rxhttp.wrapper.callback.FileOutputStreamFactory;
            import rxhttp.wrapper.callback.OutputStreamFactory;
            ${isAndroid("import rxhttp.wrapper.callback.UriOutputStreamFactory;")}
            import rxhttp.wrapper.coroutines.RangeHeader;
            import rxhttp.wrapper.entity.ParameterizedTypeImpl;
            import rxhttp.wrapper.entity.Progress;
            import rxhttp.wrapper.parse.Parser;
            import rxhttp.wrapper.parse.SmartParser;
            import rxhttp.wrapper.parse.StreamParser;
            import rxhttp.wrapper.utils.LogUtil;

            /**
             * 本类存放asXxx方法(需要单独依赖RxJava，并告知RxHttp依赖的RxJava版本)
             * 如未生成，请查看 https://github.com/liujingxing/rxhttp/wiki/FAQ
             * User: ljx
             * Date: 2020/4/11
             * Time: 18:15
             */
            public abstract class BaseRxHttp implements CallFactory, RangeHeader {

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

                public final <T> ObservableCall<T> asParser(Parser<T> parser) {
                    return new ObservableCall(this, parser);
                }

                public final <T> ObservableCall<T> asClass(Type type) {
                    return asParser(SmartParser.wrap(type));
                }
                
                public final <T> ObservableCall<T> asClass(Class<T> clazz) {
                    return asClass((Type) clazz);
                }

                public final ObservableCall<String> asString() {
                    return asClass(String.class);
                }

                public final <V> ObservableCall<Map<String, V>> asMapString(Class<V> vType) {
                    Type tTypeMap = ParameterizedTypeImpl.getParameterized(Map.class, String.class, vType);
                    return asClass(tTypeMap);
                }

                public final <T> ObservableCall<List<T>> asList(Class<T> tType) {
                    Type tTypeList = ParameterizedTypeImpl.get(List.class, tType);
                    return asClass(tTypeList);
                }
                ${isAndroid("""
                public final ObservableCall<Bitmap> asBitmap() {
                    return asClass(Bitmap.class);
                }
                """)}
                public final ObservableCall<Response> asOkResponse() {
                    return asClass(Response.class);
                }

                public final ObservableCall<Headers> asHeaders() {               
                    return asClass(Headers.class);                                        
                }

                public final ObservableCall<String> asDownload(String destPath) {
                    return asDownload(destPath, false);
                }

                public final ObservableCall<String> asDownload(String destPath, boolean append) {
                    return asDownload(new FileOutputStreamFactory(destPath), append);
                }
                ${isAndroid("""
                public final ObservableCall<Uri> asDownload(Context context, Uri uri) {
                    return asDownload(context, uri, false);   
                }                                                                  
                    
                public final ObservableCall<Uri> asDownload(Context context, Uri uri, boolean append) {            
                    return asDownload(new UriOutputStreamFactory(context, uri), append);
                }                                                                                            
                """)}
                public final <T> ObservableCall<T> asDownload(OutputStreamFactory<T> osFactory) {
                    return asDownload(osFactory, false);             
                } 
                                                                                           
                public final <T> ObservableCall<T> asDownload(OutputStreamFactory<T> osFactory, boolean append) {
                    ObservableCall<T> observableCall =  asParser(new StreamParser<>(osFactory));
                    if (append) {
                        return observableCall.onSubscribe(() -> {
                            long offsetSize = osFactory.offsetSize();
                            if (offsetSize >= 0)
                                setRangeHeader(offsetSize, -1, true);
                        });
                    } else {
                        return observableCall;
                    }
                }
            }

        """.trimIndent())
        }
    }

    private fun generatorRxHttpAbstractBodyParam(filer: Filer) {
        generatorClass(
            filer, "RxHttpAbstractBodyParam", """
                package $rxHttpPackage;
                
                import rxhttp.wrapper.BodyParamFactory;

                /**
                 * Github
                 * https://github.com/liujingxing/rxhttp
                 * https://github.com/liujingxing/rxlife
                 * https://github.com/liujingxing/rxhttp/wiki/FAQ
                 * https://github.com/liujingxing/rxhttp/wiki/更新日志
                 */
                public class RxHttpAbstractBodyParam<P extends AbstractBodyParam<P>, R extends RxHttpAbstractBodyParam<P, R>> 
                    extends RxHttp<P, R> implements BodyParamFactory {

                    protected RxHttpAbstractBodyParam(P param) {
                        super(param);
                    }
                }
            """.trimIndent()
        )
    }

    private fun generatorRxHttpNoBodyParam(filer: Filer) {
        generatorClass(filer, "RxHttpNoBodyParam", """
            package $rxHttpPackage;
            
            import org.jetbrains.annotations.NotNull;

            import java.util.Map;
            
            import rxhttp.wrapper.param.NoBodyParam;

            /**
             * Github
             * https://github.com/liujingxing/rxhttp
             * https://github.com/liujingxing/rxlife
             * https://github.com/liujingxing/rxhttp/wiki/FAQ
             * https://github.com/liujingxing/rxhttp/wiki/更新日志
             */
            public class RxHttpNoBodyParam extends RxHttp<NoBodyParam, RxHttpNoBodyParam> {
                public RxHttpNoBodyParam(NoBodyParam param) {
                    super(param);
                }
                
                public RxHttpNoBodyParam add(String key, Object value) {
                    return addQuery(key, value);
                }
                
                public RxHttpNoBodyParam add(String key, Object value, boolean isAdd) {
                    if (isAdd) {
                        addQuery(key, value);
                    }
                    return this;
                }
                
                public RxHttpNoBodyParam addAll(Map<String, ?> map) {
                    return addAllQuery(map);
                }

                public RxHttpNoBodyParam addEncoded(String key, Object value) {
                    return addEncodedQuery(key, value);
                }
                
                public RxHttpNoBodyParam addAllEncoded(@NotNull Map<String, ?> map) {
                    return addAllEncodedQuery(map);
                }
            }

        """.trimIndent())
    }

    private fun generatorRxHttpBodyParam(filer: Filer) {
        generatorClass(
            filer, "RxHttpBodyParam", """
            package $rxHttpPackage;
            ${isAndroid("""
            import android.content.Context;
            import android.net.Uri;
            import rxhttp.wrapper.utils.UriUtil;
            """)}
            import org.jetbrains.annotations.Nullable;
            
            import java.io.File;
            
            import okhttp3.MediaType;
            import okhttp3.RequestBody;
            import okio.ByteString;
            import rxhttp.wrapper.param.BodyParam;
            import rxhttp.wrapper.OkHttpCompat;
            import rxhttp.wrapper.entity.FileRequestBody;
            import rxhttp.wrapper.utils.BuildUtil;

            /**
             * Github
             * https://github.com/liujingxing/rxhttp
             * https://github.com/liujingxing/rxlife
             */
            public class RxHttpBodyParam extends RxHttpAbstractBodyParam<BodyParam, RxHttpBodyParam> {
                public RxHttpBodyParam(BodyParam param) {
                    super(param);
                }
                
                public RxHttpBodyParam setBody(String content, @Nullable MediaType contentType) {
                    return setBody(OkHttpCompat.create(contentType, content));
                }
                
                public RxHttpBodyParam setBody(ByteString content, @Nullable MediaType contentType) {
                    return setBody(OkHttpCompat.create(contentType, content));
                }
                
                public RxHttpBodyParam setBody(byte[] content, @Nullable MediaType mediaType) {
                    return setBody(content, mediaType, 0, content.length);
                }
                
                public RxHttpBodyParam setBody(byte[] content, @Nullable MediaType mediaType, int offset, int byteCount) {
                    return setBody(OkHttpCompat.create(mediaType, content, offset, byteCount));
                }
                
                public RxHttpBodyParam setBody(File file) {
                    return setBody(file, BuildUtil.getMediaType(file.getName()));
                }
                
                public RxHttpBodyParam setBody(File file, @Nullable MediaType contentType) {
                    return setBody(new FileRequestBody(file, 0, contentType));
                }
                ${isAndroid("""
                public RxHttpBodyParam setBody(Context context, Uri uri) {
                    return setBody(context, uri, BuildUtil.getMediaTypeByUri(context, uri));
                }
                
                public RxHttpBodyParam setBody(Context context, Uri uri, @Nullable MediaType contentType) {
                    return setBody(UriUtil.asRequestBody(uri, context, 0, contentType));
                }
                """)}
                public RxHttpBodyParam setBody(Object object) {
                    param.setBody(object);
                    return this;
                }
                
                public RxHttpBodyParam setBody(RequestBody requestBody) {
                    param.setBody(requestBody);
                    return this;
                }
            }

        """.trimIndent()
        )
    }

    private fun generatorRxHttpFormParam(filer: Filer) {
        generatorClass(filer, "RxHttpFormParam", """
            package $rxHttpPackage;

            ${isAndroid("import android.content.Context;")}
            ${isAndroid("import android.net.Uri;")}
            
            import org.jetbrains.annotations.NotNull;
            import org.jetbrains.annotations.Nullable;

            import java.io.File;
            import java.util.List;
            import java.util.Map;
            import java.util.Map.Entry;

            import okhttp3.Headers;
            import okhttp3.MediaType;
            import okhttp3.MultipartBody.Part;
            import okhttp3.RequestBody;
            import rxhttp.wrapper.entity.UpFile;
            import rxhttp.wrapper.param.FormParam;
            import rxhttp.wrapper.utils.UriUtil;

            /**
             * Github
             * https://github.com/liujingxing/rxhttp
             * https://github.com/liujingxing/rxlife
             * https://github.com/liujingxing/rxhttp/wiki/FAQ
             * https://github.com/liujingxing/rxhttp/wiki/更新日志
             */
            public class RxHttpFormParam extends RxHttpAbstractBodyParam<FormParam, RxHttpFormParam> {
                public RxHttpFormParam(FormParam param) {
                    super(param);
                }

                public RxHttpFormParam add(String key, Object value) {
                  param.add(key,value);
                  return this;
                }
                
                public RxHttpFormParam add(String key, Object value, boolean isAdd) {
                  if(isAdd) {
                    param.add(key,value);
                  }
                  return this;
                }
                
                public RxHttpFormParam addAll(Map<String, ?> map) {
                  param.addAll(map);
                  return this;
                }
                
                public RxHttpFormParam addEncoded(String key, Object value) {
                    param.addEncoded(key, value);
                    return this;
                }
                
                public RxHttpFormParam addAllEncoded(@NotNull Map<String, ?> map) {
                    param.addAllEncoded(map);
                    return this;
                }

                public RxHttpFormParam removeAllBody() {
                    param.removeAllBody();
                    return this;
                }

                public RxHttpFormParam removeAllBody(String key) {
                    param.removeAllBody(key);
                    return this;
                }

                public RxHttpFormParam set(String key, Object value) {
                    param.set(key, value);
                    return this;
                }

                public RxHttpFormParam setEncoded(String key, Object value) {
                    param.setEncoded(key, value);
                    return this;
                }

                public RxHttpFormParam addFile(String key, File file) {
                    param.addFile(key, file);
                    return this;
                }

                public RxHttpFormParam addFile(String key, String filePath) {
                    param.addFile(key, filePath);
                    return this;
                }

                public RxHttpFormParam addFile(String key, File file, String filename) {
                    param.addFile(key, file, filename);
                    return this;
                }

                public RxHttpFormParam addFile(UpFile file) {
                    param.addFile(file);
                    return this;
                }

                public RxHttpFormParam addFiles(List<? extends UpFile> fileList) {
                    param.addFiles(fileList);
                    return this;
                }
                
                public <T> RxHttpFormParam addFiles(Map<String, T> fileMap) {
                    param.addFiles(fileMap);
                    return this;
                }
                
                public <T> RxHttpFormParam addFiles(String key, List<T> fileList) {
                    param.addFiles(key, fileList);
                    return this;
                }

                public RxHttpFormParam addPart(@Nullable MediaType contentType, byte[] content) {
                    param.addPart(contentType, content);
                    return this;
                }

                public RxHttpFormParam addPart(@Nullable MediaType contentType, byte[] content, int offset,
                                               int byteCount) {
                    param.addPart(contentType, content, offset, byteCount);
                    return this;
                }
                ${isAndroid("""
                public RxHttpFormParam addPart(Context context, Uri uri) {
                    param.addPart(UriUtil.asRequestBody(uri, context));
                    return this;
                }

                public RxHttpFormParam addPart(Context context, String key, Uri uri) {
                    param.addPart(UriUtil.asPart(uri, context, key));
                    return this;
                }

                public RxHttpFormParam addPart(Context context, String key, String fileName, Uri uri) {
                    param.addPart(UriUtil.asPart(uri, context, key, fileName));
                    return this;
                }

                public RxHttpFormParam addPart(Context context, Uri uri, @Nullable MediaType contentType) {
                    param.addPart(UriUtil.asRequestBody(uri, context, 0, contentType));
                    return this;
                }

                public RxHttpFormParam addPart(Context context, String key, Uri uri,
                                               @Nullable MediaType contentType) {
                    param.addPart(UriUtil.asPart(uri, context, key, UriUtil.displayName(uri, context), 0, contentType));
                    return this;
                }

                public RxHttpFormParam addPart(Context context, String key, String filename, Uri uri,
                                               @Nullable MediaType contentType) {
                    param.addPart(UriUtil.asPart(uri, context, key, filename, 0, contentType));
                    return this;
                }

                public RxHttpFormParam addParts(Context context, Map<String, Uri> uriMap) {
                    for (Entry<String, Uri> entry : uriMap.entrySet()) {
                        addPart(context, entry.getKey(), entry.getValue());
                    }
                    return this;
                }

                public RxHttpFormParam addParts(Context context, List<Uri> uris) {
                    for (Uri uri : uris) {
                        addPart(context, uri);
                    }
                    return this;
                }

                public RxHttpFormParam addParts(Context context, String key, List<Uri> uris) {
                    for (Uri uri : uris) {
                        addPart(context, key, uri);
                    }
                    return this;
                }

                public RxHttpFormParam addParts(Context context, List<Uri> uris,
                                                @Nullable MediaType contentType) {
                    for (Uri uri : uris) {
                        addPart(context, uri, contentType);
                    }
                    return this;
                }

                public RxHttpFormParam addParts(Context context, String key, List<Uri> uris,
                                                @Nullable MediaType contentType) {
                    for (Uri uri : uris) {
                        addPart(context, key, uri, contentType);
                    }
                    return this;
                }
                """)}
                public RxHttpFormParam addPart(Part part) {
                    param.addPart(part);
                    return this;
                }

                public RxHttpFormParam addPart(RequestBody requestBody) {
                    param.addPart(requestBody);
                    return this;
                }

                public RxHttpFormParam addPart(Headers headers, RequestBody requestBody) {
                    param.addPart(headers, requestBody);
                    return this;
                }

                public RxHttpFormParam addFormDataPart(String key, String fileName, RequestBody requestBody) {
                    param.addFormDataPart(key, fileName, requestBody);
                    return this;
                }

                //Set content-type to multipart/form-data
                public RxHttpFormParam setMultiForm() {
                    param.setMultiForm();
                    return this;
                }
                
                //Set content-type to multipart/mixed
                public RxHttpFormParam setMultiMixed() {
                    param.setMultiMixed();
                    return this;
                }
                
                //Set content-type to multipart/alternative
                public RxHttpFormParam setMultiAlternative() {
                    param.setMultiAlternative();
                    return this;
                }
                
                //Set content-type to multipart/digest
                public RxHttpFormParam setMultiDigest() {
                    param.setMultiDigest();
                    return this;
                }
                
                //Set content-type to multipart/parallel
                public RxHttpFormParam setMultiParallel() {
                    param.setMultiParallel();
                    return this;
                }
                
                //Set the MIME type
                public RxHttpFormParam setMultiType(MediaType multiType) {
                    param.setMultiType(multiType);
                    return this;
                }
            }

        """.trimIndent())
    }

    private fun generatorRxHttpJsonParam(filer: Filer) {
        generatorClass(filer, "RxHttpJsonParam", """
            package $rxHttpPackage;

            import com.google.gson.JsonObject;

            import java.util.Map;
            
            import rxhttp.wrapper.param.JsonParam;
            /**
             * Github
             * https://github.com/liujingxing/rxhttp
             * https://github.com/liujingxing/rxlife
             * https://github.com/liujingxing/rxhttp/wiki/FAQ
             * https://github.com/liujingxing/rxhttp/wiki/更新日志
             */
            public class RxHttpJsonParam extends RxHttpAbstractBodyParam<JsonParam, RxHttpJsonParam> {
                public RxHttpJsonParam(JsonParam param) {
                    super(param);
                }

                public RxHttpJsonParam add(String key, Object value) {
                  param.add(key,value);
                  return this;
                }
                
                public RxHttpJsonParam add(String key, Object value, boolean isAdd) {
                  if(isAdd) {
                    param.add(key,value);
                  }
                  return this;
                }
                
                public RxHttpJsonParam addAll(Map<String, ?> map) {
                  param.addAll(map);
                  return this;
                }
                
                /**
                 * 将Json对象里面的key-value逐一取出，添加到另一个Json对象中，
                 * 输入非Json对象将抛出{@link IllegalStateException}异常
                 */
                public RxHttpJsonParam addAll(String jsonObject) {
                    param.addAll(jsonObject);
                    return this;
                }

                /**
                 * 将Json对象里面的key-value逐一取出，添加到另一个Json对象中
                 */
                public RxHttpJsonParam addAll(JsonObject jsonObject) {
                    param.addAll(jsonObject);
                    return this;
                }

                /**
                 * 添加一个JsonElement对象(Json对象、json数组等)
                 */
                public RxHttpJsonParam addJsonElement(String key, String jsonElement) {
                    param.addJsonElement(key, jsonElement);
                    return this;
                }
            }

        """.trimIndent())
    }

    private fun generatorRxHttpJsonArrayParam(filer: Filer) {
        generatorClass(filer, "RxHttpJsonArrayParam", """
            package $rxHttpPackage;

            import com.google.gson.JsonArray;
            import com.google.gson.JsonObject;

            import java.util.List;
            import java.util.Map;
            
            import rxhttp.wrapper.param.JsonArrayParam;

            /**
             * Github
             * https://github.com/liujingxing/rxhttp
             * https://github.com/liujingxing/rxlife
             * https://github.com/liujingxing/rxhttp/wiki/FAQ
             * https://github.com/liujingxing/rxhttp/wiki/更新日志
             */
            public class RxHttpJsonArrayParam extends RxHttpAbstractBodyParam<JsonArrayParam, RxHttpJsonArrayParam> {
                public RxHttpJsonArrayParam(JsonArrayParam param) {
                    super(param);
                }

                public RxHttpJsonArrayParam add(String key, Object value) {
                  param.add(key,value);
                  return this;
                }
                
                public RxHttpJsonArrayParam add(String key, Object value, boolean isAdd) {
                  if(isAdd) {
                    param.add(key,value);
                  }
                  return this;
                }
                
                public RxHttpJsonArrayParam addAll(Map<String, ?> map) {
                  param.addAll(map);
                  return this;
                }

                public RxHttpJsonArrayParam add(Object object) {
                    param.add(object);
                    return this;
                }

                public RxHttpJsonArrayParam addAll(List<?> list) {
                    param.addAll(list);
                    return this;
                }

                /**
                 * 添加多个对象，将字符串转JsonElement对象,并根据不同类型,执行不同操作,可输入任意非空字符串
                 */
                public RxHttpJsonArrayParam addAll(String jsonElement) {
                    param.addAll(jsonElement);
                    return this;
                }

                public RxHttpJsonArrayParam addAll(JsonArray jsonArray) {
                    param.addAll(jsonArray);
                    return this;
                }

                /**
                 * 将Json对象里面的key-value逐一取出，添加到Json数组中，成为单独的对象
                 */
                public RxHttpJsonArrayParam addAll(JsonObject jsonObject) {
                    param.addAll(jsonObject);
                    return this;
                }

                public RxHttpJsonArrayParam addJsonElement(String jsonElement) {
                    param.addJsonElement(jsonElement);
                    return this;
                }

                /**
                 * 添加一个JsonElement对象(Json对象、json数组等)
                 */
                public RxHttpJsonArrayParam addJsonElement(String key, String jsonElement) {
                    param.addJsonElement(key, jsonElement);
                    return this;
                }
            }

        """.trimIndent())
    }

    private fun generatorClass(filer: Filer, className: String, content: String) {
        try {
            val sourceFile = filer.createSourceFile("$rxHttpPackage.$className")
            BufferedWriter(sourceFile.openWriter()).use {
                it.write(content)
            }
        } catch (ignore: Exception) {
        }
    }
}