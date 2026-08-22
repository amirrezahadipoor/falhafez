package ir.siliksama.falhafez.data.ads

import android.app.Application
import android.util.Log
import com.adivery.sdk.Adivery

private const val TAG = "FalHafezAds"

/**
 * راه‌اندازیِ SDK ادیوری.
 *
 * برخلافِ تپ‌سل که کلیدش را از مانیفست می‌خواند و خودکار بالا می‌آید، ادیوری
 * `configure(Application, appKey)` می‌خواهد. چون کلید در زمانِ اجرا داده می‌شود،
 * `configure` **همگام** است و بلافاصله پس از آن می‌شود درخواست فرستاد — پس اینجا
 * نه به صفِ callback نیاز داریم و نه به مهلتِ ایمنی (برخلافِ [TapsellInit]).
 */
object AdiveryInit {

    @Volatile
    private var configured = false

    /** آیا ادیوری راه‌اندازی شده و آمادهٔ دریافتِ درخواست است؟ */
    val isReady: Boolean get() = configured

    /**
     * یک‌بار در `Application.onCreate` صدا زده می‌شود.
     * اگر placementها خالی باشند اصلاً SDK را راه نمی‌اندازیم تا هیچ
     * درخواستِ شبکه‌ای بی‌مورد فرستاده نشود.
     */
    @Synchronized
    fun install(app: Application) {
        if (configured) return
        if (!AdConfig.adiveryEnabled) {
            Log.d(TAG, "adivery: disabled (no placement ids configured) — skipping configure")
            return
        }
        runCatching {
            Adivery.configure(app, AdConfig.ADIVERY_APP_KEY)
            configured = true
            Log.d(TAG, "adivery: configured ✓")
        }.onFailure {
            Log.w(TAG, "adivery: configure failed", it)
        }
    }

    /** آیا برای این placement تبلیغی در حافظه آماده است؟ */
    fun isLoaded(placement: String): Boolean =
        configured && placement.isNotBlank() &&
            runCatching { Adivery.isLoaded(placement) }.getOrDefault(false)
}
