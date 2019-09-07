package rxhttp.wrapper.param;

import java.io.File;
import java.util.List;

import io.reactivex.annotations.NonNull;
import rxhttp.wrapper.callback.ProgressCallback;
import rxhttp.wrapper.entity.UpFile;

/**
 * User: ljx
 * Date: 2019-05-19
 * Time: 18:18
 */
@SuppressWarnings("unchecked")
public interface FileBuilder<T> {

    default T add(String key, File file) {
        return addFile(key, file.getAbsolutePath());
    }

    default T addFile(String key, File file) {
        return addFile(key, file.getAbsolutePath());
    }

    default T addFile(String key, String filePath) {
        return addFile(new UpFile(key, filePath));
    }

    default T addFile(String key, String value, String filePath) {
        UpFile upFile = new UpFile(key, filePath);
        upFile.setValue(value);
        return addFile(upFile);
    }

    default T addFile(String key, String value, File file) {
        return addFile(key, value, file.getAbsolutePath());
    }

    default T addFile(String key, List<File> fileList) {
        for (File file : fileList) {
            addFile(new UpFile(key, file.getAbsolutePath()));
        }
        return (T) this;
    }

    default T addFile(List<UpFile> upFileList) {
        for (UpFile upFile : upFileList) {
            addFile(upFile);
        }
        return (T) this;
    }

    /**
     * <p>添加文件对象
     * <P>默认不支持，如有需要,自行扩展,参考{@link PostFormParam}
     *
     * @param upFile UpFile
     * @return Param
     */
    default T addFile(@NonNull UpFile upFile) {
        throw new UnsupportedOperationException("Please override addFile method if you need");
    }

    /**
     * 根据key 移除已添加的文件
     * 默认不支持，如有需要,自行扩展,参考{@link PostFormParam}
     *
     * @param key String
     * @return Param
     */
    default T removeFile(String key) {
        throw new UnsupportedOperationException("Please override addFile method if you need");
    }

    default T setUploadMaxLength(long maxLength) {
        throw new UnsupportedOperationException("Please override setUploadMaxLength method if you need");
    }

    /**
     * <p>设置上传进度监听器
     * <p>默认不支持,如有需要，自行扩展，参考{@link PostFormParam}
     *
     * @param callback 进度回调对象
     * @return Param
     */
    default T setProgressCallback(ProgressCallback callback) {
        throw new UnsupportedOperationException("Please override setProgressCallback method if you need");
    }
}
