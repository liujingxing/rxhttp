package rxhttp.wrapper.param;

import java.io.IOException;

import okhttp3.RequestBody;
import rxhttp.wrapper.callback.ProgressCallback;
import rxhttp.wrapper.progress.ProgressRequestBody;

/**
 * User: ljx
 * Date: 2020-09-07
 * Time: 15:08
 */
@SuppressWarnings("unchecked")
public abstract class AbstractBodyParam<P extends AbstractBodyParam<P>> extends AbstractParam<P> {

    //Upload progress callback
    private ProgressCallback mCallback;
    //Upload max length
    private long uploadMaxLength = Long.MAX_VALUE;

    /**
     * @param url    request url
     * @param method {@link Method#POST}、{@link Method#PUT}、{@link Method#DELETE}、{@link Method#PATCH}
     */
    public AbstractBodyParam(String url, Method method) {
        super(url, method);
    }

    @Override
    public final RequestBody buildRequestBody() {
        RequestBody requestBody = getRequestBody();
        try {
            long contentLength = requestBody.contentLength();
            if (contentLength > uploadMaxLength)
                throw new IllegalArgumentException("The contentLength cannot be greater than " + uploadMaxLength + " bytes, " +
                    "the current contentLength is " + contentLength + " bytes");
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
        final ProgressCallback callback = mCallback;
        if (callback != null) {
            //如果设置了进度回调，则对RequestBody进行装饰
            return new ProgressRequestBody(requestBody, callback);
        }
        return requestBody;
    }

    public final P setProgressCallback(ProgressCallback callback) {
        mCallback = callback;
        return (P) this;
    }

    public P setUploadMaxLength(long maxLength) {
        uploadMaxLength = maxLength;
        return (P) this;
    }

}
