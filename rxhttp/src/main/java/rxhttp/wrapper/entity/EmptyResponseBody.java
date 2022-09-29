package rxhttp.wrapper.entity;

import org.jetbrains.annotations.Nullable;

import okhttp3.MediaType;
import okhttp3.ResponseBody;
import okio.BufferedSource;

/**
 * User: ljx
 * Date: 2022/9/5
 * Time: 15:14
 */
public final class EmptyResponseBody extends ResponseBody {
    private final @Nullable MediaType contentType;
    private final long contentLength;

    public EmptyResponseBody(@Nullable MediaType contentType, long contentLength) {
        this.contentType = contentType;
        this.contentLength = contentLength;
    }

    @Override
    public MediaType contentType() {
        return contentType;
    }

    @Override
    public long contentLength() {
        return contentLength;
    }

    @Override
    public BufferedSource source() {
        throw new IllegalStateException("Cannot read raw response body of a converted body.");
    }
}
