# kotlinx.serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.**
-keepclassmembers class ir.siliksama.falhafez.** {
    *** Companion;
}
-keepclasseswithmembers class ir.siliksama.falhafez.** {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep,includedescriptorclasses class ir.siliksama.falhafez.**$$serializer { *; }

# Room
-keep class * extends androidx.room.RoomDatabase { *; }
-keep @androidx.room.Entity class *
-dontwarn androidx.room.paging.**

# Tapsell Mediation SDK (rules included automatically, kept as a safety net)
-keep class ir.tapsell.** { *; }
-dontwarn ir.tapsell.**
