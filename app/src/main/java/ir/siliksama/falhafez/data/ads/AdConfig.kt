package ir.siliksama.falhafez.data.ads

/**
 * پیکربندیِ دو شبکهٔ تبلیغاتی: **تپ‌سل** و **ادیوری** (سرویسِ نمایشِ یکتانت).
 *
 * هر دو هم‌زمان فعال‌اند و به‌صورتِ آبشاری (waterfall) کار می‌کنند:
 * اول شبکهٔ اول درخواست می‌شود؛ اگر موجودی نداشت، بی‌درنگ سراغِ دومی می‌رویم.
 * منطقِ آبشار در [WaterfallAdManager] است.
 *
 * ### تفاوتِ بنیادینِ دو SDK در تحویلِ کلید
 * - **تپ‌سل**: کلید فقط از متادیتای مانیفست خوانده می‌شود
 *   (`TapsellMediationAppKey` در `app/build.gradle.kts`) و یک `ContentProvider`
 *   پیش از `Application.onCreate` خودش راه می‌افتد. در زمانِ اجرا تغییرپذیر نیست.
 * - **ادیوری**: کلید در زمانِ اجرا داده می‌شود — `Adivery.configure(app, key)`.
 *   به همین دلیل کلیدش اینجا به‌عنوان ثابت نگه داشته می‌شود.
 */
object AdConfig {

    // ── تپ‌سل ────────────────────────────────────────────────────────────
    // کلید در مانیفست است (تنها منبعِ حقیقت)؛ اینجا تکرار نمی‌شود.

    /** بنر استاندارد */
    const val ZONE_BANNER = "6a8738de5c9b7478ba5bf130"

    /** بنر آنی (بین‌صفحه‌ای / Interstitial) */
    const val ZONE_INTERSTITIAL = "6a8738c45c9b7478ba5bf12f"

    /** ویدیوی جایزه‌ای (Rewarded) */
    const val ZONE_REWARDED = "6a87391b5c9b7478ba5bf132"

    /** بنر همسان (Native) */
    const val ZONE_NATIVE = "6a8738a2af056d371d5ba59b"

    /** آیا zoneهای تپ‌سل پر شده‌اند؟ */
    val tapsellEnabled: Boolean
        get() = ZONE_BANNER.isNotBlank() &&
            ZONE_INTERSTITIAL.isNotBlank() &&
            ZONE_REWARDED.isNotBlank() &&
            ZONE_NATIVE.isNotBlank()

    // ── ادیوری (یکتانت) ──────────────────────────────────────────────────

    /** App Key از پنلِ ادیوری — در زمانِ اجرا به SDK داده می‌شود. */
    const val ADIVERY_APP_KEY = "9e59a668-464f-4836-a0d9-995b19c4cef7"

    /**
     * Placement IDهای ادیوری.
     *
     * ⚠️ این‌ها باید از پنلِ ادیوری (panel.adivery.com) برداشته و اینجا گذاشته شوند.
     * تا وقتی خالی‌اند، ادیوری به‌طورِ خودکار از آبشار کنار می‌ماند و همه‌چیز
     * روی تپ‌سل کار می‌کند — یعنی خالی‌بودنشان چیزی را نمی‌شکند.
     */
    const val ADIVERY_BANNER = ""
    const val ADIVERY_INTERSTITIAL = ""
    const val ADIVERY_REWARDED = ""
    const val ADIVERY_NATIVE = ""

    /**
     * ادیوری وقتی فعال است که کلید **و** دستِ‌کم یک placement پر باشد.
     * (کلید به‌تنهایی کافی نیست؛ بدونِ placement هیچ درخواستی معنا ندارد.)
     */
    val adiveryEnabled: Boolean
        get() = ADIVERY_APP_KEY.isNotBlank() && (
            ADIVERY_BANNER.isNotBlank() ||
                ADIVERY_INTERSTITIAL.isNotBlank() ||
                ADIVERY_REWARDED.isNotBlank() ||
                ADIVERY_NATIVE.isNotBlank()
            )

    /** تبلیغات وقتی فعال است که دستِ‌کم یکی از دو شبکه آماده باشد. */
    val enabled: Boolean
        get() = tapsellEnabled || adiveryEnabled
}
