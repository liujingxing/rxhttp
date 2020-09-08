package rxhttp.wrapper.callback;

import rxhttp.wrapper.entity.Progress;

/**
 * User: ljx
 * Date: 2017/12/1
 * Time: 20:22
 */
public interface ProgressCallback {

    void onProgress(Progress progress);
}
