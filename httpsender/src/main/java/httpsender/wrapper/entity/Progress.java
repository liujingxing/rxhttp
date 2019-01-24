package httpsender.wrapper.entity;

/**
 * User: ljx
 * Date: 2019/1/20
 * Time: 18:15
 */
public class Progress<T> {

    private int  progress; //当前进度 0-100
    private long currentSize;//当前已完成的字节大小
    private long totalSize; //总字节大小

    private T mResult; //http返回结果,上传/下载完成时调用

    /**
     * 上传/下载完成时调用,并将进度设置为-1
     *
     * @param result http执行结果
     */
    public Progress(T result) {
        this(-1, -1, -1);
        this.mResult = result;
    }

    public Progress(int progress, long currentSize, long totalSize) {
        this.progress = progress;
        this.currentSize = currentSize;
        this.totalSize = totalSize;
    }

    /**
     * @return 上传/下载是否完成
     */
    public boolean isCompleted() {
        return progress == -1;
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
