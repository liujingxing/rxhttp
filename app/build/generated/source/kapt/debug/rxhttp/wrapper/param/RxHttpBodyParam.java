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
 * https://github.com/liujingxing/RxHttp
 * https://github.com/liujingxing/RxLife
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
    
    public RxHttpBodyParam setBody(File file, @Nullable MediaType mediaType) {
        param.setBody(file, mediaType);
        return this;
    }
    
    public RxHttpBodyParam setBody(Context context, Uri uri) {
        param.setBody(context, uri);
        return this;
    }
    
    public RxHttpBodyParam setBody(Context context, Uri uri, @Nullable MediaType contentType) {
        param.setBody(context, uri, contentType);
        return this;
    }
    
    public <T> RxHttpBodyParam setJsonBody(T object) {
        param.setJsonBody(object);
        return this;
    }
}
