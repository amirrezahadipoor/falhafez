package ir.siliksama.falhafez.data.ads

/**
 * کلیدهای تپسل — از پنل tapsell.ir می‌گیرید و اینجا می‌گذارید
 * (راهنما: docs/TAPSELL_INTEGRATION.md و docs/TAPSELL_REGISTRATION.md).
 * تا وقتی خالی باشند، لایهٔ تبلیغات خاموش است و هیچ تبلیغی نمایش داده نمی‌شود.
 */
object AdConfig {
    const val TAPSELL_APP_KEY = ""   // ← از پنل تپسل
    const val ZONE_BANNER = ""
    const val ZONE_REWARDED = ""
    const val ZONE_INTERSTITIAL = ""
    const val ZONE_NATIVE = ""

    val enabled: Boolean get() = TAPSELL_APP_KEY.isNotBlank()
}
