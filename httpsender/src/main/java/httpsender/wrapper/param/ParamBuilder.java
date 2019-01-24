package httpsender.wrapper.param;

import java.io.File;

import httpsender.wrapper.callback.ProgressCallback;
import io.reactivex.annotations.NonNull;

/**
 * User: ljx
 * Date: 2019/1/19
 * Time: 10:25
 */
public interface ParamBuilder {

    Param setUrl(@NonNull String url);

    Param add(String key, String value);

    /**
     * 设置上传进度监听器
     *
     * @param callback 进度回调对象
     * @return Param
     */
    Param setProgressCallback(ProgressCallback callback);

    /**
     * 添加文件对象
     *
     * @param key  键
     * @param file 文件对象
     * @return Param
     */
    Param add(String key, File file);

    default Param add(String key, int value) {
        return add(key, String.valueOf(value));
    }

    default Param add(String key, long value) {
        return add(key, String.valueOf(value));
    }

    default Param add(String key, double value) {
        return add(key, String.valueOf(value));
    }

    default Param add(String key, float value) {
        return add(key, String.valueOf(value));
    }

    default Param add(String key, boolean value) {
        return add(key, String.valueOf(value));
    }

    default Param add(String key, char value) {
        return add(key, String.valueOf(value));
    }

    default Param add(String key, char[] value) {
        return add(key, String.valueOf(value));
    }
}
