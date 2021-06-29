package rxhttp.wrapper.param;

import android.content.Context;
import android.net.Uri;

import rxhttp.wrapper.annotations.Nullable;
import rxhttp.wrapper.param.BodyParam;

import java.io.File;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okio.ByteString;

/**
 * Github
 * https://github.com/liujingxing/rxhttp
 * https://github.com/liujingxing/rxlife
 */
public class RxHttpBodyParam extends RxHttpAbstractBodyParam<BodyParam, RxHttpBodyParam> {
    public RxHttpBodyParam(BodyParam param) {
        super(param);
    }
    
    public RxHttpBodyParam setBody(RequestBody requestBody) {
        param.setBody(requestBody);
        return this;
    }
    
    public RxHttpBodyParam setBody(String content, @Nullable MediaType mediaType) {
        param.setBody(content, mediaType);
        return this;
    }
    
    public RxHttpBodyParam setBody(ByteString content, @Nullable MediaType mediaType) {
        param.setBody(content, mediaType);
        return this;
    }
    
    public RxHttpBodyParam setBody(byte[] content, @Nullable MediaType mediaType) {
        param.setBody(content, mediaType);
        return this;
    }
    
    public RxHttpBodyParam setBody(byte[] content, @Nullable MediaType mediaType, int offset, int byteCount) {
        param.setBody(content, mediaType, offset, byteCount);
        return this;
    }
    
    public RxHttpBodyParam setBody(File file) {
        param.setBody(file);
        return this;
    }
    
    public RxHttpBodyParam setBody(File file, long skipSize) {
        param.setBody(file, skipSize);
        return this;
    }
    
    public RxHttpBodyParam setBody(File file, long skipSize, @Nullable MediaType mediaType) {
        param.setBody(file, skipSize, mediaType);
        return this;
    }
    
    public RxHttpBodyParam setBody(File file, @Nullable MediaType mediaType) {
        param.setBody(file, 0, mediaType);
        return this;
    }
    
    public RxHttpBodyParam setBody(Uri uri, Context context) {
        param.setBody(uri, context);
        return this;
    }
    
    public RxHttpBodyParam setBody(Uri uri, Context context, long skipSize) {
        param.setBody(uri, context, skipSize);
        return this;
    }
    
    public RxHttpBodyParam setBody(Uri uri, Context context, long skipSize, @Nullable MediaType contentType) {
        param.setBody(uri, context, skipSize, contentType);
        return this;
    }
    
    public RxHttpBodyParam setBody(Uri uri, Context context, @Nullable MediaType contentType) {
        param.setBody(uri, context, 0, contentType);
        return this;
    }
    
    public RxHttpBodyParam setJsonBody(Object object) {
        param.setJsonBody(object);
        return this;
    }
}
