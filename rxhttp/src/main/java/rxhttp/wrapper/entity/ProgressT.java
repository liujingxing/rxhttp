package rxhttp.wrapper.entity;

/**
 * User: ljx
 * Date: 2019/1/20
 * Time: 18:15
 */
public class ProgressT<T> extends Progress {

    private T result; //http返回结果,上传/下载完成时调用

    public ProgressT(T result) {
        super(0, 0, 0);
        this.result = result;
    }

    public ProgressT(int currentProgress, long currentSize, long contentLength) {
        super(currentProgress, currentSize, contentLength);
    }

    public T getResult() {
        return result;
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
