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
public interface FileBuilder {

    Param addFile(@NonNull UpFile upFile);

    default Param add(String key, File file) {
        return addFile(key, file.getAbsolutePath());
    }

    default Param addFile(String key, File file) {
        return addFile(key, file.getAbsolutePath());
    }

    default Param addFile(String key, String filePath) {
        return addFile(new UpFile(key, filePath));
    }

    default Param addFile(String key, String value, String filePath) {
        UpFile upFile = new UpFile(key, filePath);
        upFile.setValue(value);
        return addFile(upFile);
    }

    default Param addFile(String key, String value, File file) {
        return addFile(key, value, file.getAbsolutePath());
    }

    Param addFile(String key, List<File> fileList);

    Param addFile(List<UpFile> upFile);

    Param removeFile(String key);

    default Param setUploadMaxLength(long maxLength) {
        throw new UnsupportedOperationException("Please override setUploadMaxLength method if you need");
    }

    /**
     * <p>设置上传进度监听器
     * <p>默认不支持,如有需要，自行扩展，参考{@link PostFormParam}
     *
     * @param callback 进度回调对象
     * @return Param
     */
    default Param setProgressCallback(ProgressCallback callback) {
        throw new UnsupportedOperationException("Please override setProgressCallback method if you need");
    }

}
