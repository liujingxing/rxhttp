package rxhttp.wrapper.entity;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okio.BufferedSink;
import okio.Okio;
import okio.Source;
import rxhttp.wrapper.OkHttpCompat;
import rxhttp.wrapper.utils.BuildUtil;
import rxhttp.wrapper.utils.UriUtil;

/**
 * For compatibility with okHTTP 3.x version, only written in Java
 * User: ljx
 * Date: 2020/9/13
 * Time: 21:13
 */
public class UriRequestBody extends RequestBody {

    private final Uri uri;
    private final long skipSize;
    private final MediaType contentType;
    private final ContentResolver contentResolver;

    public UriRequestBody(Context context, Uri uri) {
        this(context, uri, 0, BuildUtil.getMediaTypeByUri(context, uri));
    }

    public UriRequestBody(Context context, Uri uri, @Nullable MediaType contentType) {
        this(context, uri, 0, contentType);
    }

    public UriRequestBody(Context context, Uri uri, long skipSize) {
        this(context, uri, skipSize, BuildUtil.getMediaTypeByUri(context, uri));
    }

    public UriRequestBody(Context context, Uri uri, long skipSize, @Nullable MediaType contentType) {
        this.uri = uri;
        this.skipSize = skipSize;
        this.contentType = contentType;
        contentResolver = context.getContentResolver();
    }

    @Override
    public MediaType contentType() {
        return contentType;
    }

    @Override
    public long contentLength() throws IOException {
        long realLength = UriUtil.length(uri, contentResolver);
        if (realLength > 0 && skipSize > 0 && realLength > skipSize) {
            realLength -= skipSize;
        }
        return realLength;
    }

    @Override
    public void writeTo(@NotNull BufferedSink sink) throws IOException {
        InputStream input = null;
        Source source = null;
        try {
            input = contentResolver.openInputStream(uri);
            if (skipSize > 0) input.skip(skipSize);
            source = Okio.source(input);
            sink.writeAll(source);
        } finally {
            OkHttpCompat.closeQuietly(source, input);
        }
    }
}
