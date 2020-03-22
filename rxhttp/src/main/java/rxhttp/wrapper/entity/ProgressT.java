package rxhttp.wrapper.entity;

/**
 * User: ljx
 * Date: 2019/1/20
 * Time: 18:15
 * <p>
 * It is NOT thread safe.
 */
public class ProgressT<T> extends Progress {

    private T mResult; //http返回结果,上传/下载完成时调用

    public ProgressT() {
    }

    public T getResult() {
        return mResult;
    }

    public void setResult(T result) {
        mResult = result;
    }

    @Override
    public String toString() {
        return "ProgressT{" +
            "progress=" + getProgress() +
            ", currentSize=" + getCurrentSize() +
            ", totalSize=" + getTotalSize() +
            ", mResult=" + mResult +
            '}';
    }
}
