# ==================== AutoVolume ProGuard Rules ====================

# ==================== 通用规则 ====================
-keepattributes *Annotation*
-keepattributes SourceFile,LineNumberTable
-keepattributes Signature
-keepattributes Exceptions

# ==================== 数据模型（DataStore 序列化需要）====================
-keep class com.autovolume.model.** { *; }
-keep class com.autovolume.datastore.** { *; }

# ==================== Kotlin ====================
-keep class kotlin.Metadata { *; }
-dontwarn kotlin.**
-keepclassmembers class **$WhenMappings {
    <fields>;
}
-keepclassmembers class kotlin.Metadata {
    public <methods>;
}

# ==================== Kotlin Coroutines ====================
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembers class kotlinx.coroutines.** {
    volatile <fields>;
}

# ==================== Jetpack Compose ====================
-dontwarn androidx.compose.**
-keep class androidx.compose.** { *; }

# ==================== DataStore ====================
-keep class androidx.datastore.** { *; }
-keepclassmembers class * extends com.google.protobuf.GeneratedMessageLite {
    <fields>;
}

# ==================== 前台服务 ====================
-keep class com.autovolume.service.AutoVolumeService { *; }
-keep class com.autovolume.receiver.BootReceiver { *; }

# ==================== AudioManager / AudioRecord ====================
-keep class android.media.** { *; }

# ==================== 枚举 ====================
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# ==================== Parcelable ====================
-keepclassmembers class * implements android.os.Parcelable {
    public static final ** CREATOR;
}

# ==================== Material Icons Extended ====================
# 只保留实际使用的图标类
-keep class androidx.compose.material.icons.Icons$Filled { *; }
-keep class androidx.compose.material.icons.Icons$Outlined { *; }
-keep class androidx.compose.material.icons.Icons$Rounded { *; }
-keep class androidx.compose.material.icons.Icons$Sharp { *; }
-keep class androidx.compose.material.icons.Icons$TwoTone { *; }

# 移除未使用的图标
-keepclassmembers class androidx.compose.material.icons.** {
    public static *** get*(...);
}

# ==================== R8 兼容性 ====================
# 保留 BuildConfig
-keep class com.autovolume.BuildConfig { *; }

# 避免混淆 Service 和 Receiver 的类名（AndroidManifest 引用）
-keepnames class com.autovolume.service.AutoVolumeService
-keepnames class com.autovolume.receiver.BootReceiver
-keepnames class com.autovolume.AutoVolumeApp
-keepnames class com.autovolume.MainActivity
