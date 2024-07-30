package rxhttp.wrapper.entity;

import org.jetbrains.annotations.Nullable;

/**
 * User: ljx
 * Date: 2019/1/20
 * Time: 18:15
 */
public class Progress<T> {

    private final long currentSize;//当前已完成的字节大小
    private final long totalSize; //总字节大小
    private final long speed;  //网速, 1秒更新一次 单位: byte/s

    @Nullable
    private T result; //http返回结果,上传/下载完成时才有值

    public Progress(@Nullable T result) {
        this(0, 0, 0);
        this.result = result;
    }

    public Progress(long currentSize, long totalSize, long speed) {
        this.currentSize = currentSize;
        this.totalSize = totalSize;
        this.speed = speed;
    }

    @Nullable
    public T getResult() {
        return result;
    }

    //返回上传/下载速度，单位: byte/s
    public long getSpeed() {
        return speed;
    }

    //根据当前速度计算，上传/下载剩余时间，单位: s (注：剩余时间是不精准的)
    public long calculateRemainingTime() {
        if (totalSize == -1) return -1;
        long remainingSize = totalSize - currentSize;
        if (remainingSize == 0) return 0;
        if (speed == 0) return -1;
        long remainingTime = remainingSize / speed;
        if (remainingSize % speed > 0) remainingTime++;
        return remainingTime;
    }

    //return [0, 100]
    public int getProgress() {
        return (int) (getFraction() * 100);
    }

    //return [0.0, 1.0]
    public float getFraction() {
        if (totalSize == -1) return 0.0f;
        if (totalSize < 0) {
            throw new IllegalArgumentException("totalSize must be greater than 0, but it was " + totalSize);
        }
        if (currentSize > totalSize) {
            throw new IllegalArgumentException("totalSize can't be greater than totalSize, " + "currentSize=" + currentSize + " totalSize=" + totalSize);
        }
        return currentSize * 1.0f / totalSize;
    }

    public long getCurrentSize() {
        return currentSize;
    }

    public long getTotalSize() {
        return totalSize;
    }

    @Override
    public String toString() {
        return "Progress{" + "fraction=" + getFraction() + ", currentSize=" + currentSize + ", totalSize=" + totalSize + ", speed=" + speed + '}';
    }
}
