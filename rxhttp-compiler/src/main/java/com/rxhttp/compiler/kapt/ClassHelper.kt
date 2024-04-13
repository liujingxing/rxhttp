package com.rxhttp.compiler.kapt

import com.rxhttp.compiler.common.getObservableClass
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

    private fun generatorRxHttpAbstractBodyParam(filer: Filer) {
        generatorClass(
            filer, "RxHttpAbstractBodyParam", """
                package $rxHttpPackage;
                
                import rxhttp.wrapper.BodyParamFactory;
                import rxhttp.wrapper.param.AbstractBodyParam;

                /**
                 * Github
                 * https://github.com/liujingxing/rxhttp
                 * https://github.com/liujingxing/rxlife
                 * https://github.com/liujingxing/rxhttp/wiki/FAQ
                 * https://github.com/liujingxing/rxhttp/wiki/更新日志
                 */
                public class RxHttpAbstractBodyParam<P extends AbstractBodyParam<P>, R extends RxHttpAbstractBodyParam<P, R>> 
                    extends RxHttp<P, R> implements BodyParamFactory {

                    RxHttpAbstractBodyParam(P param) {
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
                RxHttpNoBodyParam(NoBodyParam param) {
                    super(param);
                }
                
                public RxHttpNoBodyParam add(String key, Object value) {
                    return addQuery(key, value);
                }
                
                public RxHttpNoBodyParam add(String key, Object value, boolean add) {
                    if (add) addQuery(key, value);
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
            import rxhttp.wrapper.entity.UriRequestBody;
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
                RxHttpBodyParam(BodyParam param) {
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
                    return setBody(new UriRequestBody(context, uri, 0, contentType));
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
            ${isAndroid("import rxhttp.wrapper.entity.UriRequestBody;")}
            
            import org.jetbrains.annotations.Nullable;

            import java.io.File;
            import java.util.List;
            import java.util.Map;
            import java.util.Map.Entry;

            import okhttp3.Headers;
            import okhttp3.MediaType;
            import okhttp3.MultipartBody;
            import okhttp3.RequestBody;
            import rxhttp.wrapper.OkHttpCompat;
            import rxhttp.wrapper.entity.FileRequestBody;
            import rxhttp.wrapper.entity.UpFile;
            import rxhttp.wrapper.param.FormParam;
            import rxhttp.wrapper.utils.BuildUtil;
            import rxhttp.wrapper.utils.UriUtil;

            /**
             * Github
             * https://github.com/liujingxing/rxhttp
             * https://github.com/liujingxing/rxlife
             * https://github.com/liujingxing/rxhttp/wiki/FAQ
             * https://github.com/liujingxing/rxhttp/wiki/更新日志
             */
            public class RxHttpFormParam extends RxHttpAbstractBodyParam<FormParam, RxHttpFormParam> {
                RxHttpFormParam(FormParam param) {
                    super(param);
                }

                public RxHttpFormParam add(String key, @Nullable Object value) {
                    param.add(key, value);
                    return this;
                }

                public RxHttpFormParam add(String key, @Nullable Object value, boolean add) {
                    if (add) param.add(key,value);
                    return this;
                }
                
                public RxHttpFormParam addAll(Map<String, ?> map) {
                    param.addAll(map);
                    return this;
                }

                public RxHttpFormParam addEncoded(String key, @Nullable Object value) {
                    param.addEncoded(key, value);
                    return this;
                }

                public RxHttpFormParam addAllEncoded(Map<String, ?> map) {
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

                public RxHttpFormParam set(String key, @Nullable Object value) {
                    param.set(key, value);
                    return this;
                }

                public RxHttpFormParam setEncoded(String key, @Nullable Object value) {
                    param.setEncoded(key, value);
                    return this;
                }

                public RxHttpFormParam addFile(String key, @Nullable File file) {
                    if (file == null) return this;
                    return addFile(key, file, file.getName());
                }

                public RxHttpFormParam addFile(String key, @Nullable String filePath) {
                    if (filePath == null) return this;
                    return addFile(key, new File(filePath));
                }

                public RxHttpFormParam addFile(String key, @Nullable File file, @Nullable String filename) {
                    if (file == null) return this;
                    return addFile(new UpFile(key, file, filename));
                }

                public RxHttpFormParam addFile(UpFile upFile) {
                    RequestBody requestBody = new FileRequestBody(upFile.getFile(), upFile.getSkipSize(),
                        BuildUtil.getMediaType(upFile.getFilename()));
                    return addFormDataPart(upFile.getKey(), upFile.getFilename(), requestBody);
                }

                public RxHttpFormParam addFiles(List<UpFile> files) {
                    for (UpFile file : files) {
                        addFile(file);
                    }
                    return this;
                }

                public <T> RxHttpFormParam addFiles(Map<String, T> fileMap) {
                    for (Map.Entry<String, T> entry : fileMap.entrySet()) {
                        addFile(entry.getKey(), entry.getValue());
                    }
                    return this;
                }

                public <T> RxHttpFormParam addFiles(String key, List<T> files) {
                    for (T file : files) {
                        addFile(key, file);
                    }
                    return this;
                }

                private void addFile(String key, @Nullable Object file){
                    if (file instanceof File) {
                        addFile(key, (File) file);
                    } else if (file instanceof String) {
                        addFile(key, file.toString());
                    } else if (file != null){
                        throw new IllegalArgumentException("Incoming data type exception, it must be String or File");
                    }
                }

                public RxHttpFormParam addPart(byte[] content, @Nullable MediaType contentType) {
                    return addPart(content, contentType, 0, content.length);
                }

                public RxHttpFormParam addPart(byte[] content, @Nullable MediaType contentType, int offset,
                                               int byteCount) {
                    return addPart(OkHttpCompat.create(contentType, content, offset, byteCount));
                }
                ${isAndroid("""
                public RxHttpFormParam addPart(Context context, Uri uri) {
                    return addPart(context, uri, BuildUtil.getMediaTypeByUri(context, uri));
                }

                public RxHttpFormParam addPart(Context context, Uri uri, @Nullable MediaType contentType) {
                    return addPart(new UriRequestBody(context, uri, 0, contentType));
                }

                public RxHttpFormParam addPart(Context context, String key, Uri uri) {
                    return addPart(context, key, UriUtil.displayName(uri, context), uri);
                }

                public RxHttpFormParam addPart(Context context, String key, String fileName, Uri uri) {
                    return addPart(context, key, fileName, uri, BuildUtil.getMediaTypeByUri(context, uri));
                }

                public RxHttpFormParam addPart(Context context, String key, Uri uri,
                                               @Nullable MediaType contentType) {
                    return addPart(context, key, UriUtil.displayName(uri, context), uri, contentType);
                }

                public RxHttpFormParam addPart(Context context, String key, String filename, Uri uri,
                                               @Nullable MediaType contentType) {
                    return addFormDataPart(key, filename, new UriRequestBody(context, uri, 0, contentType));
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
                public RxHttpFormParam addPart(RequestBody requestBody) {
                    return addPart(OkHttpCompat.part(requestBody));
                }

                public RxHttpFormParam addPart(@Nullable Headers headers, RequestBody requestBody) {
                    return addPart(OkHttpCompat.part(headers, requestBody));
                }

                public RxHttpFormParam addFormDataPart(String key, @Nullable String fileName, RequestBody requestBody) {
                    return addPart(OkHttpCompat.part(key, fileName, requestBody));
                }
                
                public RxHttpFormParam addPart(String key, RequestBody requestBody) {
                    return addFormDataPart(key, null, requestBody);
                }
                
                public RxHttpFormParam addParts(Map<String, RequestBody> partMap) {
                    for (Entry<String, RequestBody> entry : partMap.entrySet()) {
                        addPart(entry.getKey(), entry.getValue());
                    }
                    return this;
                }
                
                public RxHttpFormParam addPart(MultipartBody.Part part) {
                    param.addPart(part);
                    return this;
                }
                
                public RxHttpFormParam addParts(List<MultipartBody.Part> parts) {
                    for (MultipartBody.Part part: parts) {
                        addPart(part);
                    }
                    return this;
                }

                //Set content-type to multipart/form-data
                public RxHttpFormParam setMultiForm() {
                    return setMultiType(MultipartBody.FORM);
                }

                //Set content-type to multipart/mixed
                public RxHttpFormParam setMultiMixed() {
                    return setMultiType(MultipartBody.MIXED);
                }

                //Set content-type to multipart/alternative
                public RxHttpFormParam setMultiAlternative() {
                    return setMultiType(MultipartBody.ALTERNATIVE);
                }

                //Set content-type to multipart/digest
                public RxHttpFormParam setMultiDigest() {
                    return setMultiType(MultipartBody.DIGEST);
                }

                //Set content-type to multipart/parallel
                public RxHttpFormParam setMultiParallel() {
                    return setMultiType(MultipartBody.PARALLEL);
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
            
            import org.jetbrains.annotations.Nullable;

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
                RxHttpJsonParam(JsonParam param) {
                    super(param);
                }

                public RxHttpJsonParam add(String key, @Nullable Object value) {
                    param.add(key,value);
                    return this;
                }
                
                public RxHttpJsonParam add(String key, @Nullable Object value, boolean add) {
                    if (add) param.add(key,value);
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
            
            import org.jetbrains.annotations.Nullable;

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
                RxHttpJsonArrayParam(JsonArrayParam param) {
                    super(param);
                }

                public RxHttpJsonArrayParam add(String key, @Nullable Object value) {
                    param.add(key,value);
                    return this;
                }
                
                public RxHttpJsonArrayParam add(String key, @Nullable Object value, boolean add) {
                    if (add) param.add(key,value);
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