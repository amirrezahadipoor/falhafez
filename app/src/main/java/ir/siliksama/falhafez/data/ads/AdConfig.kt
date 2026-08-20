package ir.siliksama.falhafez.data.ads

/**
 * پیکربندی تپسل.
 *
 * کلید اپلیکیشن در app/build.gradle.kts (manifestPlaceholders) قرار دارد و SDK به‌صورت
 * خودکار آن را می‌خواند (auto-init). جایگاه‌های تبلیغاتی (Zone ID) از پنل تپسل.
 */
object AdConfig {
    const val TAPSELL_APP_KEY = "tcgrrdhdhqmccrmqjeobdfsppktsqfhdqpijdkrfmkstiersqilbhfojrjblshbosqdkrb"

    // بنر استاندارد
    const val ZONE_BANNER = "6a8738de5c9b7478ba5bf130"

    // بنر آنی (بین‌صفحه‌ای / Interstitial)
    const val ZONE_INTERSTITIAL = "6a8738c45c9b7478ba5bf12f"

    // ویدیوی جایزه‌ای (Rewarded)
    const val ZONE_REWARDED = "6a87391b5c9b7478ba5bf132"

    // بنر همسان (Native)
    const val ZONE_NATIVE = "6a8738a2af056d371d5ba59b"

    // ویدیوی پیش‌نمایش (Preroll) — برای آینده
    const val ZONE_PREROLL = "6a8738fa5c9b7478ba5bf131"

    val enabled: Boolean get() = TAPSELL_APP_KEY.isNotBlank()
}
