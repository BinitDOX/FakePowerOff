######################################
### 📦 WorkManager
######################################
-keep class androidx.work.** { *; }
-keep class androidx.hilt.work.** { *; }

######################################
### 🛠️ Hilt Dependency Injection
######################################
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-keep @dagger.hilt.** class * { *; }

######################################
### 🪵 Timber Logging
######################################
-keep class timber.log.Timber { *; }
-keep class com.dox.fpoweroff.logging.FileLoggingTree { *; }

-keep class androidx.datastore.** { *; }

-keep class com.google.firebase.** { *; }

-keep class androidx.lifecycle.compose.** { *; }
-keepclassmembers class androidx.lifecycle.compose.** { *; }

-keep class androidx.lifecycle.LifecycleOwner
-keep class androidx.lifecycle.ViewTreeLifecycleOwner

-dontwarn timber.log.Timber

-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembers class kotlinx.coroutines.android.AndroidExceptionPreHandler {
    <init>();
}

-if public class androidx.compose.ui.platform.AndroidCompositionLocals_androidKt {
    public static *** getLocalLifecycleOwner();
}
-keep public class androidx.compose.ui.platform.AndroidCompositionLocals_androidKt {
    public static *** getLocalLifecycleOwner();
}


-keep public class * extends android.app.Activity
-keep public class * extends android.app.Service
-keep public class * extends android.app.Application
