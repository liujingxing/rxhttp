package rxhttp.wrapper.param;

import io.reactivex.annotations.NonNull;

/**
 * User: ljx
 * Date: 2019/1/19
 * Time: 10:25
 */
public interface Param<T extends Param> extends ParamBuilder<T>, HeadersBuilder<T>, NoBodyRequest, RequestBuilder {

    String GET    = "get";
    String HEAD   = "head";
    String POST   = "post";
    String PUT    = "put";
    String PATCH  = "patch";
    String DELETE = "delete";


    String DATA_DECRYPT = "data-decrypt";

    //Get请求
    static NoBodyParam get(@NonNull String url) {
        return new NoBodyParam(url, Param.GET);
    }

    //Head请求
    static NoBodyParam head(@NonNull String url) {
        return new NoBodyParam(url, Param.HEAD);
    }

    //Post请求，参数以Form表单键值对的形式提交,当有文件时，自动以{multipart/form-data}形式提交
    static FormParam postForm(@NonNull String url) {
        return new FormParam(url, POST);
    }

    //Post请求，参数以Json形式提交
    static JsonParam postJson(@NonNull String url) {
        return new JsonParam(url, POST);
    }

    //Put请求,参数以Form表单键值对的形式提交
    static FormParam putForm(@NonNull String url) {
        return new FormParam(url, PUT);
    }

    //Put请求,参数以Json形式提交
    static JsonParam putJson(@NonNull String url) {
        return new JsonParam(url, PUT);
    }

    //Patch请求,参数以Form表单键值对的形式提交
    static FormParam patchForm(@NonNull String url) {
        return new FormParam(url, Param.PATCH);
    }

    //Patch请求,参数以Json形式提交
    static JsonParam patchJson(@NonNull String url) {
        return new JsonParam(url, PATCH);
    }

    //Delete请求,参数以Form表单键值对的形式提交
    static FormParam deleteForm(@NonNull String url) {
        return new FormParam(url, Param.DELETE);
    }

    //Delete请求,参数以Json形式提交
    static JsonParam deleteJson(@NonNull String url) {
        return new JsonParam(url, DELETE);
    }
}
