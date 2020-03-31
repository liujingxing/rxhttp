package rxhttp.wrapper.param;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import okhttp3.RequestBody;
import rxhttp.wrapper.annotations.NonNull;
import rxhttp.wrapper.annotations.Nullable;
import rxhttp.wrapper.callback.ProgressCallback;
import rxhttp.wrapper.entity.KeyValuePair;
import rxhttp.wrapper.entity.UpFile;
import rxhttp.wrapper.progress.ProgressRequestBody;
import rxhttp.wrapper.utils.BuildUtil;
import rxhttp.wrapper.utils.CacheUtil;

/**
 * post、put、patch、delete请求
 * 参数以{application/x-www-form-urlencoded}形式提交
 * 当带有文件时，自动以{multipart/form-data}形式提交
 * 当调用{@link #setMultiForm()}方法，强制以{multipart/form-data}形式提交
 * <p>
 * User: ljx
 * Date: 2019-09-09
 * Time: 21:08
 */
public class FormParam extends AbstractParam<FormParam> implements IUploadLengthLimit, IFile<FormParam> {

    private ProgressCallback mCallback; //上传进度回调
    private List<UpFile> mFileList;  //附件集合
    private List<KeyValuePair> mKeyValuePairs; //请求参数

    private long uploadMaxLength = Integer.MAX_VALUE;//文件上传最大长度
    private boolean isMultiForm;

    /**
     * @param url    请求路径
     * @param method Method#POST  Method#PUT  Method#DELETE  Method#PATCH
     */
    public FormParam(String url, Method method) {
        super(url, method);
    }

    @Override
    public FormParam add(String key, Object value) {
        if (value == null) value = "";
        return add(new KeyValuePair(key, value));
    }

    public FormParam addEncoded(String key, Object value) {
        return add(new KeyValuePair(key, value, true));
    }

    public FormParam removeAllBody(String key) {
        final List<KeyValuePair> keyValuePairs = mKeyValuePairs;
        if (keyValuePairs == null) return this;
        Iterator<KeyValuePair> iterator = keyValuePairs.iterator();
        while (iterator.hasNext()) {
            KeyValuePair next = iterator.next();
            if (next.equals(key))
                iterator.remove();
        }
        return this;
    }

    public FormParam removeAllBody() {
        final List<KeyValuePair> keyValuePairs = mKeyValuePairs;
        if (keyValuePairs != null)
            keyValuePairs.clear();
        return this;
    }

    public FormParam set(String key, Object value) {
        removeAllBody(key);
        return add(key, value);
    }

    public FormParam setEncoded(String key, Object value) {
        removeAllBody(key);
        return addEncoded(key, value);
    }

    @Nullable
    public Object queryValue(String key) {
        final List<KeyValuePair> keyValuePairs = mKeyValuePairs;
        if (keyValuePairs == null) return this;
        for (KeyValuePair pair : keyValuePairs) {
            if (pair.equals(key))
                return pair.getValue();
        }
        return null;
    }

    @NonNull
    public List<Object> queryValues(String key) {
        final List<KeyValuePair> keyValuePairs = mKeyValuePairs;
        if (keyValuePairs == null) return Collections.emptyList();
        List<Object> values = new ArrayList<>();
        for (KeyValuePair pair : keyValuePairs) {
            if (pair.equals(key))
                values.add(pair.getValue());
        }
        return Collections.unmodifiableList(values);
    }

    private FormParam add(KeyValuePair keyValuePair) {
        List<KeyValuePair> keyValuePairs = mKeyValuePairs;
        if (keyValuePairs == null) {
            keyValuePairs = mKeyValuePairs = new ArrayList<>();
        }
        keyValuePairs.add(keyValuePair);
        return this;
    }

    @Override
    public FormParam addFile(@NonNull UpFile upFile) {
        List<UpFile> fileList = mFileList;
        if (fileList == null)
            fileList = mFileList = new ArrayList<>();
        fileList.add(upFile);
        return this;
    }

    @Override
    public FormParam removeFile(String key) {
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

    private long getTotalFileLength() {
        if (mFileList == null) return 0;
        long totalLength = 0;
        for (UpFile upFile : mFileList) {
            if (upFile == null) continue;
            totalLength += upFile.length();
        }
        return totalLength;
    }

    @Override
    public FormParam setUploadMaxLength(long maxLength) {
        uploadMaxLength = maxLength;
        return this;
    }

    /**
     * 设置提交方式为{multipart/form-data}
     *
     * @return FormParam
     */
    public FormParam setMultiForm() {
        isMultiForm = true;
        return this;
    }

    @Override
    public void checkLength() {
        long totalFileLength = getTotalFileLength();
        if (totalFileLength > uploadMaxLength)
            throw new IllegalArgumentException("The current total file length is " + totalFileLength + " byte, " +
                "this length cannot be greater than " + uploadMaxLength + " byte");
    }

    /**
     * 设置上传进度监听器
     *
     * @param callback 进度回调对象
     * @return FormParam
     */
    @Override
    public final FormParam setProgressCallback(ProgressCallback callback) {
        mCallback = callback;
        return this;
    }

    @Override
    public RequestBody getRequestBody() {
        final List<KeyValuePair> keyValuePairs = mKeyValuePairs;
        RequestBody requestBody = isMultiForm || hasFile() ?
            BuildUtil.buildFormRequestBody(keyValuePairs, mFileList)
            : BuildUtil.buildFormRequestBody(keyValuePairs);
        final ProgressCallback callback = mCallback;
        if (callback != null) {
            //如果设置了进度回调，则对RequestBody进行装饰
            return new ProgressRequestBody(requestBody, callback);
        }
        return requestBody;
    }

    public ProgressCallback getCallback() {
        return mCallback;
    }

    public List<UpFile> getFileList() {
        return mFileList;
    }

    public List<KeyValuePair> getKeyValuePairs() {
        return mKeyValuePairs;
    }

    public boolean isMultiForm() {
        return isMultiForm;
    }

    @Override
    public String getCacheKey() {
        String cacheKey = super.getCacheKey();
        if (cacheKey != null) return cacheKey;
        List<KeyValuePair> keyValuePairs = CacheUtil.excludeCacheKey(mKeyValuePairs);
        return BuildUtil.getHttpUrl(getSimpleUrl(), keyValuePairs).toString();
    }

    @Override
    public String toString() {
        return BuildUtil.getHttpUrl(getSimpleUrl(), mKeyValuePairs).toString();
    }
}
