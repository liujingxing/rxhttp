# okhttp 4.7.0及以上版本混淆规则
-keepclassmembers class okhttp3.internal.Util {
    public static java.lang.String userAgent;
}

# okhttp 4.7.0以下版本混淆规则
-keepclassmembers class okhttp3.internal.Version{
    # 4.0.0<=version<4.7.0
    public static java.lang.String userAgent;
    # version<4.0.0
    public static java.lang.String userAgent();
}
# okhttp 4.0.0以下版本混淆规则
-keepclassmembers class okhttp3.internal.http.StatusLine{
    public static okhttp3.internal.http.StatusLine parse(java.lang.String);
}
