package httpsender.wrapper.progress;


import java.io.IOException;

import httpsender.wrapper.callback.ProgressCallback;
import okhttp3.MediaType;
import okhttp3.ResponseBody;
import okio.*;

/**
 * 文件下载，带进度的响应实体
 */
public class ProgressResponseBody extends ResponseBody {
    public static final int MIN_INTERVAL = 50;

    //实际的待包装响应体
    private final    ResponseBody     responseBody;
    //进度回调接口
    private volatile ProgressCallback callback;
    //包装完成的BufferedSource
    private          BufferedSource   bufferedSource;

    /**
     * 构造函数，赋值
     *
     * @param responseBody 待包装的响应体
     * @param callback     回调接口
     */
    public ProgressResponseBody(ResponseBody responseBody, ProgressCallback callback) {
        this.responseBody = responseBody;
        this.callback = callback;
    }


    /**
     * 重写调用实际的响应体的contentType
     *
     * @return MediaType
     */
    @Override
    public MediaType contentType() {
        return responseBody.contentType();
    }

    /**
     * 重写调用实际的响应体的contentLength
     *
     * @return contentLength
     */
    @Override
    public long contentLength() {
        return responseBody.contentLength();
    }

    /**
     * 重写进行包装source
     *
     * @return BufferedSource
     */
    @Override
    public BufferedSource source() {
        if (bufferedSource == null) {
            //包装
            bufferedSource = Okio.buffer(source(responseBody.source()));
        }
        return bufferedSource;
    }

    /**
     * 读取，回调进度接口
     *
     * @param source Source
     * @return Source
     */
    private Source source(Source source) {

        return new ForwardingSource(source) {
            //当前读取字节数
            long totalBytesRead = 0L;
            int lastProgress; //上次回调进度
            long lastTime;//上次回调时间

            @Override
            public long read(Buffer sink, long byteCount) throws IOException {
                long bytesRead = super.read(sink, byteCount);
                //增加当前读取的字节数，如果读取完成了bytesRead会返回-1
                totalBytesRead += bytesRead != -1 ? bytesRead : 0;
                final long fileSize = responseBody.contentLength();
                //回调，如果contentLength()不知道长度，会返回-1
                final int currentProgress = (int) ((totalBytesRead * 100) / fileSize);
                //当前进度较上次没有更新，直接返回
                if (currentProgress <= lastProgress) return bytesRead;
                //当前进度小于100,需要判断两次回调时间间隔是否小于一定时间,是的话直接返回
                if (currentProgress < 100) {
                    long currentTime = System.currentTimeMillis();
                    //两次回调时间小于 MIN_INTERVAL 毫秒，直接返回，避免更新太频繁
                    if (currentTime - lastTime < MIN_INTERVAL) return bytesRead;
                    lastTime = currentTime;
                }
                lastProgress = currentProgress;
                //回调,更新进度
                updateProgress(lastProgress, totalBytesRead, fileSize);
                return bytesRead;
            }
        };
    }

    private void updateProgress(final int progress, final long currentSize, final long totalSize) {
        if (callback == null) return;
        callback.onProgress(progress, currentSize, totalSize);
    }
}