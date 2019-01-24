package httpsender.wrapper.param;


import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;

import httpsender.wrapper.callback.ProgressCallback;
import httpsender.wrapper.progress.ProgressRequestBody;
import httpsender.wrapper.utils.BuildUtil;
import io.reactivex.annotations.NonNull;
import okhttp3.RequestBody;

/**
 * 发送Post请求，参数以form表单键值对的形式提交
 * User: ljx
 * Date: 2019/1/19
 * Time: 11:36
 */
public class PostFormParam extends AbstractPostParam implements ProgressParam {

    private ProgressCallback            mCallback; //上传进度回调
    private LinkedHashMap<String, File> mFileMap;  //附件集合

    private PostFormParam(@NonNull String url) {
        super(url);
    }

    static PostFormParam with(String url) {
        return new PostFormParam(url);
    }

    @Override
    public final PostFormParam setProgressCallback(ProgressCallback callback) {
        mCallback = callback;
        return this;
    }

    @Override
    public RequestBody getRequestBody() {
        RequestBody requestBody = hasFile() ? BuildUtil.buildFormRequestBody(this, mFileMap)
                : BuildUtil.buildFormRequestBody(this);
        final ProgressCallback callback = mCallback;
        if (callback != null) {
            //设置了进度回调，则对RequestBody进行装饰
            return new ProgressRequestBody(requestBody, callback);
        }
        return requestBody;
    }

    @Override
    public final PostFormParam add(String key, File file) {
        Map<String, File> fileMap = mFileMap;
        if (fileMap == null)
            fileMap = mFileMap = new LinkedHashMap<>();
        fileMap.put(key, file);
        return this;
    }

    private boolean hasFile() {
        final Map fileMap = mFileMap;
        return fileMap != null && fileMap.size() > 0;
    }
}
