package rxhttp.wrapper.param;

import rxhttp.wrapper.annotations.NonNull;

/**
 * User: ljx
 * Date: 2019/1/19
 * Time: 10:25
 */
public interface Param<P extends Param<P>> extends IParam<P>, IHeaders<P>, ICache<P>, IRequest {

    String DATA_DECRYPT = "data-decrypt";

    //Get请求
    static NoBodyParam get(@NonNull String url) {
        return new NoBodyParam(url, Method.GET);
    }

    //Head请求
    static NoBodyParam head(@NonNull String url) {
        return new NoBodyParam(url, Method.HEAD);
    }

    /**
     * post请求
     * 参数以{application/x-www-form-urlencoded}形式提交
     * 当带有文件时，自动以{multipart/form-data}形式提交
     * 当调用{@link FormParam#setMultiForm()}方法，强制以{multipart/form-data}形式提交
     *
     * @param url url
     * @return FormParam
     */
    static FormParam postForm(@NonNull String url) {
        return new FormParam(url, Method.POST);
    }

    /**
     * put请求
     * 参数以{application/x-www-form-urlencoded}形式提交
     * 当带有文件时，自动以{multipart/form-data}形式提交
     * 当调用{@link FormParam#setMultiForm()}方法，强制以{multipart/form-data}形式提交
     *
     * @param url url
     * @return FormParam
     */
    static FormParam putForm(@NonNull String url) {
        return new FormParam(url, Method.PUT);
    }

    /**
     * patch请求
     * 参数以{application/x-www-form-urlencoded}形式提交
     * 当带有文件时，自动以{multipart/form-data}形式提交
     * 当调用{@link FormParam#setMultiForm()}方法，强制以{multipart/form-data}形式提交
     *
     * @param url url
     * @return FormParam
     */
    static FormParam patchForm(@NonNull String url) {
        return new FormParam(url, Method.PATCH);
    }

    /**
     * delete请求
     * 参数以{application/x-www-form-urlencoded}形式提交
     * 当带有文件时，自动以{multipart/form-data}形式提交
     * 当调用{@link FormParam#setMultiForm()}方法，强制以{multipart/form-data}形式提交
     *
     * @param url url
     * @return FormParam
     */
    static FormParam deleteForm(@NonNull String url) {
        return new FormParam(url, Method.DELETE);
    }

    /**
     * post请求,参数以{application/json; charset=utf-8}形式提交,提交Json对象
     *
     * @param url url
     * @return JsonParam
     */
    static JsonParam postJson(@NonNull String url) {
        return new JsonParam(url, Method.POST);
    }

    /**
     * put请求,参数以{application/json; charset=utf-8}形式提交,提交Json对象
     *
     * @param url url
     * @return JsonParam
     */
    static JsonParam putJson(@NonNull String url) {
        return new JsonParam(url, Method.PUT);
    }

    /**
     * patch请求,参数以{application/json; charset=utf-8}形式提交,提交Json对象
     *
     * @param url url
     * @return JsonParam
     */
    static JsonParam patchJson(@NonNull String url) {
        return new JsonParam(url, Method.PATCH);
    }

    /**
     * delete请求,参数以{application/json; charset=utf-8}形式提交,提交Json对象
     *
     * @param url url
     * @return JsonParam
     */
    static JsonParam deleteJson(@NonNull String url) {
        return new JsonParam(url, Method.DELETE);
    }

    /**
     * post请求,参数以{application/json; charset=utf-8}形式提交,提交Json数组
     *
     * @param url url
     * @return JsonArrayParam
     */
    static JsonArrayParam postJsonArray(@NonNull String url) {
        return new JsonArrayParam(url, Method.POST);
    }


    /**
     * put请求,参数以{application/json; charset=utf-8}形式提交,提交Json数组
     *
     * @param url url
     * @return JsonArrayParam
     */
    static JsonArrayParam putJsonArray(@NonNull String url) {
        return new JsonArrayParam(url, Method.PUT);
    }

    /**
     * patch请求,参数以{application/json; charset=utf-8}形式提交,提交Json数组
     *
     * @param url url
     * @return JsonArrayParam
     */
    static JsonArrayParam patchJsonArray(@NonNull String url) {
        return new JsonArrayParam(url, Method.PATCH);
    }

    /**
     * delete请求,参数以{application/json; charset=utf-8}形式提交,提交Json数组
     *
     * @param url url
     * @return JsonArrayParam
     */
    static JsonArrayParam deleteJsonArray(@NonNull String url) {
        return new JsonArrayParam(url, Method.DELETE);
    }

}
