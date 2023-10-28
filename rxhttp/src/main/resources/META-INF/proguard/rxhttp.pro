# okhttp version >= 4.7.0
-keepclassmembers class okhttp3.internal.Util {
    public static java.lang.String userAgent;
}

# okhttp version < 4.7.0
-keepclassmembers class okhttp3.internal.Version {
    # 4.0.0 <= version < 4.7.0
    public static java.lang.String userAgent;
    # version < 4.0.0
    public static java.lang.String userAgent();
}
# okhttp version < 4.0.0
-keepclassmembers class okhttp3.internal.http.StatusLine {
    public static okhttp3.internal.http.StatusLine parse(java.lang.String);
}
-keepclassmembers class okhttp3.internal.cache.DiskLruCache {
    public static okhttp3.internal.cache.DiskLruCache create(
                  okhttp3.internal.io.FileSystem, java.io.File, int, int, long);
}

# 4.0.0 <= okhttp version < 4.3.0
-keepclassmembers class okhttp3.internal.cache.DiskLruCache$Companion {
    public okhttp3.internal.cache.DiskLruCache create(
              okhttp3.internal.io.FileSystem, java.io.File, int, int, long);
}

# With R8 full mode generic signatures are stripped for classes that are not kept.
-keep,allowobfuscation,allowshrinking class * extends rxhttp.wrapper.parse.TypeParser