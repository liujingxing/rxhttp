package rxhttp.wrapper.entity;

/**
 * User: ljx
 * Date: 2019/1/20
 * Time: 18:15
 *
 * It is NOT thread safe.
 */
public class Progress<T> {

    private int  progress; //当前进度 0-100
    private long currentSize;//当前已完成的字节大小
    private long totalSize; //总字节大小

    private T mResult; //http返回结果,上传/下载完成时调用

    public Progress() {
    }

    public Progress(int progress, long currentSize, long totalSize) {
        this.progress = progress;
        this.currentSize = currentSize;
        this.totalSize = totalSize;
    }

    public void set(Progress<?> progress) {
        this.progress = progress.progress;
        this.currentSize = progress.currentSize;
        this.totalSize = progress.totalSize;
    }

    /**
     * @return 上传/下载是否完成
     */
    public boolean isCompleted() {
        return progress == 100;
    }

    public int getProgress() {
        return progress;
    }

    public long getCurrentSize() {
        return currentSize;
    }

    public long getTotalSize() {
        return totalSize;
    }

    public T getResult() {
        return mResult;
    }

    public void updateProgress() {
        this.progress = (int) (currentSize * 100 / totalSize);
    }

    public void setCurrentSize(long currentSize) {
        this.currentSize = currentSize;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }

    public void setTotalSize(long totalSize) {
        this.totalSize = totalSize;
    }

    public void addTotalSize(long addSize) {
        totalSize += addSize;
    }

    public void addCurrentSize(long addSize) {
        currentSize += addSize;
    }

    public void setResult(T result) {
        mResult = result;
    }

    @Override
    public String toString() {
        return "Progress{" +
                "progress=" + progress +
                ", currentSize=" + currentSize +
                ", totalSize=" + totalSize +
                ", mResult=" + mResult +
                '}';
    }
}
