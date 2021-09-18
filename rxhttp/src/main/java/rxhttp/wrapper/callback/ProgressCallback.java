package rxhttp.wrapper.callback;


/**
 * User: ljx
 * Date: 2017/12/1
 * Time: 20:22
 */
public interface ProgressCallback {

    void onProgress(int progress, long currentSize, long totalSize);
}
