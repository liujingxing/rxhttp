package rxhttp.wrapper.param;


import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import io.reactivex.annotations.NonNull;
import okhttp3.RequestBody;
import rxhttp.wrapper.callback.ProgressCallback;
import rxhttp.wrapper.entity.UpFile;
import rxhttp.wrapper.progress.ProgressRequestBody;
import rxhttp.wrapper.utils.BuildUtil;

/**
 * 发送Post请求，参数以form表单键值对的形式提交
 * User: ljx
 * Date: 2019/1/19
 * Time: 11:36
 */
public class PostFormParam extends AbstractPostParam {

    private ProgressCallback mCallback; //上传进度回调
    private List<UpFile>     mFileList;  //附件集合

    protected PostFormParam(@NonNull String url) {
        super(url);
    }

    static PostFormParam with(String url) {
        return new PostFormParam(url);
    }

    /**
     * 设置上传进度监听器
     *
     * @param callback 进度回调对象
     * @return PostFormParam
     */
    @Override
    public final PostFormParam setProgressCallback(ProgressCallback callback) {
        mCallback = callback;
        return this;
    }

    @Override
    public RequestBody getRequestBody() {
        RequestBody requestBody = hasFile() ? BuildUtil.buildFormRequestBody(this, mFileList)
                : BuildUtil.buildFormRequestBody(this);
        final ProgressCallback callback = mCallback;
        if (callback != null) {
            //如果设置了进度回调，则对RequestBody进行装饰
            return new ProgressRequestBody(requestBody, callback);
        }
        return requestBody;
    }

    @Override
    public Param addFile(@NonNull UpFile upFile) {
        List<UpFile> fileList = mFileList;
        if (fileList == null)
            fileList = mFileList = new ArrayList<>();
        fileList.add(upFile);
        return this;
    }

    @Override
    public Param removeFile(String key) {
        final List<UpFile> fileList = mFileList;
        if (fileList == null || key == null) return this;
        Iterator<UpFile> it = fileList.iterator();
        while (it.hasNext()) {
            UpFile upFile = it.next();
            if (key.equals(upFile.getKey())) {
                it.remove();
            }
        }
        return this;
    }

    private boolean hasFile() {
        final List list = mFileList;
        return list != null && list.size() > 0;
    }
}
