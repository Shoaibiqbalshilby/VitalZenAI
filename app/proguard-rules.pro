# Hilt
-keep class dagger.hilt.android.internal.managers.ViewComponentManager$ObjerctContext { *; }

# Room
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.paging.**

# Retrofit
-keepattributes Signature, InnerClasses, EnclosingMethod
-keepattributes RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations
-keepattributes AnnotationDefault
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }
-keepclasseswithmembers class * {
    @retrofit2.http.* <methods>;
}

# Gson
-keepattributes Signature
-keepattributes *Annotation*
-dontwarn sun.misc.**
-keep class com.google.gson.examples.android.model.** { *; }
-keep class com.google.gson.** { *; }

# MPAndroidChart
-keep class com.github.philjay.charting.** { *; }
-dontwarn com.github.philjay.charting.**

# Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepnames class kotlinx.coroutines.android.AndroidExceptionPreHandler {}
-keepnames class kotlinx.coroutines.android.AndroidDispatcherFactory {}
-dontwarn kotlinx.coroutines.**

# Firebase
-keep class com.google.firebase.** { *; }
-dontwarn com.google.firebase.**

# Android 14 media APIs referenced transitively by ads SDK in release minification
-dontwarn android.media.LoudnessCodecController
-dontwarn android.media.LoudnessCodecController$OnLoudnessCodecUpdateListener
