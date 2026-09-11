# Configuración avanzada de ofuscación y optimización R8 / ProGuard

# Ofuscar nombres de clases y métodos agresivamente
-repackageclasses ''
-allowaccessmodification

# Ocultar nombres de archivos fuente y mantener información de depuración estructurada
-keepattributes SourceFile,LineNumberTable,*Annotation*,Signature,InnerClasses,EnclosingMethod
-renamesourcefileattribute SourceFile

# Room Database
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.paging.**
-keep class com.example.data.model.** { *; }

# Jetpack Compose
-dontwarn androidx.compose.**

# Coroutines & Kotlin
-dontwarn kotlinx.coroutines.**

# Retrofit / OkHttp / Moshi
-dontwarn retrofit2.**
-dontwarn okhttp3.**
-dontwarn com.squareup.moshi.**

# Firebase
-dontwarn com.google.firebase.**
