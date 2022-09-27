package rxhttp.wrapper.entity;

import android.content.Context;
import android.net.Uri;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;

/**
 * User: ljx
 * Date: 2022/9/27
 * Time: 17:16
 */
public class ExpandOutputStream<T> {

    public final T expand;
    public final OutputStream os;

    public ExpandOutputStream(T expand, OutputStream os) {
        this.expand = expand;
        this.os = os;
    }

    public static ExpandOutputStream<String> with(File file, boolean append) throws FileNotFoundException {
        return new ExpandOutputStream<>(file.getAbsolutePath(), new FileOutputStream(file, append));
    }

    public static ExpandOutputStream<Uri> with(Context context, Uri uri, boolean append) throws FileNotFoundException {
        OutputStream os = context.getContentResolver().openOutputStream(uri, append ? "wa" : "w");
        return new ExpandOutputStream<>(uri, os);
    }
}
