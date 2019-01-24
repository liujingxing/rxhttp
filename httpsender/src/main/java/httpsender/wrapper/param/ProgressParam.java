package httpsender.wrapper.param;

import httpsender.wrapper.callback.ProgressCallback;

/**
 * User: ljx
 * Date: 2019/1/23
 * Time: 13:39
 */
public interface ProgressParam {

    /**
     * 设置上传进度监听器
     *
     * @param callback 进度回调对象
     * @return Param
     */
    Param setProgressCallback(ProgressCallback callback);
}
