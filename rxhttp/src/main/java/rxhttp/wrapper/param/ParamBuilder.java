package rxhttp.wrapper.param;

import java.io.File;
import java.util.Map;

import io.reactivex.annotations.NonNull;
import rxhttp.wrapper.callback.ProgressCallback;

/**
 * User: ljx
 * Date: 2019/1/19
 * Time: 10:25
 */
public interface ParamBuilder {

    Param setUrl(@NonNull String url);

    Param add(String key, Object value);

    Param add(Map<? extends String, ?> map);

    /**
     * 对Json 形式的请求，可直接调用此方法传入Json字符串做参数
     * 注:
     * 1、调用此方法后传入一个长度大于0的字符串后，通过add添加的请求参数将失效(请求头不影响)
     * 2、非Json形式的请求调用此方法，将不会有任何作用
     *
     * @param jsonParams Json字符串
     * @return Param对象
     */
    Param setJsonParams(String jsonParams);

    Map<String, Object> getParams();

    /**
     * <p>添加文件对象
     * <P>默认不支持，如有需要,自行扩展,参考{@link PostFormParam}
     *
     * @param key  键
     * @param file 文件对象
     * @return Param
     */
    default Param add(String key, File file) {
        throw new UnsupportedOperationException("Please override if you need");
    }

    /**
     * <p>设置上传进度监听器
     * <p>默认不支持,如有需要，自行扩展，参考{@link PostFormParam}
     *
     * @param callback 进度回调对象
     * @return Param
     */
    default Param setProgressCallback(ProgressCallback callback) {
        throw new UnsupportedOperationException("Please override if you need");
    }
}
