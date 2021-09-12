package rxhttp.wrapper.entity;

/**
 * User: ljx
 * Date: 2019/1/20
 * Time: 18:15
 * <p>
 * It is NOT thread safe.
 */
public class ProgressT<T> extends Progress {

    private T result; //http返回结果,上传/下载完成时调用

    public ProgressT(T result) {
        this.result = result;
    }

    public ProgressT(Progress progress) {
        super(progress.getProgress(), progress.getCurrentSize(), progress.getTotalSize());
    }

    public ProgressT(int currentProgress, long currentSize, long contentLength) {
        super(currentProgress, currentSize, contentLength);
    }

    public T getResult() {
        return result;
    }

    public void setResult(T result) {
        this.result = result;
    }

    @Override
    public String toString() {
        return "ProgressT{" +
            "progress=" + getProgress() +
            ", currentSize=" + getCurrentSize() +
            ", totalSize=" + getTotalSize() +
            ", result=" + result +
            '}';
    }
}
