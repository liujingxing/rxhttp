package rxhttp.wrapper.entity;

/**
 * User: ljx
 * Date: 2019/1/20
 * Time: 18:15
 *
 * It is NOT thread safe.
 */
public class Progress {

    private int  progress; //当前进度 0-100
    private long currentSize;//当前已完成的字节大小
    private long totalSize; //总字节大小

    public Progress() {
    }

    public Progress(int progress, long currentSize, long totalSize) {
        this.progress = progress;
        this.currentSize = currentSize;
        this.totalSize = totalSize;
    }

    public void set(Progress progress) {
        this.progress = progress.progress;
        this.currentSize = progress.currentSize;
        this.totalSize = progress.totalSize;
    }

    /**
     * 此方法的前身是isCompleted()
     * 在v1.4.3版本中，对上传/下载进度的回调有改动，不会存在进度为-1的情况；
     * 故无需在上传/下载回调中调用该方法，开发者可直接删除相关代码
     * @return 上传/下载是否完成
     */
    public boolean isFinish() {
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

    @Override
    public String toString() {
        return "Progress{" +
                "progress=" + progress +
                ", currentSize=" + currentSize +
                ", totalSize=" + totalSize +
                '}';
    }
}
