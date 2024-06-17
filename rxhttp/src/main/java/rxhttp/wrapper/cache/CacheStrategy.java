package rxhttp.wrapper.cache;

/**
 * 缓存策略
 * User: ljx
 * Date: 2019-12-15
 * Time: 20:58
 */
public class CacheStrategy {

    private String cacheKey; //缓存读写时的key
    private long cacheValidTime = Long.MAX_VALUE; //缓存有效时间  默认Long.MAX_VALUE，代表永久有效
    private CacheMode cacheMode; //缓存模式

    public CacheStrategy(CacheStrategy cacheStrategy) {
        this.cacheKey = cacheStrategy.cacheKey;
        this.cacheMode = cacheStrategy.cacheMode;
        setCacheValidTime(cacheStrategy.cacheValidTime);
    }

    public CacheStrategy(CacheMode cacheMode) {
        this.cacheMode = cacheMode;
    }

    public CacheStrategy(CacheMode cacheMode, long cacheValidTime) {
        this.cacheMode = cacheMode;
        setCacheValidTime(cacheValidTime);
    }

    public String getCacheKey() {
        return cacheKey;
    }

    public void setCacheKey(String key) {
        this.cacheKey = key;
    }

    public long getCacheValidTime() {
        return cacheValidTime;
    }

    public void setCacheValidTime(long validTime) {
        if (validTime <= 0) {
            throw new IllegalArgumentException("validTime > 0 required but it was " + validTime);
        }
        this.cacheValidTime = validTime;
    }

    public CacheMode getCacheMode() {
        return cacheMode;
    }

    public void setCacheMode(CacheMode cacheMode) {
        this.cacheMode = cacheMode;
    }
}
