package rxhttp.wrapper.param;

import java.io.File;
import java.util.List;
import java.util.Map;

import rxhttp.wrapper.annotations.NonNull;
import rxhttp.wrapper.entity.UpFile;

/**
 * User: ljx
 * Date: 2019-05-19
 * Time: 18:18
 */
@SuppressWarnings("unchecked")
public interface IFile<P extends Param<P>> {

    default P addFile(String key, File file) {
        return addFile(new UpFile(key, file));
    }

    default P addFile(String key, String filePath) {
        return addFile(new UpFile(key, filePath));
    }

    default P addFile(String key, File file, String filename) {
        return addFile(new UpFile(key, file, filename));
    }

    default <T> P addFiles(String key, List<T> list) {
        for (T src : list) {
            if (src instanceof String) {
                addFile(new UpFile(key, src.toString()));
            } else if (src instanceof File) {
                addFile(new UpFile(key, (File) src));
            } else {
                throw new IllegalArgumentException("Incoming data type exception, it must be String or File");
            }
        }
        return (P) this;
    }

    default <T> P addFiles(Map<String, T> fileMap) {
        for (Map.Entry<String, T> entry : fileMap.entrySet()) {
            String key = entry.getKey();
            T value = entry.getValue();
            if (value instanceof String) {
                addFile(new UpFile(key, value.toString()));
            } else if (value instanceof File) {
                addFile(new UpFile(key, (File) value));
            } else {
                throw new IllegalArgumentException("Incoming data type exception, it must be String or File");
            }
        }
        return (P) this;
    }

    default P addFiles(List<? extends UpFile> upFileList) {
        for (UpFile upFile : upFileList) {
            addFile(upFile);
        }
        return (P) this;
    }

    /**
     * <p>添加文件对象
     *
     * @param upFile UpFile
     * @return Param
     */
    P addFile(@NonNull UpFile upFile);
}
