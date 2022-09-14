package rxhttp.wrapper.param;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.MultipartBody.Part;
import okhttp3.RequestBody;
import rxhttp.wrapper.annotations.NonNull;
import rxhttp.wrapper.annotations.Nullable;
import rxhttp.wrapper.entity.KeyValuePair;
import rxhttp.wrapper.utils.BuildUtil;
import rxhttp.wrapper.utils.CacheUtil;

/**
 * post、put、patch、delete请求
 * 参数以 { application/x-www-form-urlencoded } 形式提交
 * 当带有文件时，自动以 { multipart/form-data } 形式提交
 * <p>
 * 当然，亦可调用一系列 setMultiXxx 方法，指定 { multipart/xxx } 形式提交，如：setMultiForm()
 * <p>
 * User: ljx
 * Date: 2019-09-09
 * Time: 21:08
 */
public class FormParam extends AbstractBodyParam<FormParam> implements IPart<FormParam> {

    private MediaType multiType;

    private List<Part> partList;  //Part List
    private List<KeyValuePair> bodyParam; //Param list

    /**
     * @param url    request url
     * @param method {@link Method#POST}、{@link Method#PUT}、{@link Method#DELETE}、{@link Method#PATCH}
     */
    public FormParam(String url, Method method) {
        super(url, method);
    }

    @Override
    public FormParam add(String key, @Nullable Object value) {
        if (value != null) add(new KeyValuePair(key, value));
        return this;
    }

    public FormParam addEncoded(String key, @Nullable Object value) {
        if (value != null) add(new KeyValuePair(key, value, true));
        return this;
    }

    public FormParam addAllEncoded(@NonNull Map<String, ?> map) {
        for (Entry<String, ?> entry : map.entrySet()) {
            addEncoded(entry.getKey(), entry.getValue());
        }
        return this;
    }

    public FormParam removeAllBody(String key) {
        final List<KeyValuePair> bodyParam = this.bodyParam;
        if (bodyParam == null) return this;
        Iterator<KeyValuePair> iterator = bodyParam.iterator();
        while (iterator.hasNext()) {
            KeyValuePair next = iterator.next();
            if (next.getKey().equals(key))
                iterator.remove();
        }
        return this;
    }

    public FormParam removeAllBody() {
        final List<KeyValuePair> bodyParam = this.bodyParam;
        if (bodyParam != null)
            bodyParam.clear();
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

    private FormParam add(KeyValuePair keyValuePair) {
        List<KeyValuePair> bodyParam = this.bodyParam;
        if (bodyParam == null) {
            bodyParam = this.bodyParam = new ArrayList<>();
        }
        bodyParam.add(keyValuePair);
        return this;
    }

    @Override
    public FormParam addPart(Part part) {
        if (partList == null) {
            partList = new ArrayList<>();
            if (!isMultipart()) setMultiForm();
        }
        partList.add(part);
        return this;
    }

    //Set content-type to multipart/form-data
    public FormParam setMultiForm() {
        return setMultiType(MultipartBody.FORM);
    }

    //Set content-type to multipart/mixed
    public FormParam setMultiMixed() {
        return setMultiType(MultipartBody.MIXED);
    }

    //Set content-type to multipart/alternative
    public FormParam setMultiAlternative() {
        return setMultiType(MultipartBody.ALTERNATIVE);
    }

    //Set content-type to multipart/digest
    public FormParam setMultiDigest() {
        return setMultiType(MultipartBody.DIGEST);
    }

    //Set content-type to multipart/parallel
    public FormParam setMultiParallel() {
        return setMultiType(MultipartBody.PARALLEL);
    }

    //Set the MIME type
    public FormParam setMultiType(MediaType multiType) {
        this.multiType = multiType;
        return this;
    }

    public boolean isMultipart() {
        return multiType != null;
    }

    @Override
    public RequestBody getRequestBody() {
        return isMultipart() ? BuildUtil.buildMultipartBody(multiType, bodyParam, partList)
            : BuildUtil.buildFormBody(bodyParam);
    }

    public List<Part> getPartList() {
        return partList;
    }

    /**
     * @return List
     * @deprecated please use {@link #getBodyParam()} instead, scheduled to be removed in RxHttp 3.0 release.
     */
    @Deprecated
    public List<KeyValuePair> getKeyValuePairs() {
        return getBodyParam();
    }

    public List<KeyValuePair> getBodyParam() {
        return bodyParam;
    }

    @Override
    public String buildCacheKey() {
        List<KeyValuePair> cachePairs = new ArrayList<>();
        List<KeyValuePair> queryPairs = getQueryParam();
        List<KeyValuePair> bodyPairs = bodyParam;
        if (queryPairs != null)
            cachePairs.addAll(queryPairs);
        if (bodyPairs != null)
            cachePairs.addAll(bodyPairs);
        List<KeyValuePair> pairs = CacheUtil.excludeCacheKey(cachePairs);
        return BuildUtil.getHttpUrl(getSimpleUrl(), pairs, getPaths()).toString();
    }

    @Override
    public String toString() {
        String url = getSimpleUrl();
        if (url.startsWith("http")) {
            url = getUrl();
        }
        return "FormParam{" +
            "url = " + url +
            " bodyParam = " + bodyParam +
            '}';
    }
}
