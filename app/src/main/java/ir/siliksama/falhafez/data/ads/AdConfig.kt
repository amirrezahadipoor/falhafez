package ir.siliksama.falhafez.data.ads

/**
 * پیکربندی تپسل.
 *
 * کلید اپلیکیشن در app/build.gradle.kts (manifestPlaceholders) قرار دارد و SDK به‌صورت
 * خودکار آن را می‌خواند (auto-init).
 *
 * جایگاه‌های تبلیغاتی (Zone ID) را از پنل تپسل (app.tapsell.ir → تبلیغ‌گاه‌ها) کپی و
 * اینجا جای‌گذاری کنید. تا وقتی خالی باشند، درخواست تبلیغ با zone پیش‌فرض انجام می‌شود
 * (در صورت پشتیبانی) یا به‌صورت امن نمایش داده نمی‌شود — بدون هیچ خطایی.
 */
object AdConfig {
    const val TAPSELL_APP_KEY = "tcgrrdhdhqmccrmqjeobdfsppktsqfhdqpijdkrfmkstiersqilbhfojrjblshbosqdkrb"

    const val ZONE_BANNER = ""
    const val ZONE_INTERSTITIAL = ""
    const val ZONE_REWARDED = ""
    const val ZONE_NATIVE = ""

    val enabled: Boolean get() = TAPSELL_APP_KEY.isNotBlank()
}
