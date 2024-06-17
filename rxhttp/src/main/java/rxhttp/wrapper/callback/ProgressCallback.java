package rxhttp.wrapper.callback;


/**
 * User: ljx
 * Date: 2017/12/1
 * Time: 20:22
 */
public interface ProgressCallback {

    void onProgress(long currentSize, long totalSize, long speed);
}
