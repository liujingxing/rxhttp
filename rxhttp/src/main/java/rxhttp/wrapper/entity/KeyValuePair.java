package rxhttp.wrapper.entity;


import org.jetbrains.annotations.Nullable;

/**
 * User: ljx
 * Date: 2019-11-15
 * Time: 22:44
 */
public class KeyValuePair {

    private final String key;
    private final Object value;
    private final boolean isEncoded;

    public KeyValuePair(String key, @Nullable Object object) {
        this(key, object, false);
    }

    public KeyValuePair(String key, @Nullable Object value, boolean isEncoded) {
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

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (!(obj instanceof KeyValuePair)) return false;
        KeyValuePair keyValuePair = (KeyValuePair) obj;
        return keyValuePair.getKey().equals(getKey());
    }

    @Override
    public int hashCode() {
        return key.hashCode();
    }
}
