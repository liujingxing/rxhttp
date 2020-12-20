package rxhttp.wrapper.param;

import android.content.Context;
import android.net.Uri;

import java.io.File;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okio.ByteString;
import rxhttp.wrapper.entity.UriRequestBody;
import rxhttp.wrapper.utils.BuildUtil;

/**
 * User: ljx
 * Date: 2019-09-11
 * Time: 11:52
 */
public class BodyParam extends AbstractBodyParam<BodyParam> {

    private RequestBody requestBody;

    /**
     * @param url    request url
     * @param method {@link Method#POST}、{@link Method#PUT}、{@link Method#DELETE}、{@link Method#PATCH}
     */
    public BodyParam(String url, Method method) {
        super(url, method);
    }

    public BodyParam setBody(RequestBody requestBody) {
        this.requestBody = requestBody;
        return this;
    }

    public <T> BodyParam setJsonBody(T object) {
        this.requestBody = convert(object);
        return this;
    }

    public BodyParam setBody(MediaType mediaType, String content) {
        requestBody = RequestBody.create(mediaType, content);
        return this;
    }

    public BodyParam setBody(MediaType mediaType, ByteString content) {
        requestBody = RequestBody.create(mediaType, content);
        return this;
    }

    public BodyParam setBody(MediaType mediaType, byte[] content) {
        requestBody = RequestBody.create(mediaType, content);
        return this;
    }

    public BodyParam setBody(MediaType mediaType, byte[] content, int offset, int byteCount) {
        requestBody = RequestBody.create(mediaType, content, offset, byteCount);
        return this;
    }

    public BodyParam setBody(File file) {
        return setBody(BuildUtil.getMediaType(file.getName()), file);
    }

    public BodyParam setBody(MediaType mediaType, File file) {
        requestBody = RequestBody.create(mediaType, file);
        return this;
    }

    public BodyParam setBody(Context context, Uri uri) {
        requestBody = new UriRequestBody(context, uri);
        return this;
    }

    public BodyParam setBody(Context context, MediaType contentType, Uri uri) {
        requestBody = new UriRequestBody(context, uri, contentType);
        return this;
    }

    @Override
    public RequestBody getRequestBody() {
        return requestBody;
    }

    @Override
    public BodyParam add(String key, Object value) {
        return this;
    }
}
