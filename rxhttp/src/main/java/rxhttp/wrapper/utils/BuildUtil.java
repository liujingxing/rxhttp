package rxhttp.wrapper.utils;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;

import java.net.URLConnection;
import java.util.List;
import java.util.regex.Pattern;

import okhttp3.FormBody;
import okhttp3.Headers;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.MultipartBody.Part;
import okhttp3.Request;
import okhttp3.RequestBody;
import rxhttp.wrapper.annotations.NonNull;
import rxhttp.wrapper.annotations.Nullable;
import rxhttp.wrapper.entity.KeyValuePair;
import rxhttp.wrapper.param.IRequest;

/**
 * User: ljx
 * Date: 2017/12/1
 * Time: 18:36
 */
public class BuildUtil {

    private static final Pattern PATH_TRAVERSAL = Pattern.compile("(.*/)?(\\.|%2e|%2E){1,2}(/.*)?");

    public static Request buildRequest(@NonNull IRequest r, @NonNull Request.Builder builder) {
        builder.url(r.getHttpUrl())
            .method(r.getMethod().name(), r.buildRequestBody());
        Headers headers = r.getHeaders();
        if (headers != null) {
            builder.headers(headers);
        }
        return builder.build();
    }

    /**
     * 构建一个表单 (不带文件)
     *
     * @param pairs 参数集合
     * @return RequestBody
     */
    public static RequestBody buildFormBody(List<KeyValuePair> pairs) {
        FormBody.Builder builder = new FormBody.Builder();
        if (pairs != null) {
            for (KeyValuePair pair : pairs) {
                Object value = pair.getValue();
                if (value == null) continue;
                String name = pair.getKey();
                if (pair.isEncoded()) {
                    builder.addEncoded(name, value.toString());
                } else {
                    builder.add(name, value.toString());
                }
            }
        }
        return builder.build();
    }

    /**
     * 构建一个表单(带文件)
     *
     * @param multiType MediaType
     * @param pairs     map参数集合
     * @param partList  文件列表
     * @return RequestBody
     */
    public static RequestBody buildMultipartBody(MediaType multiType, List<KeyValuePair> pairs, List<Part> partList) {
        MultipartBody.Builder builder = new MultipartBody.Builder();
        builder.setType(multiType);
        if (pairs != null) {
            //遍历参数
            for (KeyValuePair pair : pairs) {
                Object value = pair.getValue();
                if (value == null) continue;
                String name = pair.getKey();
                builder.addFormDataPart(name, value.toString());
            }
        }
        if (partList != null) {
            //遍历文件
            for (Part part : partList) {
                builder.addPart(part);
            }
        }
        return builder.build();
    }

    public static HttpUrl getHttpUrl(@NonNull String url, @Nullable List<KeyValuePair> queryList,
                                     @Nullable List<KeyValuePair> paths) {
        if (paths != null) {
            for (KeyValuePair path : paths) {
                String name = path.getKey();
                Object value = path.getValue();
                if (value == null) {
                    throw new IllegalArgumentException("Path parameter \"" + name + "\" value must not be null.");
                }
                String replacement = PathEncoderKt.canonicalizeForPath(value.toString(), path.isEncoded());
                String newUrl = url.replace("{" + name + "}", replacement);
                if (PATH_TRAVERSAL.matcher(newUrl).matches()) {
                    throw new IllegalArgumentException(
                        "Path parameters shouldn't perform path traversal ('.' or '..'): " + name + " is " + value);
                }
                url = newUrl;
            }
        }
        HttpUrl httpUrl = HttpUrl.get(url);
        if (queryList == null || queryList.size() == 0) return httpUrl;
        HttpUrl.Builder builder = httpUrl.newBuilder();
        for (KeyValuePair pair : queryList) {
            String name = pair.getKey();
            Object object = pair.getValue();
            String value = object == null ? null : object.toString();
            if (pair.isEncoded()) {
                builder.addEncodedQueryParameter(name, value);
            } else {
                builder.addQueryParameter(name, value);
            }
        }
        return builder.build();
    }

    //For compatibility with okHTTP 3.x version, only written in Java
    public static MediaType getMediaType(@Nullable String filename) {
        if (filename == null) return null;
        int index = filename.lastIndexOf(".") + 1;
        String fileSuffix = filename.substring(index);
        String contentType = URLConnection.guessContentTypeFromName(fileSuffix);
        return contentType != null ? MediaType.parse(contentType) : null;
    }

    //For compatibility with okHTTP 3.x version, only written in Java
    public static MediaType getMediaTypeByUri(Context context, Uri uri) {
        if (uri.getScheme().equals(ContentResolver.SCHEME_FILE)) {
            return BuildUtil.getMediaType((uri.getLastPathSegment()));
        } else {
            String contentType = context.getContentResolver().getType(uri);
            return contentType != null ? MediaType.parse(contentType) : null;
        }
    }
}
