package rxhttp.wrapper.progress;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okio.Buffer;
import okio.BufferedSink;
import okio.ForwardingSink;
import okio.Okio;
import okio.Sink;
import rxhttp.wrapper.callback.ProgressCallbackHelper;

//Track upload progress
public class ProgressRequestBody extends RequestBody {

    @NotNull
    private final RequestBody requestBody;
    @NotNull
    private final ProgressCallbackHelper callback;

    public ProgressRequestBody(@NotNull RequestBody requestBody, @NotNull ProgressCallbackHelper callback) {
        this.requestBody = requestBody;
        this.callback = callback;
    }

    @NotNull
    public RequestBody getRequestBody() {
        return requestBody;
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
    public void writeTo(@NotNull BufferedSink sink) throws IOException {
        if (sink instanceof Buffer
            || sink.toString().contains(
            "com.android.tools.profiler.support.network.HttpTracker$OutputStreamTracker")) {
            requestBody.writeTo(sink);
        } else {
            BufferedSink bufferedSink = Okio.buffer(sink(sink));
            requestBody.writeTo(bufferedSink);
            bufferedSink.close();
        }
    }

    private Sink sink(Sink sink) {
        callback.onStart(0);
        return new ForwardingSink(sink) {
            long contentLength = Long.MIN_VALUE;

            @Override
            public void write(@NotNull Buffer source, long byteCount) throws IOException {
                super.write(source, byteCount);
                if (contentLength == Long.MIN_VALUE) {
                    contentLength = contentLength();
                }
                callback.onProgress(byteCount, contentLength);
            }
        };
    }
}
