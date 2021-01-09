package rxhttp.wrapper.utils;

import java.net.URLConnection;
import java.util.List;

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
     * @param keyValuePairs map参数集合
     * @param partList      文件列表
     * @return RequestBody
     */
    public static RequestBody buildFormRequestBody(List<KeyValuePair> keyValuePairs, List<Part> partList) {
        MultipartBody.Builder builder = new MultipartBody.Builder();
        builder.setType(MultipartBody.FORM);
        if (keyValuePairs != null) {
            //遍历参数
            for (KeyValuePair entry : keyValuePairs) {
                builder.addFormDataPart(entry.getKey(), entry.getValue().toString());
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

    public static HttpUrl getHttpUrl(@NonNull String url, @Nullable List<KeyValuePair> list) {
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

    public static MediaType getMediaType(String filename) {
        int index = filename.lastIndexOf(".") + 1;
        String fileSuffix = filename.substring(index);
        String contentType = URLConnection.guessContentTypeFromName(fileSuffix);
        if (contentType == null || contentType.isEmpty()) {
            contentType = "application/octet-stream";
        }
        return MediaType.parse(contentType);
    }
}
