package rxhttp.wrapper.param;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import okhttp3.MultipartBody.Part;
import okhttp3.RequestBody;
import rxhttp.wrapper.annotations.NonNull;
import rxhttp.wrapper.annotations.Nullable;
import rxhttp.wrapper.callback.ProgressCallback;
import rxhttp.wrapper.entity.KeyValuePair;
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
public class FormParam extends AbstractParam<FormParam> implements IPart<FormParam> {

    private ProgressCallback mCallback; //上传进度回调
    private List<Part> mPartList;  //附件集合
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
    public FormParam addPart(Part part) {
        List<Part> partList = mPartList;
        if (partList == null)
            partList = mPartList = new ArrayList<>();
        partList.add(part);
        return this;
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
    public final RequestBody getRequestBody() {
        RequestBody requestBody = buildRequestBody(isMultiForm(), mKeyValuePairs, mPartList);
        try {
            long contentLength = requestBody.contentLength();
            if (contentLength > uploadMaxLength)
                throw new IllegalArgumentException("The contentLength cannot be greater than " + uploadMaxLength + " bytes, " +
                    "the current contentLength is " + contentLength + " bytes");
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
        final ProgressCallback callback = mCallback;
        if (callback != null) {
            //如果设置了进度回调，则对RequestBody进行装饰
            return new ProgressRequestBody(requestBody, callback);
        }
        return requestBody;
    }

    protected RequestBody buildRequestBody(
        boolean isMultiForm,
        List<KeyValuePair> keyValuePairs,
        List<Part> partList
    ) {
        return isMultiForm ? BuildUtil.buildFormRequestBody(keyValuePairs, partList)
            : BuildUtil.buildFormRequestBody(keyValuePairs);
    }

    public ProgressCallback getCallback() {
        return mCallback;
    }

    public List<Part> getPartList() {
        return mPartList;
    }

    public List<KeyValuePair> getKeyValuePairs() {
        return mKeyValuePairs;
    }

    public boolean isMultiForm() {
        if (isMultiForm) return true;
        final List<?> list = mPartList;
        return list != null && !list.isEmpty();
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
