package rxhttp.wrapper.utils;

import android.net.Uri;
import android.text.TextUtils;

import org.json.JSONObject;

import java.net.URLConnection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import io.reactivex.annotations.NonNull;
import okhttp3.CacheControl;
import okhttp3.FormBody;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.Request;
import okhttp3.Request.Builder;
import okhttp3.RequestBody;
import rxhttp.wrapper.entity.UpFile;
import rxhttp.wrapper.param.BodyRequest;
import rxhttp.wrapper.param.NoBodyRequest;
import rxhttp.wrapper.param.Param;

/**
 * User: ljx
 * Date: 2017/12/1
 * Time: 18:36
 */
public class BuildUtil {

    private static final MediaType MEDIA_TYPE_JSON = MediaType.parse("application/json;charset=utf-8");

    public static Request buildRequest(@NonNull NoBodyRequest r, String method) {
        Builder builder = new Request.Builder().url(r.getSimpleUrl())
            .tag(r.getTag());
        switch (method) {
            case Param.GET:
                builder.get();
                break;
            case Param.HEAD:
                builder.head();
                break;
        }
        CacheControl cacheControl = r.getCacheControl();
        if (cacheControl != null) {
            builder.cacheControl(cacheControl);
        }
        Headers headers = r.getHeaders();
        if (headers != null) {
            builder.headers(headers);
        }
        return builder.build();
    }

    public static Request buildRequest(@NonNull BodyRequest r, String method) {
        Builder builder = new Request.Builder().url(r.getSimpleUrl())
            .tag(r.getTag());
        switch (method) {
            case Param.POST:
                builder.post(r.getRequestBody());
                break;
            case Param.PUT:
                builder.put(r.getRequestBody());
                break;
            case Param.PATCH:
                builder.patch(r.getRequestBody());
                break;
            case Param.DELETE:
                builder.delete(r.getRequestBody());
                break;
        }
        CacheControl cacheControl = r.getCacheControl();
        if (cacheControl != null) {
            builder.cacheControl(cacheControl);
        }
        Headers headers = r.getHeaders();
        if (headers != null) {
            builder.headers(headers);
        }
        return builder.build();
    }

    /**
     * 构建一个表单 (不带文件)
     *
     * @param map map参数集合
     * @return RequestBody
     */
    public static <K, V> RequestBody buildFormRequestBody(@NonNull Map<K, V> map) {
        FormBody.Builder builder = new FormBody.Builder();
        for (Entry<K, V> entry : map.entrySet()) {
            builder.add(entry.getKey().toString(), entry.getValue().toString());
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
    public static <K, V> RequestBody buildFormRequestBody(@NonNull Map<K, V> map,
                                                          @NonNull List<UpFile> fileList) {
        MultipartBody.Builder builder = new MultipartBody.Builder();
        builder.setType(MultipartBody.FORM);
        //遍历参数
        for (Entry<K, V> entry : map.entrySet()) {
            builder.addFormDataPart(entry.getKey().toString(), entry.getValue().toString());
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
     * map对象转Json字符串
     *
     * @param map map对象
     * @return Json 字符串
     */
    public static <K, V> String mapToJson(@NonNull Map<K, V> map) {
        return new JSONObject(map).toString();
    }

    /**
     * 构建一个json形式的RequestBody
     *
     * @param map map集合
     * @return RequestBody
     */
    public static <K, V> RequestBody buildJsonRequestBody(@NonNull Map<K, V> map) {
        return buildJsonRequestBody(new JSONObject(map).toString());
    }

    /**
     * 构建一个json形式的RequestBody
     *
     * @param json json字符串
     * @return RequestBody
     */
    public static RequestBody buildJsonRequestBody(@NonNull String json) {
        return RequestBody.create(MEDIA_TYPE_JSON, json);
    }

    /**
     * 所有参数以 key=value 格式拼接(用 & 拼接)在一起并返回
     *
     * @param map Map集合
     * @return 拼接后的字符串
     */
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

    /**
     * 组合url及参数 格式:mUrl?key=value...
     *
     * @param url url链接
     * @param map map集合
     * @return 拼接后的字符串
     */
    public static <K, V> String mergeUrlAndParams(@NonNull String url, Map<K, V> map) {
        if (map == null || map.size() == 0) return url;
        Uri.Builder builder = Uri.parse(url).buildUpon();
        for (Entry<K, V> e : map.entrySet()) {
            builder.appendQueryParameter(e.getKey().toString(), e.getValue().toString());
        }
        return builder.toString();
    }

    private static MediaType getMediaType(String fName) {
        String contentType = URLConnection.guessContentTypeFromName(fName);
        if (TextUtils.isEmpty(contentType)) {
            contentType = "application/octet-stream";
        }
        return MediaType.parse(contentType);
    }
}
