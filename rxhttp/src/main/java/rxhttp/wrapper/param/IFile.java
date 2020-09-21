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

    @Deprecated
    default P add(String key, File file) {
        return addFile(new UpFile(key, file));
    }

    default P addFile(String key, File file) {
        return addFile(new UpFile(key, file));
    }

    default P addFile(String key, String filePath) {
        return addFile(new UpFile(key, filePath));
    }

    default P addFile(String key, String fileName, String filePath) {
        return addFile(new UpFile(key, fileName, filePath));
    }

    default P addFile(String key, String fileName, File file) {
        return addFile(new UpFile(key, fileName, file));
    }

    default P addFile(String key, List<? extends File> fileList) {
        for (File file : fileList) {
            addFile(new UpFile(key, file));
        }
        return (P) this;
    }

    default P addFile(Map<String, ? extends File> fileMap) {
        for (Map.Entry<String, ? extends File> entry : fileMap.entrySet()) {
            addFile(new UpFile(entry.getKey(), entry.getValue()));
        }
        return (P) this;
    }

    default P addFile(List<? extends UpFile> upFileList) {
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
