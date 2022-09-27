package rxhttp.wrapper.entity;

/**
 * User: ljx
 * Date: 2019/1/20
 * Time: 18:15
 */
public class Progress {

    private final int  progress; //当前进度 0-100
    private final long currentSize;//当前已完成的字节大小
    private final long totalSize; //总字节大小

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

    @Override
    public String toString() {
        return "Progress{" +
                "progress=" + progress +
                ", currentSize=" + currentSize +
                ", totalSize=" + totalSize +
                '}';
    }
}
