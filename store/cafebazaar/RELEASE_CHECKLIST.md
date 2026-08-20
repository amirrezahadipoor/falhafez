# چک‌لیست انتشار در کافه‌بازار

## پیش از ثبت نسخهٔ نهایی
- [x] package name یکتا: `com.amirrezahadipoor.falhafez`
- [x] آیکون 512×512: `store/cafebazaar/icon-512.png`
- [x] توضیحات کوتاه/بلند: `store/cafebazaar/`
- [x] سیاست حریم خصوصی (لینک واقعی): https://amirrezahadipoor.github.io/falhafez/privacy.html
- [ ] اسکرین‌شات‌های واقعی (2 تا 8): از ورک‌فلو `Store Screenshots` در گیت‌هاب یا از گوشی خودت
- [ ] نسخهٔ نهایی signed با کلید رسمی خودت (نه کلید CI — کلید را امن نگه دار، برای همیشه)

## نکات کلیدی کافه‌بازار
1. **تبلیغات**: برای ایران، Tapsell ضروری است (AdMob بدون Google Play Services روی بیشتر گوشی‌های ایرانی کار نمی‌کند).
   → `docs/TAPSELL_INTEGRATION.md` را بخوان؛ کلید Tapsell را از پنل tapsell.ir بگیر.
2. **کلید امضا**: یک keystore با keytool بساز و در Secrets گیت‌هاب بگذار:
   `KEYSTORE_PATH`, `KEYSTORE_PASSWORD`, `KEY_ALIAS`, `KEY_PASSWORD`
   (ورک‌فلو اگر این متغیرها باشند، release را با همان کلید امضا می‌کند.)
3. **ورژن**: هر انتشار باید `versionCode` را بالا ببرد (app/build.gradle.kts).
4. **دسته**: ادبیات / سرگرمی. **ردهٔ سنی**: +3.
5. اگر کافه‌بازار درخواست «لینک پشتیبانی» کرد، یک ایمیل یا کانال پشتیبانی بده.

## ساخت APK انتشار
روش ۱ — ورک‌فلو گیت‌هاب: هر push روی main → تب Actions → artifact «falhafez-apks» → `release/app-release.apk`.
روش ۲ — محلی: Android Studio → Build → Generate Signed App Bundle / APK با کلید خودت.
