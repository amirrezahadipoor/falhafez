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

    /**
     * کلیدِ تپ‌سل که در مانیفست نشسته (از `manifestPlaceholders`).
     * اینجا فقط برای **اعتبارسنجی** نگه داشته می‌شود، نه برای دادن به SDK.
     */
    const val TAPSELL_APP_KEY =
        "tcgrrdhdhqmccrmqjeobdfsppktsqfhdqpijdkrfmkstiersqilbhfojrjblshbosqdkrb"

    /**
     * آیا کلیدِ تپ‌سل شکلِ قابلِ قبولی دارد؟
     *
     * ### تاریخچه‌ای که مهم است
     * تپ‌سل **۱.۳.۰** کلید را با دقیقاً دو الگو می‌سنجید — `^[a-fA-F0-9]{24}$`
     * یا یک UUID — و در غیرِ آن یک `TapsellManifestException` پرتاب می‌کرد
     * («Invalid mediation app key provided in application manifest»). راه‌اندازی
     * همان‌جا می‌مرد و `onInitializationComplete` هرگز صدا زده نمی‌شد.
     *
     * کلیدی که پنلِ تپ‌سل امروز می‌دهد ۷۰ نویسه و غیرِهگز است، یعنی با ۱.۳.۰
     * **ذاتاً ناسازگار** بود. خودِ تپ‌سل هم این را پذیرفته: در
     * **۱.۴.۰-alpha03** آن اعتبارسنجی به‌کلی حذف شده — نه regex مانده، نه
     * پیامِ خطا، نه استثنا. (هر سه با استخراج از بایت‌کد تأیید شد.)
     *
     * پس پروژه روی ۱.۴.۰-alpha03 است و تنها شرطِ معقول این است که کلید خالی
     * نباشد؛ اعتبارِ واقعی را سرورِ تپ‌سل تعیین می‌کند، نه یک regex در اپ.
     */
    val tapsellKeyLooksValid: Boolean
        get() = TAPSELL_APP_KEY.isNotBlank()

    /** آیا zoneهای تپ‌سل پر شده‌اند؟ */
    val tapsellEnabled: Boolean
        get() = tapsellKeyLooksValid &&
            ZONE_BANNER.isNotBlank() &&
            ZONE_INTERSTITIAL.isNotBlank() &&
            ZONE_REWARDED.isNotBlank() &&
            ZONE_NATIVE.isNotBlank()

    // ── ادیوری (یکتانت) ──────────────────────────────────────────────────

    /** App Key از پنلِ ادیوری — در زمانِ اجرا به SDK داده می‌شود. */
    const val ADIVERY_APP_KEY = "9e59a668-464f-4836-a0d9-995b19c4cef7"

    /**
     * Placement IDهای ادیوری — از پنلِ ادیوری (panel.adivery.com).
     *
     * اگر روزی یکی از این‌ها خالی شود، همان قالب به‌تنهایی از آبشار کنار می‌رود
     * و بقیه سرِ جای خود کار می‌کنند.
     */
    /** بنر استاندارد */
    const val ADIVERY_BANNER = "661cc465-86ad-4384-8dbf-234ce70d632d"

    /** تمام‌صفحهٔ میانه‌ای (Interstitial) */
    const val ADIVERY_INTERSTITIAL = "11bc0de5-45c8-45e3-b988-5f45c6effff8"

    /** ویدیوی جایزه‌ای (Rewarded) */
    const val ADIVERY_REWARDED = "68edeb4e-0149-4d93-805f-694eef5349b8"

    /** بنر همسان (Native) */
    const val ADIVERY_NATIVE = "15bcc548-0a62-4fda-8856-652b02293ea8"

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
