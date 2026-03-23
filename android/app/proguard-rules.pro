# BakeCost Pro ProGuard rules

# Keep WebView JS interface methods
-keepclassmembers class com.bakecostpro.app.AndroidBridge {
    @android.webkit.JavascriptInterface <methods>;
}

# Keep AppCompat
-keep class androidx.appcompat.** { *; }
-keep class androidx.webkit.** { *; }

# Kotlin
-keep class kotlin.** { *; }
-keepclassmembers class **$WhenMappings { *; }
