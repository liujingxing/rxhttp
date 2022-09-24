package rxhttp.wrapper.param;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.MultipartBody.Part;
import okhttp3.RequestBody;
import rxhttp.wrapper.entity.KeyValuePair;
import rxhttp.wrapper.utils.BuildUtil;
import rxhttp.wrapper.utils.CacheUtil;

/**
 * post/put/patch/delete request
 *
 * Content-Type: application/x-www-form-urlencoded
 *
 * if have file, Content-Type: multipart/form-data
 *
 * call {@link FormParam#setMultiType(MediaType)}, specify Content-Type
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

    public FormParam addAllEncoded(@NotNull Map<String, ?> map) {
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
            if (!isMultipart()) setMultiType(MultipartBody.FORM);
        }
        partList.add(part);
        return this;
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
