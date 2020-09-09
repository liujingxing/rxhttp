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

    public int getProgress() {
        return progress;
    }

    public long getCurrentSize() {
        return currentSize;
    }

    public long getTotalSize() {
        return totalSize;
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

    @Override
    public String toString() {
        return "Progress{" +
                "progress=" + progress +
                ", currentSize=" + currentSize +
                ", totalSize=" + totalSize +
                '}';
    }
}
