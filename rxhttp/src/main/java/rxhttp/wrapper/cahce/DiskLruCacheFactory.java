package rxhttp.wrapper.cahce;

import java.io.File;

import okhttp3.internal.cache.DiskLruCache;
import okhttp3.internal.io.FileSystem;

/**
 * User: ljx
 * Date: 2020/5/16
 * Time: 23:57
 */
public class DiskLruCacheFactory {

    public static IDiskLruCacheFactory factory;

    public static DiskLruCache newDiskLruCache(FileSystem fileSystem, File directory, int appVersion, int valueCount, long maxSize) {
        return factory.newDiskLruCache(fileSystem, directory, appVersion, valueCount, maxSize);
    }


    public interface IDiskLruCacheFactory {

        /**
         * create {@link DiskLruCache} object
         *
         * @param fileSystem FileSystem
         * @param directory  cache directory
         * @param maxSize    cache maxSize
         * @param appVersion cache version
         * @param valueCount The number of individual keys relative to value
         * @return DiskLruCache
         */
        DiskLruCache newDiskLruCache(FileSystem fileSystem, File directory, int appVersion, int valueCount, long maxSize);
    }
}
