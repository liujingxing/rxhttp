package rxhttp.wrapper.progress;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Response;
import rxhttp.wrapper.callback.ProgressCallback;

/**
 * 下载进度拦截器
 * User: ljx
 * Date: 2019/1/20
 * Time: 14:19
 */
public class ProgressInterceptor implements Interceptor {

    private ProgressCallback progressCallback;

    public ProgressInterceptor(ProgressCallback progressCallback) {
        this.progressCallback = progressCallback;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        //拦截
        Response originalResponse = chain.proceed(chain.request());
        //包装响应体并返回
        return originalResponse.newBuilder()
            .body(new ProgressResponseBody(originalResponse, progressCallback))
            .build();
    }
}
