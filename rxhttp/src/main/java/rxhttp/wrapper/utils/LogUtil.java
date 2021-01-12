package rxhttp.wrapper.utils;

import java.io.EOFException;
import java.io.IOException;
import java.nio.charset.Charset;

import kotlin.text.Charsets;
import okhttp3.Headers;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.MultipartBody.Part;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.Buffer;
import okio.BufferedSource;
import rxhttp.Platform;
import rxhttp.RxHttpPlugins;
import rxhttp.internal.RxHttpVersion;
import rxhttp.wrapper.OkHttpCompat;
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
    //日志长度超出logcat单条日志打印长度时，是否分段打印，默认false
    private static boolean isSegmentPrint = false;

    public static void setDebug(boolean debug) {
        setDebug(debug, false);
    }

    public static void setDebug(boolean debug, boolean segmentPrint) {
        isDebug = debug;
        isSegmentPrint = segmentPrint;
    }

    public static boolean isDebug() {
        return isDebug;
    }

    public static boolean isSegmentPrint() {
        return isSegmentPrint;
    }

    //打印Http请求连接失败异常日志
    public static void log(Throwable throwable) {
        if (!isDebug) return;
        Platform.get().loge(TAG_RXJAVA, throwable.toString());
    }

    //打印Http请求连接失败异常日志
    public static void log(String url, Throwable throwable) {
        if (!isDebug) return;
        try {
            throwable.printStackTrace();
            StringBuilder builder = new StringBuilder(throwable.toString());
            if (!(throwable instanceof ParseException) && !(throwable instanceof HttpStatusCodeException)) {
                builder.append("\n\n").append(url);
            }
            Platform.get().loge(TAG, builder.toString());
        } catch (Throwable e) {
            Platform.get().logd(TAG, "Request error Log printing failed", e);
        }
    }

    //请求前，打印日志
    public static void log(@NonNull Request request) {
        if (!isDebug) return;
        try {
            StringBuilder builder = new StringBuilder("<------ ")
                .append(RxHttpVersion.userAgent).append(" ")
                .append(OkHttpCompat.getOkHttpUserAgent())
                .append(" request start ------>\n")
                .append(request.method())
                .append(" ").append(request.url())
                .append("\n").append(request.headers());
            RequestBody body = request.body();
            if (body != null) {
                builder.append("Content-Type: ").append(body.contentType());
                builder.append("\nContent-Length: ").append(body.contentLength());
                builder.append("\n\n").append(requestBody2Str(body));
            }
            Platform.get().logd(TAG, builder.toString());
        } catch (Throwable e) {
            Platform.get().logd(TAG, "Request start log printing failed", e);
        }
    }

    //打印Http返回的正常结果
    public static void log(@NonNull Response response, String body) {
        if (!isDebug) return;
        try {
            Request request = response.request();
            LogTime logTime = request.tag(LogTime.class);
            long tookMs = logTime != null ? logTime.tookMs() : 0;
            String result = body != null ? body :
                getResult(OkHttpCompat.requireBody(response), OkHttpCompat.needDecodeResult(response));
            StringBuilder builder = new StringBuilder("<------ ")
                .append(RxHttpVersion.userAgent).append(" ")
                .append(OkHttpCompat.getOkHttpUserAgent())
                .append(" request end ------>\n")
                .append(request.method()).append(" ").append(getEncodedUrlAndParams(request))
                .append("\n\n").append(response.protocol()).append(" ")
                .append(response.code()).append(" ").append(response.message())
                .append(tookMs > 0 ? " " + tookMs + "ms" : "")
                .append("\n").append(response.headers())
                .append("\n").append(result);
            Platform.get().logi(TAG, builder.toString());
        } catch (Throwable e) {
            Platform.get().logd(TAG, "Request end Log printing failed", e);
        }
    }

    public static String getEncodedUrlAndParams(Request request) {
        RequestBody body = request.body();
        HttpUrl url = request.url();
        try {
            if (body != null) {
                return url + "\n\n" + requestBody2Str(body);
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return url.toString();
    }

    private static String requestBody2Str(@NonNull RequestBody body) throws IOException {
        if (body instanceof ProgressRequestBody) {
            body = ((ProgressRequestBody) body).getRequestBody();
        }
        if (body instanceof MultipartBody) {
            return multipartBody2Str((MultipartBody) body);
        }
        Buffer buffer = new Buffer();
        body.writeTo(buffer);
        if (!isProbablyUtf8(buffer)) {
            return "(binary " + body.contentLength() + "-byte body omitted)";
        } else {
            return buffer.readString(getCharset(body));
        }
    }

    private static String multipartBody2Str(MultipartBody multipartBody) {
        final byte[] colonSpace = {':', ' '};
        final byte[] CRLF = {'\r', '\n'};
        final byte[] dashDash = {'-', '-'};
        Buffer sink = new Buffer();
        for (Part part : multipartBody.parts()) {
            Headers headers = part.headers();
            RequestBody body = part.body();
            sink.write(dashDash)
                .writeUtf8(multipartBody.boundary())
                .write(CRLF);
            if (headers != null) {
                for (int i = 0, size = headers.size(); i < size; i++) {
                    sink.writeUtf8(headers.name(i))
                        .write(colonSpace)
                        .writeUtf8(headers.value(i))
                        .write(CRLF);
                }
            }
            MediaType contentType = body.contentType();
            if (contentType != null) {
                sink.writeUtf8("Content-Type: ")
                    .writeUtf8(contentType.toString())
                    .write(CRLF);
            }
            long contentLength = -1;
            try {
                contentLength = body.contentLength();
            } catch (IOException e) {
                e.printStackTrace();
            }
            sink.writeUtf8("Content-Length: ")
                .writeDecimalLong(contentLength)
                .write(CRLF);

            if (contentLength > 1024) {
                sink.writeUtf8("(binary " + contentLength + "-byte body omitted)");
            } else {
                if (body instanceof MultipartBody) {
                    sink.write(CRLF)
                        .writeUtf8(multipartBody2Str((MultipartBody) body));
                } else {
                    try {
                        body.writeTo(sink);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            if (contentLength > 0) sink.write(CRLF);
            sink.write(CRLF);
        }
        sink.write(dashDash)
            .writeUtf8(multipartBody.boundary())
            .write(dashDash);
        return sink.readString(getCharset(multipartBody));
    }

    private static String getResult(ResponseBody body, boolean onResultDecoder) throws IOException {
        BufferedSource source = body.source();
        source.request(Long.MAX_VALUE); // Buffer the entire body.
        Buffer buffer = source.buffer();
        String result;
        if (isProbablyUtf8(buffer)) {
            result = buffer.clone().readString(getCharset(body));
            if (onResultDecoder) {
                result = RxHttpPlugins.onResultDecoder(result);
            }
        } else {
            result = "(binary " + buffer.size() + "-byte body omitted)";
        }
        return result;
    }

    private static boolean isProbablyUtf8(Buffer buffer) {
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

    private static Charset getCharset(RequestBody requestBody) {
        MediaType mediaType = requestBody.contentType();
        return mediaType != null ? mediaType.charset(Charsets.UTF_8) : Charsets.UTF_8;
    }

    private static Charset getCharset(ResponseBody responseBody) {
        MediaType mediaType = responseBody.contentType();
        return mediaType != null ? mediaType.charset(Charsets.UTF_8) : Charsets.UTF_8;
    }
}
