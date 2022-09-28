package rxhttp.wrapper.entity;

import android.content.Context;
import android.net.Uri;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * User: ljx
 * Date: 2022/9/27
 * Time: 17:16
 */
public final class ExpandOutputStream<T> extends OutputStream {

    private final T expand;
    private final OutputStream os;

    public ExpandOutputStream(T expand, OutputStream os) {
        this.expand = expand;
        this.os = os;
    }

    public static ExpandOutputStream<String> open(File file, boolean append) throws FileNotFoundException {
        return new ExpandOutputStream<>(file.getAbsolutePath(), new FileOutputStream(file, append));
    }

    public static ExpandOutputStream<Uri> open(Context context, Uri uri, boolean append) throws FileNotFoundException {
        OutputStream os = context.getContentResolver().openOutputStream(uri, append ? "wa" : "w");
        return new ExpandOutputStream<>(uri, os);
    }

    public T getExpand() {
        return expand;
    }

    @Override
    public void write(int b) throws IOException {
        os.write(b);
    }

    @Override
    public void write(@NotNull byte[] b) throws IOException {
        os.write(b);
    }

    @Override
    public void write(@NotNull byte[] b, int off, int len) throws IOException {
        os.write(b, off, len);
    }

    @Override
    public void close() throws IOException {
        os.close();
    }

    @Override
    public void flush() throws IOException {
        os.flush();
    }
}
