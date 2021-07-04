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
import rxhttp.wrapper.entity.Progress;

//Track upload progress
public class ProgressRequestBody extends RequestBody {

    private final RequestBody requestBody;
    private final ProgressCallback callback;

    public ProgressRequestBody(RequestBody requestBody, ProgressCallback callback) {
        this.requestBody = requestBody;
        this.callback = callback;
    }

    @Override
    public MediaType contentType() {
        return requestBody.contentType();
    }

    @Override
    public long contentLength() throws IOException {
        return requestBody.contentLength();
    }

    @Override
    public void writeTo(BufferedSink sink) throws IOException {
        if (sink instanceof Buffer
            && sink.toString().contains(
            "com.android.tools.profiler.support.network.HttpTracker$OutputStreamTracker")) {
            requestBody.writeTo(sink);
        } else {
            BufferedSink bufferedSink = Okio.buffer(sink(sink));
            requestBody.writeTo(bufferedSink);
            bufferedSink.close();
        }
    }

    private Sink sink(Sink sink) {
        return new ForwardingSink(sink) {
            long bytesWritten = 0L;
            long contentLength = 0L;
            int lastProgress;

            @Override
            public void write(Buffer source, long byteCount) throws IOException {
                super.write(source, byteCount);
                if (contentLength == 0) {
                    contentLength = contentLength();
                }
                bytesWritten += byteCount;

                int currentProgress = (int) ((bytesWritten * 100) / contentLength);
                if (currentProgress > lastProgress) {
                    lastProgress = currentProgress;
                    updateProgress(lastProgress, bytesWritten, contentLength);
                }
            }
        };
    }

    private void updateProgress(int progress, long currentSize, long totalSize) {
        if (callback == null) return;
        Progress p = new Progress(progress, currentSize, totalSize);
        callback.onProgress(p);
    }
}
