package rxhttp.wrapper.param;

import android.content.Context;
import android.net.Uri;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.MultipartBody.Part;
import okhttp3.RequestBody;
import rxhttp.wrapper.annotations.NonNull;
import rxhttp.wrapper.annotations.Nullable;
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
    
    public RxHttpFormParam addAllEncoded(@NonNull Map<String, ?> map) {
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

    /**
     * @deprecated please user {@link #addFile(String, File)} instead
     */
    @Deprecated
    public RxHttpFormParam add(String key, File file) {
        param.addFile(key, file);
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

    /**
     * @deprecated please user {@link #addFiles(List)} instead
     */
    @Deprecated
    public RxHttpFormParam addFile(List<? extends UpFile> fileList) {
        return addFiles(fileList);
    }
    
    /**
     * @deprecated please user {@link #addFiles(String, List)} instead
     */
    @Deprecated
    public <T> RxHttpFormParam addFile(String key, List<T> fileList) {
        return addFiles(key, fileList);
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
