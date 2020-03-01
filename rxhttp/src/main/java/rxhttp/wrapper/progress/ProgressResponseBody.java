package rxhttp.wrapper.progress;


import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.Buffer;
import okio.BufferedSource;
import okio.ForwardingSource;
import okio.Okio;
import okio.Source;
import rxhttp.wrapper.callback.ProgressCallback;

/**
 * 文件下载，带进度的响应实体
 */
public class ProgressResponseBody extends ResponseBody {
    private static final int MIN_INTERVAL = 50;

    //实际的待包装响应体
    private final ResponseBody responseBody;
    //进度回调接口
    private volatile ProgressCallback callback;
    //包装完成的BufferedSource
    private BufferedSource bufferedSource;

    private long contentLength; //ResponseBody 内容长度，部分接口拿不到，会返回-1，此时会没有进度回调

    /**
     * 构造函数，赋值
     *
     * @param response 响应体
     * @param callback 回调接口
     */
    public ProgressResponseBody(Response response, ProgressCallback callback) {
        this.responseBody = response.body();
        this.callback = callback;
        if (responseBody != null) {
            contentLength = responseBody.contentLength();
        }
        if (contentLength == -1) {
            contentLength = getContentLengthByHeader(response);
        }
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
        return contentLength;
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
                if (bytesRead == -1) {   //-1 代表读取完毕
                    if (contentLength == -1) contentLength = totalBytesRead;
                } else {
                    totalBytesRead += bytesRead; //未读取完，则累加已读取的字节
                }

                //当前进度 = 当前已读取的字节 / 总字节
                final int currentProgress = (int) ((totalBytesRead * 100) / contentLength);
                if (currentProgress > lastProgress) {  //前进度大于上次进度，则更新进度
                    if (currentProgress < 100) {
                        long currentTime = System.currentTimeMillis();
                        //两次回调时间小于 MIN_INTERVAL 毫秒，直接返回，避免更新太频繁
                        if (currentTime - lastTime < MIN_INTERVAL) return bytesRead;
                        lastTime = currentTime;
                    }
                    lastProgress = currentProgress;
                    //回调,更新进度
                    updateProgress(lastProgress, totalBytesRead, contentLength);
                }
                return bytesRead;
            }
        };
    }

    private void updateProgress(final int progress, final long currentSize, final long totalSize) {
        if (callback == null) return;
        callback.onProgress(progress, currentSize, totalSize);
    }

    //从响应头 Content-Range 中，取 contentLength
    private long getContentLengthByHeader(Response response) {
        String headerValue = response.header("Content-Range");
        long contentLength = -1;
        if (headerValue != null) {
            //响应头Content-Range格式 : bytes 100001-20000000/20000001
            try {
                int divideIndex = headerValue.indexOf("/"); //斜杠下标
                int blankIndex = headerValue.indexOf(" ");
                String fromToValue = headerValue.substring(blankIndex + 1, divideIndex);
                String[] split = fromToValue.split("-");
                long start = Long.parseLong(split[0]); //开始下载位置
                long end = Long.parseLong(split[1]);   //结束下载位置
                contentLength = end - start + 1;       //要下载的总长度
            } catch (Exception ignore) {
            }
        }
        return contentLength;
    }
}