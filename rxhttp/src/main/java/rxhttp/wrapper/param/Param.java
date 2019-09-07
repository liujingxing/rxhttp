package rxhttp.wrapper.param;

import io.reactivex.annotations.NonNull;

/**
 * User: ljx
 * Date: 2019/1/19
 * Time: 10:25
 */
public interface Param extends ParamBuilder, FileBuilder<Param>, HeadersBuilder, NoBodyRequest, RequestBuilder {

    String DATA_DECRYPT = "data-decrypt";

    //Get请求
    static Param get(@NonNull String url) {
        return GetParam.with(url);
    }

    //Head请求
    static Param head(@NonNull String url) {
        return HeadParam.with(url);
    }

    //Post请求，参数以Form表单键值对的形式提交
    static Param postForm(@NonNull String url) {
        return PostFormParam.with(url);
    }

    //Post请求，参数以Json形式提交
    static Param postJson(@NonNull String url) {
        return PostJsonParam.with(url);
    }

    //Put请求,参数以Form表单键值对的形式提交
    static Param putForm(@NonNull String url) {
        return PutFormParam.with(url);
    }

    //Put请求,参数以Json形式提交
    static Param putJson(@NonNull String url) {
        return PutJsonParam.with(url);
    }

    //Patch请求,参数以Form表单键值对的形式提交
    static Param patchForm(@NonNull String url) {
        return PatchFormParam.with(url);
    }

    //Patch请求,参数以Json形式提交
    static Param patchJson(@NonNull String url) {
        return PatchJsonParam.with(url);
    }

    //Delete请求,参数以Form表单键值对的形式提交
    static Param deleteForm(@NonNull String url) {
        return DeleteFormParam.with(url);
    }

    //Delete请求,参数以Json形式提交
    static Param deleteJson(@NonNull String url) {
        return DeleteJsonParam.with(url);
    }
}
