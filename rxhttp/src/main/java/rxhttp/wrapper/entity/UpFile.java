package rxhttp.wrapper.entity;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;

/**
 * User: ljx
 * Date: 2022/9/27
 * Time: 18:54
 */
public class UpFile {

    public final String key;
    public final File file;
    public final String filename;
    public final long skipSize;

    public UpFile(String key, String path) {
        this(key, new File(path));
    }

    public UpFile(String key, File file) {
        this(key, file, file.getName());
    }

    public UpFile(String key, File file, String filename) {
        this(key, file, filename, 0);
    }

    public UpFile(@NotNull String key, @NotNull File file, @Nullable String filename, long skipSize) {
        this.key = key;
        this.file = file;
        this.filename = filename;
        this.skipSize = skipSize;
    }

    public String getKey() {
        return key;
    }

    public File getFile() {
        return file;
    }

    public String getFilename() {
        return filename;
    }

    public long getSkipSize() {
        return skipSize;
    }
}
