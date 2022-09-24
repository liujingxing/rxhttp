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

    //Get请求
    public static NoBodyParam get(@NotNull String url) {
        return new NoBodyParam(url, Method.GET);
    }

    //Head请求
    public static NoBodyParam head(@NotNull String url) {
        return new NoBodyParam(url, Method.HEAD);
    }

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
     * post request
     *
     * Content-Type: application/x-www-form-urlencoded
     * if have file, Content-Type: multipart/form-data
     *
     * call {@link FormParam#setMultiType(MediaType)}, specify Content-Type
     *
     * @param url url
     * @return FormParam
     */
    public static FormParam postForm(@NotNull String url) {
        return new FormParam(url, Method.POST);
    }

    /**
     * put request
     *
     * Content-Type: application/x-www-form-urlencoded
     * if have file, Content-Type: multipart/form-data
     *
     * call {@link FormParam#setMultiType(MediaType)}, specify Content-Type
     *
     * @param url url
     * @return FormParam
     */
    public static FormParam putForm(@NotNull String url) {
        return new FormParam(url, Method.PUT);
    }

    /**
     * patch request
     *
     * Content-Type: application/x-www-form-urlencoded
     * if have file, Content-Type: multipart/form-data
     *
     * call {@link FormParam#setMultiType(MediaType)}, specify Content-Type
     *
     * @param url url
     * @return FormParam
     */
    public static FormParam patchForm(@NotNull String url) {
        return new FormParam(url, Method.PATCH);
    }

    /**
     * delete request
     *
     * Content-Type: application/x-www-form-urlencoded
     * if have file, Content-Type: multipart/form-data
     *
     * call {@link FormParam#setMultiType(MediaType)}, specify Content-Type
     *
     * @param url url
     * @return FormParam
     */
    public static FormParam deleteForm(@NotNull String url) {
        return new FormParam(url, Method.DELETE);
    }

    /**
     * post请求,参数以{application/json; charset=utf-8}形式提交,提交Json对象
     *
     * @param url url
     * @return JsonParam
     */
    public static JsonParam postJson(@NotNull String url) {
        return new JsonParam(url, Method.POST);
    }

    /**
     * put请求,参数以{application/json; charset=utf-8}形式提交,提交Json对象
     *
     * @param url url
     * @return JsonParam
     */
    public static JsonParam putJson(@NotNull String url) {
        return new JsonParam(url, Method.PUT);
    }

    /**
     * patch请求,参数以{application/json; charset=utf-8}形式提交,提交Json对象
     *
     * @param url url
     * @return JsonParam
     */
    public static JsonParam patchJson(@NotNull String url) {
        return new JsonParam(url, Method.PATCH);
    }

    /**
     * delete请求,参数以{application/json; charset=utf-8}形式提交,提交Json对象
     *
     * @param url url
     * @return JsonParam
     */
    public static JsonParam deleteJson(@NotNull String url) {
        return new JsonParam(url, Method.DELETE);
    }

    /**
     * post请求,参数以{application/json; charset=utf-8}形式提交,提交Json数组
     *
     * @param url url
     * @return JsonArrayParam
     */
    public static JsonArrayParam postJsonArray(@NotNull String url) {
        return new JsonArrayParam(url, Method.POST);
    }


    /**
     * put请求,参数以{application/json; charset=utf-8}形式提交,提交Json数组
     *
     * @param url url
     * @return JsonArrayParam
     */
    public static JsonArrayParam putJsonArray(@NotNull String url) {
        return new JsonArrayParam(url, Method.PUT);
    }

    /**
     * patch请求,参数以{application/json; charset=utf-8}形式提交,提交Json数组
     *
     * @param url url
     * @return JsonArrayParam
     */
    public static JsonArrayParam patchJsonArray(@NotNull String url) {
        return new JsonArrayParam(url, Method.PATCH);
    }

    /**
     * delete请求,参数以{application/json; charset=utf-8}形式提交,提交Json数组
     *
     * @param url url
     * @return JsonArrayParam
     */
    public static JsonArrayParam deleteJsonArray(@NotNull String url) {
        return new JsonArrayParam(url, Method.DELETE);
    }

}
