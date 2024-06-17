package rxhttp.wrapper.param;

import okhttp3.RequestBody;
import rxhttp.wrapper.callback.ProgressCallback;
import rxhttp.wrapper.callback.ProgressCallbackHelper;
import rxhttp.wrapper.progress.ProgressRequestBody;

/**
 * User: ljx
 * Date: 2020-09-07
 * Time: 15:08
 */
public abstract class AbstractBodyParam<P extends AbstractBodyParam<P>> extends AbstractParam<P> {

    //Upload progress callback
    private ProgressCallbackHelper callback;

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
        //Wrap RequestBody if callback not null
        return callback != null ? new ProgressRequestBody(requestBody, callback) : requestBody;
    }

    public final P setProgressCallback(int minPeriod, ProgressCallback callback) {
        this.callback = new ProgressCallbackHelper(minPeriod, callback);
        return self();
    }
}
