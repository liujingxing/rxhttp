package rxhttp.wrapper.param;

import java.io.File;
import java.util.List;

import rxhttp.wrapper.annotations.NonNull;
import rxhttp.wrapper.callback.ProgressCallback;
import rxhttp.wrapper.entity.UpFile;

/**
 * User: ljx
 * Date: 2019-05-19
 * Time: 18:18
 */
@SuppressWarnings("unchecked")
public interface IFile<P extends Param<P>> {

    default P add(String key, File file) {
        return addFile(key, file.getAbsolutePath());
    }

    default P addFile(String key, File file) {
        return addFile(key, file.getAbsolutePath());
    }

    default P addFile(String key, String filePath) {
        return addFile(new UpFile(key, filePath));
    }

    default P addFile(String key, String value, String filePath) {
        UpFile upFile = new UpFile(key, filePath);
        upFile.setValue(value);
        return addFile(upFile);
    }

    default P addFile(String key, String value, File file) {
        return addFile(key, value, file.getAbsolutePath());
    }

    default P addFile(String key, List<File> fileList) {
        for (File file : fileList) {
            addFile(new UpFile(key, file.getAbsolutePath()));
        }
        return (P) this;
    }

    default P addFile(List<UpFile> upFileList) {
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

    /**
     * 根据key 移除已添加的文件
     *
     * @param key String
     * @return Param
     */
    P removeFile(String key);

    P setUploadMaxLength(long maxLength);

    /**
     * <p>设置上传进度监听器
     *
     * @param callback 进度回调对象
     * @return Param
     */
    P setProgressCallback(ProgressCallback callback);
}
