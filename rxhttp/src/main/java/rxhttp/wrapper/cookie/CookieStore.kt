package rxhttp.wrapper.cookie

import okhttp3.Cookie
import okhttp3.HttpUrl
import okhttp3.internal.cache.DiskLruCache
import okhttp3.internal.io.FileSystem
import okio.Source
import rxhttp.OkHttpCompat
import rxhttp.wrapper.annotations.Nullable
import rxhttp.wrapper.cahce.DiskLruCacheFactory
import java.io.File
import java.io.IOException
import java.util.*
import java.util.concurrent.ConcurrentHashMap

/**
 *
 * User: ljx
 * Date: 2019-12-29
 * Time: 21:51
 *
 * Cookie管理类，支持内存、磁盘同时缓存，默认仅开启内存缓存；若要开启磁盘缓存，构造方法传入磁盘缓存目录即可
 * 注意：内存缓存、磁盘缓存至少要开启一个，否则抛出非法参数异常
 *
 * @param directory     磁盘缓存目录，传入null，则代表不开启磁盘缓存
 * @param maxSize       磁盘缓存最大size，默认为 Integer.MAX_VALUE
 * @param enabledMemory 是否开启内存缓存
 */
class CookieStore @JvmOverloads constructor(
    @Nullable directory: File? = null,
    maxSize: Long = Int.MAX_VALUE.toLong(),
    enabledMemory: Boolean = true
) : ICookieJar {

    init {
        //内存缓存、磁盘缓存都没有开启，则抛出异常
        require(!(!enabledMemory && directory == null)) { "Memory or disk caching must be enabled" }
    }

    //磁盘缓存
    private val mDiskCache: DiskLruCache? by lazy {
        return@lazy if (directory == null) null
        else DiskLruCacheFactory.newDiskLruCache(FileSystem.SYSTEM, directory, appVersion, 1, maxSize)
    }

    //内存缓存
    private val memoryCache: MutableMap<String, MutableList<Cookie>>? by lazy {
        return@lazy if (enabledMemory) ConcurrentHashMap<String, MutableList<Cookie>>()
        else null
    }

    constructor(@Nullable directory: File?, enabledMemory: Boolean) : this(directory, Int.MAX_VALUE.toLong(), enabledMemory)

    /**
     * 保存url对应的cookie，线程安全，若开启了磁盘缓存，建议在子线程调用
     *
     * @param url    HttpUrl
     * @param cookie Cookie
     */
    override fun saveCookie(url: HttpUrl, cookie: Cookie) {
        val cookies = ArrayList<Cookie>()
        cookies.add(cookie)
        saveCookie(url, cookies)
    }

    /**
     * 保存url对应的所有cookie，线程安全，若开启了磁盘缓存，建议在子线程调用
     *
     * @param url     HttpUrl
     * @param cookies List
     */
    override fun saveCookie(url: HttpUrl, cookies: MutableList<Cookie>) {
        val host = OkHttpCompat.host(url)
        memoryCache?.put(host, cookies)
        val diskCache = mDiskCache
        if (diskCache != null) { //开启了磁盘缓存，则将cookie写入磁盘
            var editor: DiskLruCache.Editor? = null
            try {
                editor = diskCache.edit(md5(host))
                if (editor == null) {
                    return
                }
                writeCookie(editor, cookies)
                editor.commit()
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                abortQuietly(editor)
            }
        }
    }

    /**
     * 加载url对应的cookie，线程安全，若开启了磁盘缓存，建议在子线程调用
     *
     * @param url HttpUrl
     * @return List
     */
    override fun loadCookie(url: HttpUrl): List<Cookie> {
        val host = OkHttpCompat.host(url)
        var cookies = memoryCache?.get(host)  //1、开启了内存缓存，则从内存查找cookie
        if (cookies != null) {                                   //2、内存缓存查找成功，直接返回
            return Collections.unmodifiableList(cookies)
        }

        cookies = ArrayList()
        //3、开启了磁盘缓存，则从磁盘查找cookie
        mDiskCache?.apply {
            var snapshot: DiskLruCache.Snapshot? = null
            try {
                //4、磁盘缓存查找
                snapshot = get(md5(host))
                if (snapshot == null) return Collections.unmodifiableList(cookies)
                val cookiesList = readCookie(url, snapshot.getSource(0))
                if (cookiesList.isNotEmpty()) cookies.addAll(cookiesList)
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                OkHttpCompat.closeQuietly(snapshot)
            }
        }
        if (cookies.isNotEmpty()) //5、磁盘缓存查找成功，添加进内存缓存
            memoryCache?.put(host, cookies)
        return Collections.unmodifiableList(cookies)
    }

    /**
     * 移除url对应的cookie，线程安全，若开启了磁盘缓存，建议在子线程调用
     *
     * @param url HttpUrl
     */
    override fun removeCookie(url: HttpUrl) {
        val host = OkHttpCompat.host(url)
        memoryCache?.remove(host)
        mDiskCache?.apply {
            try {
                remove(md5(host))
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    /**
     * 移除所有的cookie，线程安全，若开启了磁盘缓存，建议在子线程调用
     */
    override fun removeAllCookie() {
        memoryCache?.clear()
        mDiskCache?.apply {
            try {
                evictAll()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    //从磁盘都cookie
    @Throws(IOException::class)
    private fun readCookie(url: HttpUrl, source: Source): List<Cookie> {
        val cookies: MutableList<Cookie> = ArrayList()
        source.use {
            val bufferSource = OkHttpCompat.buffer(it)
            val size = bufferSource.readInt()
            for (i in 0 until size) {
                val name = bufferSource.readUtf8LineStrict()
                Cookie.parse(url, name)?.apply {
                    cookies.add(this)
                }
            }
        }
        return cookies
    }

    //cookie写入磁盘
    @Throws(IOException::class)
    private fun writeCookie(editor: DiskLruCache.Editor, cookies: List<Cookie>) {
        val sink = OkHttpCompat.buffer(editor.newSink(0))
        sink.writeInt(cookies.size)
        for (cookie in cookies) {
            sink.writeUtf8(cookie.toString()).writeByte('\n'.toInt())
        }
        sink.close()
    }

    private fun abortQuietly(@Nullable editor: DiskLruCache.Editor?) {
        try {
            editor?.abort()
        } catch (ignored: Exception) {
        }
    }

    companion object {
        private const val appVersion = 1
        private fun md5(key: String): String {
            return OkHttpCompat.encodeUtf8(key).md5().hex()
        }
    }
}