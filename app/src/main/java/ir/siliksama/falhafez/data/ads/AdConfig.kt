package ir.siliksama.falhafez.data.ads

/**
 * پیکربندی تپسل.
 *
 * کلیدِ اپلیکیشن **فقط** در `app/build.gradle.kts` (manifestPlaceholders) تعریف می‌شود و
 * SDK آن را از مانیفست می‌خواند (auto-init). اینجا کلید تکرار نمی‌شود تا «تنها یک منبعِ
 * حقیقت» داشته باشیم.
 *
 * جایگاه‌های تبلیغاتی (Zone ID) از پنل تپسل: app.tapsell.ir
 */
object AdConfig {

    // بنر استاندارد
    const val ZONE_BANNER = "6a8738de5c9b7478ba5bf130"

    // بنر آنی (بین‌صفحه‌ای / Interstitial)
    const val ZONE_INTERSTITIAL = "6a8738c45c9b7478ba5bf12f"

    // ویدیوی جایزه‌ای (Rewarded)
    const val ZONE_REWARDED = "6a87391b5c9b7478ba5bf132"

    // بنر همسان (Native)
    const val ZONE_NATIVE = "6a8738a2af056d371d5ba59b"

    /**
     * تبلیغات وقتی فعال است که همهٔ zoneها پر باشند.
     * (کلید در مانیفست است؛ اگر کلید نامعتبر باشد SDK خودش لاگ می‌دهد.)
     */
    val enabled: Boolean
        get() = ZONE_BANNER.isNotBlank() &&
            ZONE_INTERSTITIAL.isNotBlank() &&
            ZONE_REWARDED.isNotBlank() &&
            ZONE_NATIVE.isNotBlank()
}
