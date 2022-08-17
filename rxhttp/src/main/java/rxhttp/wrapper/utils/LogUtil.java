package rxhttp.wrapper.utils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.EOFException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;

import kotlin.text.Charsets;
import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.Headers;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.MultipartBody.Part;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.internal.http.HttpHeaders;
import okio.Buffer;
import okio.BufferedSource;
import rxhttp.Platform;
import rxhttp.RxHttpPlugins;
import rxhttp.internal.RxHttpVersion;
import rxhttp.wrapper.OkHttpCompat;
import rxhttp.wrapper.annotations.NonNull;
import rxhttp.wrapper.entity.FileRequestBody;
import rxhttp.wrapper.entity.UriRequestBody;
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
    private static int indentSpaces = -1; //json数据缩进空间，默认不缩进

    public static void setDebug(boolean debug) {
        setDebug(debug, false, -1);
    }

    /**
     * 调试模式下，会有完整的请求日志，过滤`RxHttp`即可看到
     *
     * @param debug        是否开始调试模式
     * @param segmentPrint 是否开启分段打印
     * @param indentSpaces json数据缩进空间，大于等于0时，json数据将格式化输出
     */
    public static void setDebug(boolean debug, boolean segmentPrint, int indentSpaces) {
        isDebug = debug;
        isSegmentPrint = segmentPrint;
        LogUtil.indentSpaces = indentSpaces;
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

    public static void log(String msg) {
        if (!isDebug()) return;
        Platform.get().loge(TAG, msg);
    }

    //打印上传进度日志
    public static void logUpProgress(int progress, long currentSize, long totalSize) {
        if (!isDebug) return;
        Platform.get().logi(TAG, "UpProgress{progress=" + progress + ", currentSize=" + currentSize + ", totalSize=" + totalSize + "}");
    }

    //打印下载进度日志
    public static void logDownProgress(int progress, long currentSize, long totalSize) {
        if (!isDebug) return;
        Platform.get().logi(TAG, "DownProgress{progress=" + progress + ", currentSize=" + currentSize + ", totalSize=" + totalSize + "}");
    }

    //请求前，打印日志
    public static void log(@NonNull Request userRequest, CookieJar cookieJar) {
        if (!isDebug) return;
        try {
            Request.Builder requestBuilder = userRequest.newBuilder();
            StringBuilder builder = new StringBuilder("<------ ")
                .append(RxHttpVersion.userAgent).append(" ")
                .append(OkHttpCompat.getOkHttpUserAgent())
                .append(" request start ------>\n")
                .append(userRequest.method())
                .append(" ").append(userRequest.url());
            RequestBody body = userRequest.body();
            if (body != null) {
                MediaType contentType = body.contentType();
                if (contentType != null) {
                    requestBuilder.header("Content-Type", contentType.toString());
                }
                long contentLength = body.contentLength();
                if (contentLength != -1L) {
                    requestBuilder.header("Content-Length", String.valueOf(contentLength));
                    requestBuilder.removeHeader("Transfer-Encoding");
                } else {
                    requestBuilder.header("Transfer-Encoding", "chunked");
                    requestBuilder.removeHeader("Content-Length");
                }
            }

            if (userRequest.header("Host") == null) {
                requestBuilder.header("Host", hostHeader(userRequest.url()));
            }

            if (userRequest.header("Connection") == null) {
                requestBuilder.header("Connection", "Keep-Alive");
            }

            // If we add an "Accept-Encoding: gzip" header field we're responsible for also decompressing
            // the transfer stream.
            if (userRequest.header("Accept-Encoding") == null
                && userRequest.header("Range") == null) {
                requestBuilder.header("Accept-Encoding", "gzip");
            }
            List<Cookie> cookies = cookieJar.loadForRequest(userRequest.url());
            if (!cookies.isEmpty()) {
                requestBuilder.header("Cookie", cookieHeader(cookies));
            }
            if (userRequest.header("User-Agent") == null) {
                requestBuilder.header("User-Agent", OkHttpCompat.getOkHttpUserAgent());
            }
            builder.append("\n").append(readHeaders(requestBuilder.build().headers()));
            if (body != null) {
                builder.append("\n");
                if (bodyHasUnknownEncoding(userRequest.headers())) {
                    builder.append("(binary ")
                        .append(body.contentLength())
                        .append("-byte encoded body omitted)");
                } else {
                    builder.append(formattingJson(requestBody2Str(body), indentSpaces));
                }
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
            String result;
            if (body != null) {
                result = body;
            } else if (!promisesBody(response)) {
                result = "No Response Body";
            } else if (bodyHasUnknownEncoding(response.headers())) {
                result = "(binary " + response.body().contentLength() + "-byte encoded body omitted)";
            } else {
                result = formattingJson(response2Str(response), indentSpaces);
            }
            LogTime logTime = request.tag(LogTime.class);
            long tookMs = logTime != null ? logTime.tookMs() : 0;
            StringBuilder builder = new StringBuilder("<------ ")
                .append(RxHttpVersion.userAgent).append(" ")
                .append(OkHttpCompat.getOkHttpUserAgent())
                .append(" request end ------>\n")
                .append(request.method()).append(" ").append(request.url())
                .append("\n\n").append(response.protocol()).append(" ")
                .append(response.code()).append(" ").append(response.message())
                .append(tookMs > 0 ? " " + tookMs + "ms" : "")
                .append("\n").append(readHeaders(response.headers()))
                .append("\n").append(result);
            Platform.get().logi(TAG, builder.toString());
        } catch (Throwable e) {
            Platform.get().logd(TAG, "Request end Log printing failed", e);
        }
    }

    private static String requestBody2Str(@NonNull RequestBody body) throws IOException {
        if (body instanceof ProgressRequestBody) {
            body = ((ProgressRequestBody) body).getRequestBody();
        }
        if (body instanceof MultipartBody) {
            return multipartBody2Str((MultipartBody) body);
        }
        long contentLength = -1;
        try {
            contentLength = body.contentLength();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (body instanceof FileRequestBody) {
            return "(binary " + contentLength + "-byte file body omitted)";
        } else if (body instanceof UriRequestBody) {
            return "(binary " + contentLength + "-byte uri body omitted)";
        } else if (versionGte3140() && body.isDuplex()) {
            return "(binary " + contentLength + "-byte duplex body omitted)";
        } else if (versionGte3140() && body.isOneShot()) {
            return "(binary " + contentLength + "-byte one-shot body omitted)";
        } else {
            Buffer buffer = new Buffer();
            body.writeTo(buffer);
            if (!isProbablyUtf8(buffer)) {
                return "(binary " + body.contentLength() + "-byte body omitted)";
            } else {
                return buffer.readString(getCharset(body));
            }
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

            if (body instanceof MultipartBody) {
                sink.write(CRLF)
                    .writeUtf8(multipartBody2Str((MultipartBody) body));
            } else if (body instanceof FileRequestBody) {
                sink.writeUtf8("(binary " + contentLength + "-byte file body omitted)");
            } else if (body instanceof UriRequestBody) {
                sink.writeUtf8("(binary " + contentLength + "-byte uri body omitted)");
            } else if (versionGte3140() && body.isDuplex()) {
                sink.writeUtf8("(binary " + contentLength + "-byte duplex body omitted)");
            } else if (versionGte3140() && body.isOneShot()) {
                sink.writeUtf8("(binary " + contentLength + "-byte one-shot body omitted)");
            } else if (contentLength > 1024) {
                sink.writeUtf8("(binary " + contentLength + "-byte body omitted)");
            } else {
                try {
                    body.writeTo(sink);
                } catch (IOException e) {
                    e.printStackTrace();
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

    private static boolean versionGte3140() {
        return OkHttpCompat.okHttpVersionCompare("3.14.0") >= 0;
    }

    private static boolean versionGte400() {
        return OkHttpCompat.okHttpVersionCompare("4.0.0") >= 0;
    }

    @SuppressWarnings("deprecation")
    private static String response2Str(Response response) throws IOException {
        ResponseBody body = OkHttpCompat.requireBody(response);
        boolean onResultDecoder = OkHttpCompat.needDecodeResult(response);

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

    //格式化json数据
    private static String formattingJson(String json, int indentSpaces) {
        if (indentSpaces >= 0) {
            try {
                if (json.startsWith("[")) {
                    JSONArray jsonObject = new JSONArray(json);
                    return new JSONStringer(indentSpaces).write(jsonObject).toString();
                } else if (json.startsWith("{")) {
                    JSONObject jsonObject = new JSONObject(json);
                    return new JSONStringer(indentSpaces).write(jsonObject).toString();
                }
            } catch (Throwable ignore) {
                return json;
            }
        }
        return json;
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


    private static String hostHeader(HttpUrl url) {
        String host = url.host().contains(":")
            ? "[" + url.host() + "]"
            : url.host();
        return host + ":" + url.port();
    }

    /**
     * Returns a 'Cookie' HTTP request header with all cookies, like {@code a=b; c=d}.
     */
    private static String cookieHeader(List<Cookie> cookies) {
        StringBuilder cookieHeader = new StringBuilder();
        for (int i = 0, size = cookies.size(); i < size; i++) {
            if (i > 0) {
                cookieHeader.append("; ");
            }
            Cookie cookie = cookies.get(i);
            cookieHeader.append(cookie.name()).append('=').append(cookie.value());
        }
        return cookieHeader.toString();
    }

    private static boolean bodyHasUnknownEncoding(Headers headers) {
        String contentEncoding = headers.get("Content-Encoding");
        return contentEncoding != null
            && !contentEncoding.equalsIgnoreCase("identity")
            && !contentEncoding.equalsIgnoreCase("gzip");
    }

    /**
     * okhttp 4.9.2版本起，{@link Headers#toString()}方法
     * 将会对Authorization、Cookie、Proxy-Authorization、Set-Cookie头部隐藏敏感信息，故这里手动读取Headers信息以打印日志
     *
     * @param headers okhttp3.Headers
     * @return String
     */
    private static String readHeaders(Headers headers) {
        StringBuilder builder = new StringBuilder();
        int size = headers.size();
        for (int i = 0; i < size; i++) {
            builder.append(headers.name(i))
                .append(": ")
                .append(headers.value(i))
                .append("\n");
        }
        return builder.toString();
    }

    private static boolean promisesBody(Response response) {
        //okhttp 4.0.0 版本没有HttpHeaders.hasBody方法，但在4.0.1版本又恢复了，但被标记未过时
        return versionGte400() ? HttpHeaders.promisesBody(response) : HttpHeaders.hasBody(response);
    }
}
