# اتصال تپسل — انجام‌شده با Tapsell Mediation SDK

## وضعیت فعلی (فعال)
- **SDK**: `ir.tapsell:tapsell:1.3.0` + `ir.tapsell.mediation.adapter:legacy:1.3.0` (نسخهٔ پایدار از Maven Central — بدون مخزن خصوصی).
- **کلید اپ**: `TapsellMediationAppKey` در `app/build.gradle.kts` → `addManifestPlaceholders(...)`؛
  SDK به‌صورت خودکار (ContentProvider) با همین کلید راه‌اندازی می‌شود.
- **کلید شما**: `tcgrrdhdhqmccrmqjeobdfsppktsqfhdqpijdkrfmkstiersqilbhfojrjblshbosqdkrb`

## جایگاه‌های تبلیغاتی (Zone ID)
در `data/ads/AdConfig.kt` چهار ثابت هست که باید از پنل تپسل (app.tapsell.ir → تبلیغ‌گاه‌ها) پر شوند:

```kotlin
const val ZONE_BANNER = ""        // جایگاه بنر
const val ZONE_INTERSTITIAL = ""  // جایگاه آنی (بین‌صفحه‌ای)
const val ZONE_REWARDED = ""      // جایگاه ویدیوی پاداشی
const val ZONE_NATIVE = ""        // جایگاه همسان (نیتیو)
```

- تا وقتی خالی باشند، درخواست با **zone پیش‌فرض** انجام می‌شود (در صورت پشتیبانی) یا بدون خطا نمایش داده نمی‌شود.
- برای بیشترین درآمد، هر چهار zone را از اسکرین‌شات پنل خودت کپی کن و در `AdConfig` بگذار.

## معماری (تغییرناپذیر)
- `AdManager` (واسط) → `TapsellAdManager` (پیاده‌سازی واقعی) → بایندینگ در `di/AdModule.kt`.
- `BannerAdView` و `NativeAdCard` (Compose) بنر و تبلیغ همسان را لود می‌کنند.
- بین‌صفحه‌ای: هر ۴ فال یک‌بار، فقط هنگام بازگشت از نتیجه (هرگز حین آیین فال).
- ویدئوی پاداشی: فال اضافه، پرش ضرب‌آهنگ، بازکردن قفل قالب یلدا.
- **حمایت مالی** (هر سه سطح) تبلیغات را برای همیشه خاموش می‌کند.

## نکتهٔ مهم بازار ایران
- تپسل مستقل از Google Play است — برای **کافه‌بازار** و حتی APK مستقیم کار می‌کند؛ نیازی به Play Store نیست.
- AD_ID (شناسهٔ تبلیغاتی) در مانیفست اضافه شده تا درآمد بهینه باشد.
