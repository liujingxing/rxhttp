package rxhttp.wrapper.entity;


import rxhttp.wrapper.annotations.NonNull;

/**
 * User: ljx
 * Date: 2019-11-15
 * Time: 22:44
 */
public class KeyValuePair {

    private String key;
    private Object value;
    private boolean isEncoded;

    public KeyValuePair(String key, Object object) {
        this(key, object, false);
    }

    public KeyValuePair(String key, Object value, boolean isEncoded) {
        this.key = key;
        this.value = value;
        this.isEncoded = isEncoded;
    }

    public String getKey() {
        return key;
    }

    public Object getValue() {
        return value;
    }

    public boolean isEncoded() {
        return isEncoded;
    }

    public boolean equals(@NonNull String key) {
        return key.equals(getKey());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj instanceof String && obj.equals(getKey())) {
            return true;
        }
        if (obj instanceof KeyValuePair && ((KeyValuePair) obj).getKey().equals(getKey())) {
            return true;
        }
        return super.equals(obj);
    }
}
