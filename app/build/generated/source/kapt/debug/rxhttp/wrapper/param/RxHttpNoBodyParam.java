package rxhttp.wrapper.param;

import java.util.List;
import java.util.Map;

import rxhttp.wrapper.annotations.NonNull;
import rxhttp.wrapper.param.NoBodyParam;

/**
 * Github
 * https://github.com/liujingxing/rxhttp
 * https://github.com/liujingxing/rxlife
 * https://github.com/liujingxing/rxhttp/wiki/FAQ
 * https://github.com/liujingxing/rxhttp/wiki/更新日志
 */
public class RxHttpNoBodyParam extends RxHttp<NoBodyParam, RxHttpNoBodyParam> {
    public RxHttpNoBodyParam(NoBodyParam param) {
        super(param);
    }
    
    public RxHttpNoBodyParam add(String key, Object value) {
        return addQuery(key, value);
    }
    
    public RxHttpNoBodyParam add(String key, Object value, boolean isAdd) {
        if (isAdd) {
            addQuery(key, value);
        }
        return this;
    }
    
    public RxHttpNoBodyParam addAll(Map<String, ?> map) {
        return addAllQuery(map);
    }

    public RxHttpNoBodyParam addEncoded(String key, Object value) {
        return addEncodedQuery(key, value);
    }
    
    public RxHttpNoBodyParam addAllEncoded(@NonNull Map<String, ?> map) {
        return addAllEncodedQuery(map);
    }

    public RxHttpNoBodyParam set(String key, Object value) {
        return setQuery(key, value);
    }

    public RxHttpNoBodyParam setEncoded(String key, Object value) {
        return setEncodedQuery(key, value); 
    }
}
