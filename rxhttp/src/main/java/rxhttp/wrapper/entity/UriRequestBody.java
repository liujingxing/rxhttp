package rxhttp.wrapper.entity;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.os.ParcelFileDescriptor;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okio.BufferedSink;
import okio.Okio;

/**
 * For compatibility with okHTTP 3.x version, only written in Java
 * User: ljx
 * Date: 2020/9/13
 * Time: 21:13
 */
public class UriRequestBody extends RequestBody {

    private Uri uri;
    private ContentResolver contentResolver;
    private MediaType defaultMediaType;

    public UriRequestBody(Context context, Uri uri) {
        this(context, uri, null);
    }

    public UriRequestBody(Context context, Uri uri, @Nullable MediaType defaultMediaType) {
        this.uri = uri;
        this.defaultMediaType = defaultMediaType;
        contentResolver = context.getContentResolver();
    }

    @Override
    public MediaType contentType() {
        MediaType mediaType = MediaType.parse(contentResolver.getType(uri));
        return mediaType != null ? mediaType : defaultMediaType;
    }

    @Override
    public long contentLength() throws IOException {
        ParcelFileDescriptor descriptor = contentResolver.openFileDescriptor(uri, "r");
        return descriptor != null ? descriptor.getStatSize() : -1L;
    }

    @Override
    public void writeTo(@NotNull BufferedSink sink) throws IOException {
        InputStream inputStream = contentResolver.openInputStream(uri);
        sink.writeAll(Okio.source(inputStream));
    }
}
