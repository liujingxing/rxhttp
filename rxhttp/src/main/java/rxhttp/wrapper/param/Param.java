package rxhttp.wrapper.param;

import org.jetbrains.annotations.NotNull;

import okhttp3.MediaType;

/**
 * User: ljx
 * Date: 2019/1/19
 * Time: 10:25
 */
public abstract class Param<P extends Param<P>> implements IParam<P>, IHeaders<P>, ICache<P>, IRequest {

    public static String DATA_DECRYPT = "data-decrypt";

    public static NoBodyParam get(@NotNull String url) {
        return new NoBodyParam(url, Method.GET);
    }

    public static NoBodyParam head(@NotNull String url) {
        return new NoBodyParam(url, Method.HEAD);
    }

    //The Content-Type depends on the RequestBody
    public static BodyParam postBody(@NotNull String url) {
        return new BodyParam(url, Method.POST);
    }

    public static BodyParam putBody(@NotNull String url) {
        return new BodyParam(url, Method.PUT);
    }

    public static BodyParam patchBody(@NotNull String url) {
        return new BodyParam(url, Method.PATCH);
    }

    public static BodyParam deleteBody(@NotNull String url) {
        return new BodyParam(url, Method.DELETE);
    }

    /**
     * Content-Type: application/x-www-form-urlencoded
     * if have file, Content-Type: multipart/form-data
     * call {@link FormParam#setMultiType(MediaType)}, specify Content-Type
     *
     * @param url url
     * @return FormParam
     */
    public static FormParam postForm(@NotNull String url) {
        return new FormParam(url, Method.POST);
    }

    public static FormParam putForm(@NotNull String url) {
        return new FormParam(url, Method.PUT);
    }

    public static FormParam patchForm(@NotNull String url) {
        return new FormParam(url, Method.PATCH);
    }

    public static FormParam deleteForm(@NotNull String url) {
        return new FormParam(url, Method.DELETE);
    }

    //Content-Type: application/json; charset=utf-8
    public static JsonParam postJson(@NotNull String url) {
        return new JsonParam(url, Method.POST);
    }

    public static JsonParam putJson(@NotNull String url) {
        return new JsonParam(url, Method.PUT);
    }

    public static JsonParam patchJson(@NotNull String url) {
        return new JsonParam(url, Method.PATCH);
    }

    public static JsonParam deleteJson(@NotNull String url) {
        return new JsonParam(url, Method.DELETE);
    }

    //Content-Type: application/json; charset=utf-8
    public static JsonArrayParam postJsonArray(@NotNull String url) {
        return new JsonArrayParam(url, Method.POST);
    }

    public static JsonArrayParam putJsonArray(@NotNull String url) {
        return new JsonArrayParam(url, Method.PUT);
    }

    public static JsonArrayParam patchJsonArray(@NotNull String url) {
        return new JsonArrayParam(url, Method.PATCH);
    }

    public static JsonArrayParam deleteJsonArray(@NotNull String url) {
        return new JsonArrayParam(url, Method.DELETE);
    }

}
