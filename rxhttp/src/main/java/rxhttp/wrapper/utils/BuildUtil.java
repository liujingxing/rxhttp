package rxhttp.wrapper.utils;

import android.net.Uri;

import org.json.JSONObject;

import java.io.File;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import rxhttp.wrapper.entity.UpFile;
import io.reactivex.annotations.NonNull;
import okhttp3.*;
import okhttp3.Request.Builder;
import rxhttp.wrapper.param.*;

/**
 * User: ljx
 * Date: 2017/12/1
 * Time: 18:36
 */
public class BuildUtil {

    private static final MediaType MEDIA_TYPE_ATTACH = MediaType.parse("application/octet-stream;charset=utf-8");
    private static final MediaType MEDIA_TYPE_JSON   = MediaType.parse("application/json;charset=utf-8");


    /**
     * 构建一个Get Request
     *
     * @param r GetRequest
     * @return Request
     */
    public static Request buildGetRequest(@NonNull GetRequest r) {
        Builder builder = buildRequestBuilder(r);
        return builder.url(r.getUrl()).get().build();
    }

    /**
     * 构建一个Head Request
     *
     * @param r HeadRequest
     * @return Request
     */
    public static Request buildHeadRequest(@NonNull HeadRequest r) {
        Builder builder = buildRequestBuilder(r);
        return builder.url(r.getUrl()).head().build();
    }

    /**
     * 构建一个post Request
     *
     * @param r PostRequest接口对象
     * @return Request
     */
    public static Request buildPostRequest(@NonNull PostRequest r) {
        Builder builder = buildRequestBuilder(r);
        return builder.url(r.getSimpleUrl()).post(r.getRequestBody()).build();
    }

    /**
     * 构建一个Put Request
     *
     * @param r PutRequest接口对象
     * @return Request
     */
    public static Request buildPutRequest(@NonNull PutRequest r) {
        Builder builder = buildRequestBuilder(r);
        return builder.url(r.getSimpleUrl()).put(r.getRequestBody()).build();
    }

    /**
     * 构建一个Patch Request
     *
     * @param r PatchRequest接口对象
     * @return Request
     */
    public static Request buildPatchRequest(@NonNull PatchRequest r) {
        Builder builder = buildRequestBuilder(r);
        return builder.url(r.getSimpleUrl()).patch(r.getRequestBody()).build();
    }

    /**
     * 构建一个Delete Request
     *
     * @param r DeleteRequest接口对象
     * @return Request
     */
    public static Request buildDeleteRequest(@NonNull DeleteRequest r) {
        Builder builder = buildRequestBuilder(r);
        return builder.url(r.getSimpleUrl()).delete(r.getRequestBody()).build();
    }

    /**
     * 构建一个没有设置请求方法的 Request.Builder
     *
     * @param request CommonRequest对象
     * @return Request.Builder对象
     */
    public static Request.Builder buildRequestBuilder(NoBodyRequest request) {
        return buildRequestBuilder(request.getHeaders(), request.getCacheControl(), request.getTag());
    }

    /**
     * 构建一个没有设置请求方法的 Request.Builder
     *
     * @param headers      请求头
     * @param cacheControl 缓存控制
     * @param tag          tag
     * @return Request.Builder对象
     */
    public static Request.Builder buildRequestBuilder(Headers headers,
                                                      CacheControl cacheControl, Object tag) {
        Request.Builder builder = new Request.Builder();
        builder.tag(tag);
        if (headers != null)
            builder.headers(headers);
        if (cacheControl != null)
            builder.cacheControl(cacheControl);
        return builder;
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
     * @param map  map参数集合
     * @param fileMap map文件集合
     * @return RequestBody
     */
    public static <K, V> RequestBody buildFormRequestBody(@NonNull Map<K, V> map,
                                                          @NonNull Map<String, File> fileMap) {
        MultipartBody.Builder builder = new MultipartBody.Builder();
        builder.setType(MultipartBody.FORM);
        //遍历参数
        for (Entry<K, V> entry : map.entrySet()) {
            builder.addFormDataPart(entry.getKey().toString(), entry.getValue().toString());
        }
        //遍历文件
        for (Entry<String, File> entry : fileMap.entrySet()) {
            File file = entry.getValue();
            if (!file.exists() || !file.isFile()) continue;
            String value = file instanceof UpFile ? ((UpFile) file).getValue() : file.getName();
            builder.addFormDataPart(entry.getKey(), value, RequestBody.create(MEDIA_TYPE_ATTACH, file));
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
}
