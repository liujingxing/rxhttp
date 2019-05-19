package rxhttp.wrapper.param;

import java.io.File;
import java.util.List;

import io.reactivex.annotations.NonNull;
import okhttp3.CacheControl;
import rxhttp.wrapper.entity.UpFile;

/**
 * User: ljx
 * Date: 2019/1/19
 * Time: 10:25
 */
public interface Param extends ParamBuilder, FileBuilder, HeadersBuilder, NoBodyRequest, RequestBuilder {

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

    /**
     * @return 判断是否对参数添加装饰，即是否添加公共参数
     */
    boolean isAssemblyEnabled();

    /**
     * 设置是否对参数添加装饰，即是否添加公共参数
     *
     * @param enabled true 是
     * @return Param
     */
    Param setAssemblyEnabled(boolean enabled);

    Param tag(Object tag);

    Param cacheControl(CacheControl cacheControl);

    @Override
    default Param setJsonParams(String jsonParams) {
        return this;
    }

    @Override
    default Param addFile(String key, List<File> fileList) {
        for (File file : fileList) {
            addFile(new UpFile(key, file.getAbsolutePath()));
        }
        return this;
    }

    @Override
    default Param addFile(List<UpFile> fileList) {
        for (UpFile upFile : fileList) {
            addFile(upFile);
        }
        return this;
    }

    /**
     * <p>添加文件对象
     * <P>默认不支持，如有需要,自行扩展,参考{@link PostFormParam}
     *
     * @param upFile UpFile
     * @return Param
     */
    @Override
    default Param addFile(UpFile upFile) {
        throw new UnsupportedOperationException("Please override if you need");
    }

    /**
     * 根据key 移除已添加的文件
     * 默认不支持，如有需要,自行扩展,参考{@link PostFormParam}
     * @param key String
     * @return Param
     */
    @Override
    default Param removeFile(String key) {
        throw new UnsupportedOperationException("Please override if you need");
    }
}
