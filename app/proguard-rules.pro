# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

-keep class com.example.httpsender.entity.**{*;}

# 2.2.7以上版本将无需手动配置混淆规则，内部会自动配置
#
## okhttp 4.7.0及以上版本混淆规则
#-keepclassmembers class okhttp3.internal.Util {
#    public static java.lang.String userAgent;
#}
#
## okhttp 4.7.0以下版本混淆规则
#-keepclassmembers class okhttp3.internal.Version{
#    # 4.0.0<=version<4.7.0
#    public static java.lang.String userAgent;
#    # version<4.0.0
#    public static java.lang.String userAgent();
#}
## okhttp 4.0.0以下版本混淆规则
#-keepclassmembers class okhttp3.internal.http.StatusLine{
#    public static okhttp3.internal.http.StatusLine parse(java.lang.String);
#}
