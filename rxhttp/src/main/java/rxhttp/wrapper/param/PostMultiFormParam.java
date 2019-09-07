package rxhttp.wrapper.param;


import io.reactivex.annotations.NonNull;
import okhttp3.RequestBody;
import rxhttp.wrapper.callback.ProgressCallback;
import rxhttp.wrapper.progress.ProgressRequestBody;
import rxhttp.wrapper.utils.BuildUtil;

/**
 * 发送Post请求，参数以{multipart/form-data}形式提交
 * User: ljx
 * Date: 2019/1/19
 * Time: 11:36
 */
public class PostMultiFormParam extends PostFormParam {

    protected PostMultiFormParam(@NonNull String url) {
        super(url);
    }

    public static PostMultiFormParam with(String url) {
        return new PostMultiFormParam(url);
    }

    @Override
    public RequestBody getRequestBody() {
        RequestBody requestBody = BuildUtil.buildFormRequestBody(this, mFileList);
        final ProgressCallback callback = mCallback;
        if (callback != null) {
            //如果设置了进度回调，则对RequestBody进行装饰
            return new ProgressRequestBody(requestBody, callback);
        }
        return requestBody;
    }

}
