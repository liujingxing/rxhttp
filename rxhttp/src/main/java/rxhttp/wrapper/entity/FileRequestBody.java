package rxhttp.wrapper.entity;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okio.BufferedSink;
import okio.Okio;
import okio.Source;
import rxhttp.wrapper.OkHttpCompat;
import rxhttp.wrapper.annotations.Nullable;

/**
 * For compatibility with okHTTP 3.x version, only written in Java
 * User: ljx
 * Date: 2021/6/24
 * Time: 21:13
 */
public class FileRequestBody extends RequestBody {

    private final File file;
    private final long skipSize;
    private final MediaType mediaType;

    public FileRequestBody(File file, long skipSize, @Nullable MediaType mediaType) {
        this.file = file;
        if (skipSize < 0) {
            throw new IllegalArgumentException("skipSize >= 0 required but it was " + skipSize);
        }
        if (skipSize > file.length()) {
            throw new IllegalArgumentException("skipSize cannot be larger than the file length. " +
                "The file length is " + file.length() + ", but it was " + skipSize);
        }
        this.skipSize = skipSize;
        this.mediaType = mediaType;
    }

    @Override
    public MediaType contentType() {
        return mediaType;
    }

    @Override
    public long contentLength() throws IOException {
        return file.length() - skipSize;
    }

    @Override
    public void writeTo(@NotNull BufferedSink sink) throws IOException {
        InputStream input = null;
        Source source = null;
        try {
            input = new FileInputStream(file);
            if (skipSize > 0) {
                long skip = input.skip(skipSize);
                if (skip != skipSize) {
                    throw new IllegalArgumentException(
                        "Expected to skip " + skipSize + " bytes, actually skipped " + skip + " bytes");
                }
            }
            source = Okio.source(input);
            sink.writeAll(source);
        } finally {
            OkHttpCompat.closeQuietly(source, input);
        }
    }
}
