package rxhttp.wrapper.intercept;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import rxhttp.wrapper.callback.OutputStreamFactory;
import rxhttp.wrapper.entity.DownloadOffSize;

/**
 * User: ljx
 * Date: 2022/10/18
 * Time: 21:41
 */
public class RangeInterceptor implements Interceptor {
    @NotNull
    @Override
    public Response intercept(@NotNull Chain chain) throws IOException {
        Request request = chain.request();
        OutputStreamFactory<?> osFactory = request.tag(OutputStreamFactory.class);
        long startIndex;
        if (osFactory != null && (startIndex = osFactory.offsetSize()) >= 0) {
            String rangeHeader = "bytes=" + startIndex + "-";
            request = request.newBuilder()
                .addHeader("Range", rangeHeader)
                .tag(DownloadOffSize.class, new DownloadOffSize(startIndex))
                .build();
        }
        return chain.proceed(request);
    }
}
