package rxhttp.wrapper.progress;


import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okio.Buffer;
import okio.BufferedSink;
import okio.ForwardingSink;
import okio.Okio;
import okio.Sink;
import rxhttp.wrapper.callback.ProgressCallback;

//上传文件，带进度的请求实体
public class ProgressRequestBody extends RequestBody {
    public static final int MIN_INTERVAL = 50;

    //实际的待包装请求体
    private final RequestBody requestBody;
    //进度回调接口
    private final ProgressCallback callback;
    //包装完成的BufferedSink
    private BufferedSink bufferedSink;

    /**
     * 构造函数，赋值
     *
     * @param requestBody 待包装的请求体
     * @param callback    回调接口
     */
    public ProgressRequestBody(RequestBody requestBody, ProgressCallback callback) {
        this.requestBody = requestBody;
        this.callback = callback;
    }

    public RequestBody getRequestBody() {
        return requestBody;
    }

    /**
     * 重写调用实际的响应体的contentType
     *
     * @return MediaType
     */
    @Override
    public MediaType contentType() {
        return requestBody.contentType();
    }

    /**
     * 重写调用实际的响应体的contentLength
     *
     * @return contentLength
     * @throws IOException 异常
     */
    @Override
    public long contentLength() throws IOException {
        return requestBody.contentLength();
    }

    /**
     * 重写进行写入
     *
     * @param sink BufferedSink
     * @throws IOException 异常
     */
    @Override
    public void writeTo(BufferedSink sink) throws IOException {
        //此行代码为兼容添加 HttpLoggingInterceptor 拦截器后，上传进度超过100%，达到200%问题
        if (sink instanceof Buffer) return;
        if (bufferedSink == null) {
            //包装
            bufferedSink = Okio.buffer(sink(sink));
        }
        //写入
        requestBody.writeTo(bufferedSink);
        //必须调用flush，否则最后一部分数据可能不会被写入
        bufferedSink.flush();

    }

    /**
     * 写入，回调进度接口
     *
     * @param sink Sink
     * @return Sink
     */
    private Sink sink(Sink sink) {
        return new ForwardingSink(sink) {
            //当前写入字节数
            long bytesWritten = 0L;
            //总字节长度，避免多次调用contentLength()方法
            long contentLength = 0L;

            int lastProgress; //上次回调进度
            long lastTime;//上次回调时间

            @Override
            public void write(Buffer source, long byteCount) throws IOException {
                super.write(source, byteCount);
                if (contentLength == 0) {
                    //获得contentLength的值，后续不再调用
                    contentLength = contentLength();
                }
                //增加当前写入的字节数
                bytesWritten += byteCount;

                int currentProgress = (int) ((bytesWritten * 100) / contentLength);
                if (currentProgress <= lastProgress) return; //进度较上次没有更新，直接返回
                //当前进度小于100,需要判断两次回调时间间隔是否小于一定时间,是的话直接返回
                if (currentProgress < 100) {
                    long currentTime = System.currentTimeMillis();
                    //两次回调时间间隔小于 MIN_INTERVAL 毫秒,直接返回,避免更新太频繁
                    if (currentTime - lastTime < MIN_INTERVAL) return;
                    lastTime = currentTime;
                }
                lastProgress = currentProgress;
                //回调, 更新进度
                updateProgress(lastProgress, bytesWritten, contentLength);
            }
        };
    }

    private void updateProgress(final int progress, final long currentSize, final long totalSize) {
        if (callback == null) return;
        callback.onProgress(progress, currentSize, totalSize);
    }
}
