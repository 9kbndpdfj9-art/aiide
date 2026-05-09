-keep class com.aiide.** { *; }
-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}
