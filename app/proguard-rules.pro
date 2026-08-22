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

# Poolakey (پرداخت درون‌برنامه‌ای کافه‌بازار)
-keep class ir.cafebazaar.poolakey.** { *; }
-dontwarn ir.cafebazaar.poolakey.**

# ─────────────────────────────────────────────────────────────────────────
# ادیوری (سرویسِ نمایشِ یکتانت)
#
# SDK ادیوری آداپترهای چند شبکهٔ خارجی را در خود دارد (AdMob، Chartboost،
# IronSource، MBridge، StartApp، UnityAds). ما هیچ‌کدام از آن SDKها را وارد
# نکرده‌ایم، پس R8 ارجاع‌هایشان را «کلاسِ گم‌شده» می‌بیند و build را می‌شکند.
#
# این ارجاع‌ها در زمانِ اجرا هرگز اجرا نمی‌شوند: ادیوری پیش از استفاده وجودِ هر
# آداپتر را بررسی می‌کند. پس نادیده‌گرفتنشان امن است و حجمِ APK را هم پایین
# نگه می‌دارد (به‌جای واردکردنِ شش SDK بی‌استفاده).
-keep class com.adivery.sdk.** { *; }
-keep class com.adivery.data.location.** { public *; }
-keep class com.adivery.sdk.core.UninstallReceiver { public *; }
-keepattributes JavascriptInterface
-keep class android.webkit.JavascriptInterface { *; }

-dontwarn com.google.android.gms.ads.**
-dontwarn com.chartboost.sdk.**
-dontwarn com.ironsource.**
-dontwarn com.mbridge.msdk.**
-dontwarn com.startapp.**
-dontwarn com.unity3d.ads.**
-dontwarn com.unity3d.services.**
