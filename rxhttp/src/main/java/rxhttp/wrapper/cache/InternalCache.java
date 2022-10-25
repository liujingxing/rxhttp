package rxhttp.wrapper.cache;

import org.jetbrains.annotations.Nullable;

import java.io.IOException;

import okhttp3.Request;
import okhttp3.Response;

/**
 * RxHttp's internal cache interface. Applications shouldn't implement this: instead use {@link CacheManager}.
 */
public interface InternalCache {
    @Nullable
    Response get(Request request, String key) throws IOException;

    Response put(Response response, String key) throws IOException;

    void remove(String key) throws IOException;

    void removeAll() throws IOException;

    long size() throws IOException;
}
