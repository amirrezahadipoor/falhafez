package ir.siliksama.falhafez.data.updates

import android.content.Context
import android.content.pm.PackageManager
import ir.siliksama.falhafez.BuildConfig
import ir.siliksama.falhafez.domain.model.UpdateCheckResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * بررسی وجودِ نسخهٔ جدید.
 *
 * ## چرا پیاده‌سازیِ قبلی حذف شد
 * نسخهٔ پیشین به `api.cafebazaar.ir/rest-v1/process/AppDownloadInfoRequest` درخواست
 * POST می‌فرستاد و **خودش را جای اپلیکیشنِ بازار جا می‌زد** — بدنهٔ درخواست
 * `clientVersionCode: 1100301` و `clientVersion: "11.3.1"` داشت، یعنی وانمود می‌کرد
 * نسخهٔ ۱۱.۳.۱ خودِ بازار است. سه مشکل داشت:
 *
 *  ۱. **این API عمومی و مستندشده نیست.** یک endpoint داخلی است؛ هر تغییری در آن
 *     بی‌خبر قابلیت را می‌شکند و ما هیچ راهی برای فهمیدنش نداریم.
 *  ۲. **جعلِ کلاینت نقضِ شرایطِ استفاده است** و می‌تواند به مسدودشدنِ توسعه‌دهنده
 *     منجر شود — ریسکی نامتناسب با «نمایشِ یک اعلانِ بروزرسانی».
 *  ۳. `clientVersion` هاردکد شده بود و با گذشتِ زمان کهنه‌تر می‌شد.
 *
 * ## راهکارِ جایگزین
 * خودِ اپلیکیشنِ بازار روی دستگاه، بروزرسانی‌ها را مدیریت می‌کند — این کارِ اوست،
 * نه کارِ ما. بنابراین:
 *
 *  • اگر بازار نصب **نیست** → [UpdateCheckResult.Failed]. کاربر یا از جای دیگری
 *    نصب کرده یا بازار را پاک کرده؛ در هر دو حالت اعلانِ «بروزرسانی از بازار»
 *    بی‌معنی است.
 *  • اگر نصب **هست** → [UpdateCheckResult.UpToDate]. یعنی «چیزی برای نشان‌دادن
 *    نداریم»، و کاربر از دکمهٔ تنظیمات می‌تواند صفحهٔ اپ را در بازار باز کند و
 *    نسخهٔ واقعی را ببیند.
 *
 * نتیجه: هیچ اعلانِ **دروغینی** نمایش داده نمی‌شود. رفتار بی‌سر‌و‌صدا و امن است و
 * دیگر هیچ درخواستِ شبکه‌ای هم زده نمی‌شود — یعنی برای کاربرِ آفلاین هم صفر هزینه.
 *
 * اگر بعداً بخواهیم اعلانِ واقعی داشته باشیم، راهِ درست انتشارِ یک فایلِ JSON کوچک
 * روی دامنهٔ خودمان است (مثلاً `versionCode` آخرین نسخه) و خواندنِ همان — نه جعلِ
 * کلاینتِ فروشگاه.
 */
object UpdateChecker {

    /** نامِ بستهٔ اپلیکیشنِ کافه‌بازار. */
    const val BAZAAR_PACKAGE = "com.farsitel.bazaar"

    /**
     * بدونِ [context] نمی‌شود چیزی فهمید، پس امن‌ترین پاسخ داده می‌شود.
     * (امضای بدونِ ورودی برای سازگاری با فراخوان‌های قدیمی نگه داشته شده.)
     */
    suspend fun check(): UpdateCheckResult = UpdateCheckResult.UpToDate

    suspend fun check(context: Context): UpdateCheckResult = withContext(Dispatchers.IO) {
        if (!isBazaarInstalled(context)) UpdateCheckResult.Failed else UpdateCheckResult.UpToDate
    }

    /** آیا اپلیکیشنِ بازار روی دستگاه هست؟ (نیازمندِ ورودیِ `<queries>` در مانیفست) */
    fun isBazaarInstalled(context: Context): Boolean = runCatching {
        context.packageManager.getPackageInfo(BAZAAR_PACKAGE, 0)
        true
    }.getOrElse { e ->
        if (e is PackageManager.NameNotFoundException) false else false
    }

    /** نسخهٔ نصب‌شدهٔ فعلی — برای نمایش در تنظیمات. */
    fun currentVersion(): String = "${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})"
}
