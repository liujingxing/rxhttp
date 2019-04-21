package rxhttp.wrapper.callback;

/**
 * 上传/下载进度回调接口
 * User: ljx
 * Date: 2017/12/1
 * Time: 20:22
 */
public interface ProgressCallback {

    /**
     * 回调时机:
     * 1、当前进度大于上一次回调进度;
     * 2、进度小于100且两次回调时间间隔大于一个时间阀值(例如50毫秒) 注:要保证进度为100的事件能够回调，故加入进度判断
     * 只有以上两个条件同时满足，才会执行回调
     *
     * @param progress    当前进度
     * @param currentSize 当前上传/下载完成的字节大小
     * @param totalSize   上传/下载总字节大小
     */
    void onProgress(int progress, long currentSize, long totalSize);
}
