package rxhttp.wrapper;

import java.io.Closeable;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import okhttp3.Headers;
import okhttp3.HttpUrl;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.internal.Util;
import okhttp3.internal.http.StatusLine;

/**
 * 此类的作用在于兼用OkHttp版本  注意: 本类一定要用Java语言编写，kotlin将无法兼容新老版本
 * User: ljx
 * Date: 2020/5/17
 * Time: 15:28
 */
public class OkHttpCompat {

    private static String OKHTTP_USER_AGENT;

    public static void closeQuietly(Response response) {
        if (response == null) return;
        closeQuietly(response.body());
    }

    public static void closeQuietly(Closeable closeable) {
        if (closeable == null) return;
        Util.closeQuietly(closeable);
    }

    public static Request request(Response response) {
        return response.request();
    }

    public static HttpUrl url(Request request) {
        return request.url();
    }

    public static Headers headers(Response response) {
        return response.headers();
    }

    public static String header(Response response, String name) {
        return response.header(name);
    }

    public static long receivedResponseAtMillis(Response response) {
        return response.receivedResponseAtMillis();
    }

    //解析http状态行
    public static StatusLine parse(String statusLine) throws IOException {
        String okHttpUserAgent = getOkHttpUserAgent();
        if (okHttpUserAgent.compareTo("okhttp/4.0.0") >= 0) {
            return StatusLine.Companion.parse(statusLine);
        } else {
            Class<StatusLine> statusLineClass = StatusLine.class;
            try {
                Method parse = statusLineClass.getDeclaredMethod("parse", String.class);
                return (StatusLine) parse.invoke(statusLineClass, statusLine);
            } catch (Exception e) {
                throw new IOException(e.getMessage());
            }
        }
    }

    public static String getOkHttpUserAgent() {
        if (OKHTTP_USER_AGENT != null) return OKHTTP_USER_AGENT;
        try {
            //4.7.x及以上版本获取userAgent方式
            Class<?> utilClass = Class.forName("okhttp3.internal.Util");
            return OKHTTP_USER_AGENT = (String) utilClass.getDeclaredField("userAgent").get(utilClass);
        } catch (ClassNotFoundException | NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
        try {
            Class<?> versionClass = Class.forName("okhttp3.internal.Version");
            try {
                //4.x.x及以上版本获取userAgent方式
                Field userAgent = versionClass.getDeclaredField("userAgent");
                return OKHTTP_USER_AGENT = (String) userAgent.get(versionClass);
            } catch (Exception ignore) {

            }
            try {
                //4.x.x以下版本获取userAgent方式
                Method userAgent = versionClass.getDeclaredMethod("userAgent");
                return OKHTTP_USER_AGENT = (String) userAgent.invoke(versionClass);
            } catch (Exception ignore) {
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return OKHTTP_USER_AGENT = "okhttp/x.x.x";
    }
}
