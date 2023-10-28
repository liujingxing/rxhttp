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

-keepclassmembers class com.example.httpsender.entity.** {
    <init>();  #R8 full mode下, 默认构造方法不保留
    !transient <fields>;
}

# With R8 full mode generic signatures are stripped for classes that are not kept.
-keep,allowobfuscation,allowshrinking class com.example.httpsender.entity.Response
-keep,allowobfuscation,allowshrinking class com.example.httpsender.entity.PageList


#依赖simple-xml后打包失败，需加入以下规则
-dontwarn android.content.res.**

#依赖fastjson后打包失败，需加入以下规则
-dontwarn javax.ws.rs.**
-dontwarn org.glassfish.jersey.internal.spi.**
