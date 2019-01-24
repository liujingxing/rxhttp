package httpsender.wrapper.param;

import java.io.File;

import httpsender.wrapper.callback.ProgressCallback;
import io.reactivex.annotations.NonNull;

/**
 * User: ljx
 * Date: 2019/1/19
 * Time: 10:25
 */
public interface Param extends ParamBuilder, HeadersBuilder, NoBodyRequest, RequestBuilder {

    int GET         = 0; //Get请求
    int HEAD        = 1; //Head请求
    int POST_FORM   = 2; //Post请求，参数以Form表单键值对的形式提交
    int POST_JSON   = 3; //Post请求，参数以Json形式提交
    int PUT_FORM    = 4; //Put请求,参数以Form表单键值对的形式提交
    int PUT_JSON    = 5; //Put请求,参数以Json形式提交
    int PATCH_FORM  = 6; //Patch请求,参数以Form表单键值对的形式提交
    int PATCH_JSON  = 7; //Patch请求,参数以Json形式提交
    int DELETE_FORM = 8; //Delete请求,参数以Form表单键值对的形式提交
    int DELETE_JSON = 9; //Delete请求,参数以Json形式提交

    static Param with(@NonNull String url, int method) {
        switch (method) {
            case GET:
                return GetParam.with(url);
            case HEAD:
                return HeadParam.with(url);
            case POST_FORM:
                return PostFormParam.with(url);
            case POST_JSON:
                return PostJsonParam.with(url);
            case PUT_FORM:
                return PutFormParam.with(url);
            case PUT_JSON:
                return PutJsonParam.with(url);
            case PATCH_FORM:
                return PatchFormParam.with(url);
            case PATCH_JSON:
                return PatchJsonParam.with(url);
            case DELETE_FORM:
                return DeleteFormParam.with(url);
            case DELETE_JSON:
                return DeleteJsonParam.with(url);
        }
        throw new IllegalArgumentException("method is invalid");
    }

    static Param get(@NonNull String url) {
        return with(url, GET);
    }

    static Param head(@NonNull String url) {
        return with(url, HEAD);
    }

    static Param postForm(@NonNull String url) {
        return with(url, POST_FORM);
    }

    static Param postJson(@NonNull String url) {
        return with(url, POST_JSON);
    }

    static Param putForm(@NonNull String url) {
        return with(url, PUT_FORM);
    }

    static Param putJson(@NonNull String url) {
        return with(url, PUT_JSON);
    }

    static Param patchForm(@NonNull String url) {
        return with(url, PATCH_FORM);
    }

    static Param patchJson(@NonNull String url) {
        return with(url, PATCH_JSON);
    }

    static Param deleteForm(@NonNull String url) {
        return with(url, DELETE_FORM);
    }

    static Param deleteJson(@NonNull String url) {
        return with(url, DELETE_JSON);
    }

    /**
     * <p>添加文件对象
     * <P>默认空实现，如有需要,自行扩展,参考{@link PostFormParam}
     *
     * @param key  键
     * @param file 文件对象
     * @return Param
     */
    default Param add(String key, File file) {
        return this;
    }

    /**
     * <p>设置上传进度监听器
     * <p>默认空实现,如有需要，自行扩展，参考{@link PostFormParam}
     *
     * @param callback 进度回调对象
     * @return Param
     */
    default Param setProgressCallback(ProgressCallback callback) {
        return this;
    }
}
