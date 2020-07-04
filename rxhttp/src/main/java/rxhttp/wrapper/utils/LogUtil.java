package rxhttp.wrapper.utils;


import java.io.EOFException;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.util.List;

import kotlin.text.Charsets;
import okhttp3.Headers;
import okhttp3.HttpUrl.Builder;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.Buffer;
import okio.BufferedSource;
import rxhttp.Platform;
import rxhttp.RxHttpPlugins;
import rxhttp.wrapper.OkHttpCompat;
import rxhttp.internal.RxHttpVersion;
import rxhttp.wrapper.annotations.NonNull;
import rxhttp.wrapper.exception.HttpStatusCodeException;
import rxhttp.wrapper.exception.ParseException;
import rxhttp.wrapper.progress.ProgressRequestBody;

/**
 * User: ljx
 * Date: 2019/4/1
 * Time: 17:21
 */
public class LogUtil {

    private static final String TAG = "RxHttp";
    private static final String TAG_RXJAVA = "RxJava";

    private static boolean isDebug = false;

    public static void setDebug(boolean debug) {
        isDebug = debug;
    }

    public static boolean isIsDebug() {
        return isDebug;
    }

    //打印Http请求连接失败异常日志
    public static void log(Throwable throwable) {
        if (!isDebug) return;
        Platform.get().loge(TAG_RXJAVA, throwable.toString());
    }

    //打印Http请求连接失败异常日志
    @SuppressWarnings("deprecation")
    public static void log(String url, Throwable throwable) {
        if (!isDebug) return;
        try {
            throwable.printStackTrace();
            StringBuilder builder = new StringBuilder()
                .append(throwable.toString());
            if (!(throwable instanceof ParseException) && !(throwable instanceof HttpStatusCodeException)) {
                builder.append("\n\n")
                    .append(URLDecoder.decode(url));
            }
            Platform.get().loge(TAG, builder.toString());
        } catch (Throwable e) {
            Platform.get().logd(TAG, "Request error Log printing failed", e);
        }
    }

    //打印Http返回的正常结果
    public static void log(@NonNull Response response, boolean onResultDecoder, String downloadPath) {
        if (!isDebug) return;
        try {
            Request request = response.request();
            LogTime logTime = request.tag(LogTime.class);
            long tookMs = logTime != null ? logTime.tookMs() : 0;
            String result = downloadPath != null ? downloadPath : getResult(response.body(), onResultDecoder);
            StringBuilder builder = new StringBuilder()
                .append("<------ ")
                .append(RxHttpVersion.userAgent + " " + OkHttpCompat.getOkHttpUserAgent())
                .append(" request end Method=")
                .append(request.method())
                .append(" Code=").append(response.code())
                .append(" ------>");
            if (tookMs > 0) {
                builder.append("(").append(tookMs).append("ms)");
            }
            builder.append("\n\n").append(getEncodedUrlAndParams(request))
                .append("\n\n").append(response.headers())
                .append("\n").append(result);
            Platform.get().logi(TAG, builder.toString());
        } catch (Throwable e) {
            Platform.get().logd(TAG, "Request end Log printing failed", e);
        }
    }

    //请求前，打印日志
    public static void log(@NonNull Request request) {
        if (!isDebug) return;
        try {
            String builder = "<------ " + RxHttpVersion.userAgent + " " + OkHttpCompat.getOkHttpUserAgent() + " request start Method=" +
                request.method() + " ------>" +
                request2Str(request);
            Platform.get().logd(TAG, builder);
        } catch (Throwable e) {
            Platform.get().logd(TAG, "Request start log printing failed", e);
        }
    }

    @SuppressWarnings("deprecation")
    public static String getEncodedUrlAndParams(Request request) {
        String result;
        try {
            result = getRequestParams(request);
        } catch (Throwable e) {
            e.printStackTrace();
            result = request.url().toString();
        }
        try {
            return URLDecoder.decode(result);
        } catch (Throwable e) {
            return result;
        }
    }

    private static String request2Str(Request request) {
        StringBuilder builder = new StringBuilder();
        builder.append("\n\n")
            .append(getEncodedUrlAndParams(request));
        RequestBody body = request.body();
        if (body != null) {
            builder.append("\n\nContent-Type: ").append(body.contentType());
            try {
                builder.append("\nContent-Length: ").append(body.contentLength());
            } catch (IOException ignore) {
            }
        }
        builder.append(body != null ? "\n" : "\n\n").append(request.headers());
        return builder.toString();
    }

    private static String getRequestParams(Request request) throws IOException {
        RequestBody body = request.body();
        if (body instanceof ProgressRequestBody) {
            body = ((ProgressRequestBody) body).getRequestBody();
        }
        Builder urlBuilder = request.url().newBuilder();

        if (body instanceof MultipartBody) {
            MultipartBody multipartBody = (MultipartBody) body;
            List<MultipartBody.Part> parts = multipartBody.parts();
            StringBuilder fileBuilder = new StringBuilder();
            for (int i = 0, size = parts.size(); i < size; i++) {
                MultipartBody.Part part = parts.get(i);
                RequestBody requestBody = part.body();
                Headers headers = part.headers();
                if (headers == null || headers.size() == 0) continue;
                String[] split = headers.value(0).split(";");
                String name = null, fileName = null;
                for (String s : split) {
                    if (s.equals("form-data")) continue;
                    String[] keyValue = s.split("=");
                    if (keyValue.length < 2) continue;
                    String value = keyValue[1].substring(1, keyValue[1].length() - 1);
                    if (name == null) {
                        name = value;
                    } else {
                        fileName = value;
                        break;
                    }
                }
                if (name == null) continue;
                if (requestBody.contentLength() < 1024) {
                    Buffer buffer = new Buffer();
                    requestBody.writeTo(buffer);
                    String value = buffer.readUtf8();
                    urlBuilder.addQueryParameter(name, value);
                } else {
                    if (fileBuilder.length() > 0) {
                        fileBuilder.append("&");
                    }
                    fileBuilder.append(name).append("=").append(fileName);
                }
            }
            return urlBuilder.toString() + "\n\nfiles = " + fileBuilder.toString();
        }

        if (body != null) {
            Buffer buffer = new Buffer();
            body.writeTo(buffer);
            if (!isPlaintext(buffer)) {
                return urlBuilder.toString() + "\n\n(binary "
                    + body.contentLength() + "-byte body omitted)";
            } else {
                return urlBuilder.toString() + "\n\n" + buffer.readUtf8();
            }
        }
        return urlBuilder.toString();
    }

    @SuppressWarnings("deprecation")
    private static String getResult(ResponseBody body, boolean onResultDecoder) throws IOException {
        BufferedSource source = body.source();
        source.request(Long.MAX_VALUE); // Buffer the entire body.
        Buffer buffer = source.buffer();
        String result;
        if (isPlaintext(buffer)) {
            Charset UTF_8 = null;
            MediaType contentType = body.contentType();
            if (contentType != null) {
                UTF_8 = contentType.charset();
            }
            if (UTF_8 == null) {
                UTF_8 = Charsets.UTF_8;
            }
            result = buffer.clone().readString(UTF_8);
            if (onResultDecoder) {
                result = RxHttpPlugins.onResultDecoder(result);
            }
        } else {
            result = "(binary " + buffer.size() + "-byte body omitted)";
        }
        return result;
    }


    private static boolean isPlaintext(Buffer buffer) {
        try {
            Buffer prefix = new Buffer();
            long byteCount = buffer.size() < 64 ? buffer.size() : 64;
            buffer.copyTo(prefix, 0, byteCount);
            for (int i = 0; i < 16; i++) {
                if (prefix.exhausted()) {
                    break;
                }
                int codePoint = prefix.readUtf8CodePoint();
                if (Character.isISOControl(codePoint) && !Character.isWhitespace(codePoint)) {
                    return false;
                }
            }
            return true;
        } catch (EOFException e) {
            return false; // Truncated UTF-8 sequence.
        }
    }
}
