package rxhttp.wrapper.param;

import android.content.Context;
import android.net.Uri;

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

    public <T> RxHttpBodyParam setJsonBody(T object) {
        param.setJsonBody(object);
        return this;
    }

    public RxHttpBodyParam setBody(MediaType mediaType, String content) {
        param.setBody(mediaType, content);
        return this;
    }

    public RxHttpBodyParam setBody(MediaType mediaType, ByteString content) {
        param.setBody(mediaType, content);
        return this;
    }

    public RxHttpBodyParam setBody(MediaType mediaType, byte[] content) {
        param.setBody(mediaType, content);
        return this;
    }

    public RxHttpBodyParam setBody(MediaType mediaType, byte[] content, int offset, int byteCount) {
        param.setBody(mediaType, content, offset, byteCount);
        return this;
    }

    public RxHttpBodyParam setBody(File file) {
        param.setBody(file);
        return this;
    }
    
    public RxHttpBodyParam setBody(MediaType mediaType, File file) {
        param.setBody(mediaType, file);
        return this;
    }

    public RxHttpBodyParam setBody(Context context, Uri uri) {
        param.setBody(context, uri);
        return this;
    }

    public RxHttpBodyParam setBody(Context context, MediaType contentType, Uri uri) {
        param.setBody(context, contentType, uri);
        return this;
    }
}
