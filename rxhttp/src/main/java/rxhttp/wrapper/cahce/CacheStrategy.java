package rxhttp.wrapper.cahce;

/**
 * 缓存策略
 * User: ljx
 * Date: 2019-12-15
 * Time: 20:58
 */
public class CacheStrategy {

    private String mCacheKey; //缓存读写时的key
    private long mCacheValidTime = Long.MAX_VALUE; //缓存有效时间  默认Long.MAX_VALUE，代表永久有效
    private CacheMode mCacheMode; //缓存模式

    public CacheStrategy(CacheStrategy cacheStrategy) {
        this.mCacheKey = cacheStrategy.mCacheKey;
        this.mCacheValidTime = cacheStrategy.mCacheValidTime;
        this.mCacheMode = cacheStrategy.mCacheMode;
    }

    public CacheStrategy(CacheMode cacheMode) {
        mCacheMode = cacheMode;
    }

    public CacheStrategy(CacheMode cacheMode, long cacheValidTime) {
        mCacheMode = cacheMode;
        mCacheValidTime = cacheValidTime;
    }

    public String getCacheKey() {
        return mCacheKey;
    }

    public void setCacheKey(String key) {
        this.mCacheKey = key;
    }

    public long getCacheValidTime() {
        return mCacheValidTime;
    }

    public void setCacheValidTime(long validTime) {
        this.mCacheValidTime = validTime;
    }

    public CacheMode getCacheMode() {
        return mCacheMode;
    }

    public void setCacheMode(CacheMode cacheMode) {
        mCacheMode = cacheMode;
    }
}
