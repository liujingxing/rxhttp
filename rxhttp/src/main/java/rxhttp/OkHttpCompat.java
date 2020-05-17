package rxhttp;

import java.io.Closeable;

import okhttp3.Headers;
import okhttp3.HttpUrl;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.internal.Util;
import okio.BufferedSink;
import okio.BufferedSource;
import okio.ByteString;
import okio.Okio;
import okio.Sink;
import okio.Source;

/**
 * 此类的作用在于兼用OkHttp版本  注意: 本类一定要用Java语言编写，kotlin将无法兼容新老版本
 * User: ljx
 * Date: 2020/5/17
 * Time: 15:28
 */
public class OkHttpCompat {

    public static BufferedSource buffer(Source source) {
        return Okio.buffer(source);
    }

    public static BufferedSink buffer(Sink sink) {
        return Okio.buffer(sink);
    }

    public static ByteString encodeUtf8(String s) {
        return ByteString.encodeUtf8(s);
    }

    public static void closeQuietly(Closeable closeable) {
        Util.closeQuietly(closeable);
    }

    public static Request request(Response response) {
        return response.request();
    }

    public static String host(HttpUrl url) {
        return url.host();
    }

    public static HttpUrl url(Request request) {
        return request.url();
    }

    public static Headers headers(Response response) {
        return response.headers();
    }

    public static long receivedResponseAtMillis(Response response) {
        return response.receivedResponseAtMillis();
    }
}
