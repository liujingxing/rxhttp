package rxhttp.wrapper.utils;

import java.net.URLConnection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import okhttp3.FormBody;
import okhttp3.Headers;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.Request;
import okhttp3.RequestBody;
import rxhttp.wrapper.annotations.NonNull;
import rxhttp.wrapper.entity.KeyValuePair;
import rxhttp.wrapper.entity.UpFile;
import rxhttp.wrapper.param.IRequest;

/**
 * User: ljx
 * Date: 2017/12/1
 * Time: 18:36
 */
public class BuildUtil {

    public static Request buildRequest(@NonNull IRequest r, @NonNull Request.Builder builder) {
        builder.url(r.getHttpUrl())
            .method(r.getMethod().name(), r.getRequestBody());
        Headers headers = r.getHeaders();
        if (headers != null) {
            builder.headers(headers);
        }
        if (LogUtil.isIsDebug()) {
            builder.tag(LogTime.class, new LogTime());
        }
        return builder.build();
    }

    /**
     * 构建一个表单 (不带文件)
     *
     * @param map map参数集合
     * @param <K> key
     * @param <V> value
     * @return RequestBody
     */
    @Deprecated
    public static <K, V> RequestBody buildFormRequestBody(Map<K, V> map) {
        FormBody.Builder builder = new FormBody.Builder();
        if (map != null) {
            for (Entry<K, V> entry : map.entrySet()) {
                builder.add(entry.getKey().toString(), entry.getValue().toString());
            }
        }
        return builder.build();
    }

    /**
     * 构建一个表单(带文件)
     *
     * @param map      map参数集合
     * @param fileList 文件列表
     * @param <K>      key
     * @param <V>      value
     * @return RequestBody
     */
    @Deprecated
    public static <K, V> RequestBody buildFormRequestBody(Map<K, V> map,
                                                          List<UpFile> fileList) {
        MultipartBody.Builder builder = new MultipartBody.Builder();
        builder.setType(MultipartBody.FORM);
        if (map != null) {
            //遍历参数
            for (Entry<K, V> entry : map.entrySet()) {
                builder.addFormDataPart(entry.getKey().toString(), entry.getValue().toString());
            }
        }
        if (fileList != null) {
            //遍历文件
            for (UpFile file : fileList) {
                if (!file.exists() || !file.isFile()) continue;
                RequestBody requestBody = RequestBody.create(getMediaType(file.getName()), file);
                builder.addFormDataPart(file.getKey(), file.getValue(), requestBody);
            }
        }
        return builder.build();
    }

    /**
     * 构建一个表单 (不带文件)
     *
     * @param map map参数集合
     * @return RequestBody
     */
    public static RequestBody buildFormRequestBody(List<KeyValuePair> map) {
        FormBody.Builder builder = new FormBody.Builder();
        if (map != null) {
            for (KeyValuePair entry : map) {
                if (entry.isEncoded()) {
                    builder.addEncoded(entry.getKey(), entry.getValue().toString());
                } else {
                    builder.add(entry.getKey(), entry.getValue().toString());
                }

            }
        }
        return builder.build();
    }

    /**
     * 构建一个表单(带文件)
     *
     * @param map      map参数集合
     * @param fileList 文件列表
     * @return RequestBody
     */
    public static RequestBody buildFormRequestBody(List<KeyValuePair> map,
                                                   List<UpFile> fileList) {
        MultipartBody.Builder builder = new MultipartBody.Builder();
        builder.setType(MultipartBody.FORM);
        if (map != null) {
            //遍历参数
            for (KeyValuePair entry : map) {
                builder.addFormDataPart(entry.getKey(), entry.getValue().toString());
            }
        }
        if (fileList != null) {
            //遍历文件
            for (UpFile file : fileList) {
                if (!file.exists() || !file.isFile()) continue;
                RequestBody requestBody = RequestBody.create(getMediaType(file.getName()), file);
                builder.addFormDataPart(file.getKey(), file.getValue(), requestBody);
            }
        }
        return builder.build();
    }

    /**
     * 所有参数以 key=value 格式拼接在一起并返回
     *
     * @param map Map集合
     * @param <K> key
     * @param <V> value
     * @return 拼接后的字符串
     */
    @Deprecated
    public static <K, V> String toKeyValue(Map<K, V> map) {
        if (map == null || map.size() == 0) return "";
        Iterator<Entry<K, V>> i = map.entrySet().iterator();
        if (!i.hasNext()) return "";
        StringBuilder builder = new StringBuilder();
        while (true) {
            Entry<K, V> e = i.next();
            builder.append(e.getKey())
                .append("=")
                .append(e.getValue());
            if (!i.hasNext())
                return builder.toString();
            builder.append("&");
        }
    }

    @Deprecated
    public static <K, V> HttpUrl getHttpUrl(@NonNull String url, Map<K, V> map) {
        HttpUrl httpUrl = HttpUrl.get(url);
        if (map == null || map.size() == 0) return httpUrl;
        HttpUrl.Builder builder = httpUrl.newBuilder();
        for (Entry<K, V> e : map.entrySet()) {
            builder.addQueryParameter(e.getKey().toString(), e.getValue().toString());
        }
        return builder.build();
    }

    public static HttpUrl getHttpUrl(@NonNull String url, List<KeyValuePair> list) {
        HttpUrl httpUrl = HttpUrl.get(url);
        if (list == null || list.size() == 0) return httpUrl;
        HttpUrl.Builder builder = httpUrl.newBuilder();
        for (KeyValuePair pair : list) {
            if (pair.isEncoded()) {
                builder.addEncodedQueryParameter(pair.getKey(), pair.getValue().toString());
            } else {
                builder.addQueryParameter(pair.getKey(), pair.getValue().toString());
            }
        }
        return builder.build();
    }

    private static MediaType getMediaType(String fName) {
        String contentType = URLConnection.guessContentTypeFromName(fName);
        if (contentType == null || contentType.isEmpty()) {
            contentType = "application/octet-stream";
        }
        return MediaType.parse(contentType);
    }
}
