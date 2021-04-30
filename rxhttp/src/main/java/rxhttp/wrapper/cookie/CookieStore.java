package rxhttp.wrapper.cookie;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import okhttp3.Cookie;
import okhttp3.HttpUrl;
import okhttp3.internal.cache.DiskLruCache;
import okhttp3.internal.io.FileSystem;
import okio.BufferedSink;
import okio.BufferedSource;
import okio.ByteString;
import okio.Okio;
import okio.Source;
import rxhttp.wrapper.OkHttpCompat;
import rxhttp.wrapper.annotations.Nullable;

/**
 * Cookie管理类，支持内存、磁盘同时缓存，默认仅开启内存缓存；若要开启磁盘缓存，构造方法传入磁盘缓存目录即可
 * <p>
 * 注：内存、磁盘缓存至少要开启一个，否则构造方法抛出非法参数异常
 * User: ljx
 * Date: 2019-12-29
 * Time: 21:51
 */
public class CookieStore implements ICookieJar {

    private static final int appVersion = 1;

    private final File directory;
    private final long maxSize;
    private DiskLruCache diskCache; //磁盘缓存
    private Map<String, ConcurrentHashMap<String, Cookie>> memoryCache; //内存缓存

    public CookieStore() {
        this(null, Integer.MAX_VALUE, true);
    }

    public CookieStore(@Nullable File directory) {
        this(directory, Integer.MAX_VALUE, true);
    }

    public CookieStore(@Nullable File directory, boolean enabledMemory) {
        this(directory, Integer.MAX_VALUE, enabledMemory);
    }

    /**
     * 配置cookie 存储策略，注意：内存缓存、磁盘缓存至少要开启一个，否则抛出非法参数异常
     *
     * @param directory     磁盘缓存目录，传入null，则代表不开启磁盘缓存
     * @param maxSize       磁盘缓存最大size，默认为 Integer.MAX_VALUE
     * @param enabledMemory 是否开启内存缓存
     */
    public CookieStore(@Nullable File directory, long maxSize, boolean enabledMemory) {
        if (!enabledMemory && directory == null)
            throw new IllegalArgumentException("Memory or disk caching must be enabled");
        if (enabledMemory) {
            memoryCache = new ConcurrentHashMap<>();
        }
        this.directory = directory;
        this.maxSize = maxSize;

    }

    private DiskLruCache getDiskLruCache() {
        if (directory != null && diskCache == null) {
            diskCache = OkHttpCompat.newDiskLruCache(FileSystem.SYSTEM, directory, appVersion, 1, maxSize);
        }
        return diskCache;
    }

    /**
     * 保存url对应的cookie，线程安全，若开启了磁盘缓存，建议在子线程调用
     *
     * @param url    HttpUrl
     * @param cookie Cookie
     */
    @Override
    public void saveCookie(HttpUrl url, Cookie cookie) {
        List<Cookie> cookies = new ArrayList<>();
        cookies.add(cookie);
        saveCookie(url, cookies);
    }

    /**
     * 保存url对应的所有cookie，线程安全，若开启了磁盘缓存，建议在子线程调用
     *
     * @param url     HttpUrl
     * @param cookies List
     */
    @Override
    public void saveCookie(HttpUrl url, List<Cookie> cookies) {
        final String host = url.host();
        ConcurrentHashMap<String, Cookie> cookieMap;
        if (memoryCache != null) { //开启了内存缓存，则将cookie写入内存
            cookieMap = memoryCache.get(host);
            if (cookieMap == null) {
                memoryCache.put(host, cookieMap = new ConcurrentHashMap<>());
            }
        } else {
            cookieMap = new ConcurrentHashMap<>();
        }
        for (Cookie cookie : cookies) {
            cookieMap.put(getToken(cookie), cookie);
        }

        DiskLruCache diskCache = getDiskLruCache();
        if (diskCache != null) { //开启了磁盘缓存，则将cookie写入磁盘
            DiskLruCache.Editor editor = null;
            try {
                editor = diskCache.edit(md5(host));
                if (editor == null) {
                    return;
                }
                writeCookie(editor, cookieMap);
                editor.commit();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                abortQuietly(editor);
            }
        }
    }

    /**
     * 加载url对应的cookie，线程安全，若开启了磁盘缓存，建议在子线程调用
     *
     * @param url HttpUrl
     * @return List
     */
    @Override
    public List<Cookie> loadCookie(HttpUrl url) {
        final String host = url.host();
        if (memoryCache != null) {    //1、开启了内存缓存，则从内存查找cookie
            Map<String, Cookie> cookieMap = memoryCache.get(host);
            if (cookieMap != null) {  //2、内存缓存查找成功，直接返回
                return matchCookies(url, cookieMap);
            }
        }
        ConcurrentHashMap<String, Cookie> cookieMap = new ConcurrentHashMap<>();
        DiskLruCache diskCache = getDiskLruCache();
        if (diskCache != null) { //3、开启了磁盘缓存，则从磁盘查找cookie
            DiskLruCache.Snapshot snapshot = null;
            try {
                //4、磁盘缓存查找
                snapshot = diskCache.get(md5(host));
                if (snapshot == null) return Collections.emptyList();
                List<Cookie> cookiesList = readCookie(url, snapshot.getSource(0));
                for (Cookie cookie : cookiesList) {
                    cookieMap.put(getToken(cookie), cookie);
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                OkHttpCompat.closeQuietly(snapshot);
            }
        }
        if (memoryCache != null && !cookieMap.isEmpty()) //5、磁盘缓存查找成功，添加进内存缓存
            memoryCache.put(host, cookieMap);
        return matchCookies(url, cookieMap);
    }

    private List<Cookie> matchCookies(HttpUrl url, Map<String, Cookie> cookieMap) {
        List<Cookie> matchCookies = new ArrayList<>();
        for (Cookie cookie : cookieMap.values()) {
            //cookie匹配且未过期
            if (cookie.matches(url) && cookie.expiresAt() > System.currentTimeMillis()) {
                matchCookies.add(cookie);
            }
        }
        return Collections.unmodifiableList(matchCookies);
    }


    private String getToken(Cookie cookie) {
        return cookie.name() + "; " + cookie.domain() + "; " + cookie.path() + "; " + cookie.secure();
    }

    /**
     * 移除url对应的cookie，线程安全，若开启了磁盘缓存，建议在子线程调用
     *
     * @param url HttpUrl
     */
    @Override
    public void removeCookie(HttpUrl url) {
        String host = url.host();
        if (memoryCache != null) {
            memoryCache.remove(host);
        }
        DiskLruCache diskCache = getDiskLruCache();
        if (diskCache != null) {
            try {
                diskCache.remove(md5(host));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 移除所有的cookie，线程安全，若开启了磁盘缓存，建议在子线程调用
     */
    @Override
    public void removeAllCookie() {
        if (memoryCache != null)
            memoryCache.clear();
        DiskLruCache diskCache = getDiskLruCache();
        if (diskCache != null) {
            try {
                diskCache.evictAll();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    //从磁盘都cookie
    private List<Cookie> readCookie(HttpUrl url, Source in) throws IOException {
        List<Cookie> cookies = new ArrayList<>();
        try {
            BufferedSource source = Okio.buffer(in);
            int size = source.readInt();
            for (int i = 0; i < size; i++) {
                String name = source.readUtf8LineStrict();
                cookies.add(Cookie.parse(url, name));
            }
        } finally {
            in.close();
        }
        return cookies;
    }

    //cookie写入磁盘
    private void writeCookie(DiskLruCache.Editor editor, Map<String, Cookie> cookieMap) throws
        IOException {
        BufferedSink sink = Okio.buffer(editor.newSink(0));
        sink.writeInt(cookieMap.size());
        for (Cookie cookie : cookieMap.values()) {
            sink.writeUtf8(cookie.toString()).writeByte('\n');
        }
        sink.close();
    }

    private void abortQuietly(@Nullable DiskLruCache.Editor editor) {
        try {
            if (editor != null) {
                editor.abort();
            }
        } catch (Exception ignored) {
        }
    }

    private static String md5(String key) {
        return ByteString.encodeUtf8(key).md5().hex();
    }
}
