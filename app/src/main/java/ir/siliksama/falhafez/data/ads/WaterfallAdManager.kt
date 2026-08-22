package ir.siliksama.falhafez.data.ads

import android.app.Activity
import android.util.Log
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

private const val TAG = "FalHafezAds"

/**
 * آبشارِ دو شبکهٔ تبلیغاتی: **تپ‌سل** و **ادیوری** (سرویسِ نمایشِ یکتانت).
 *
 * این همان چیزی است که خواسته شده بود: «هرکدام موجودی داشت، همان نمایش داده شود».
 *
 * ### چرا آبشار، نه «دو کلید در یک SDK»
 * تپ‌سل کلیدش را فقط از مانیفست می‌خواند و در زمانِ اجرا عوض نمی‌شود، پس دو کلید
 * در یک SDK ممکن نیست. اما دو **SDK مستقل** کنارِ هم کاملاً ممکن است — و در عمل
 * نتیجهٔ بهتری هم می‌دهد، چون دو موجودیِ جدا داریم نه دو حسابِ یک شبکه.
 *
 * ### ترتیب
 * تپ‌سل اول امتحان می‌شود چون zoneهایش پیکربندی‌شده و آزموده‌اند؛ اگر تبلیغی
 * نداشت، بی‌درنگ ادیوری. اگر هیچ‌کدام ندادند `false` برمی‌گردد و طبقِ سیاستِ اپ
 * **فال به‌هرحال باز می‌شود** — کاربر هرگز پشتِ تبلیغ گیر نمی‌کند.
 *
 * هر دو شبکه هم‌زمان preload می‌شوند تا وقتی یکی خالی بود، دیگری همان لحظه آماده باشد.
 */
@Singleton
class WaterfallAdManager @Inject constructor(
    @Named("tapsell") private val tapsell: AdManager,
    @Named("adivery") private val adivery: AdManager,
) : AdManager {

    /** شبکه‌هایی که واقعاً پیکربندی شده‌اند، به ترتیبِ اولویت. */
    private val networks: List<Pair<String, AdManager>>
        get() = buildList {
            if (tapsell.enabled) add("tapsell" to tapsell)
            if (adivery.enabled) add("adivery" to adivery)
        }

    override val enabled: Boolean get() = networks.isNotEmpty()

    override suspend fun isNetworkAvailable(): Boolean =
        networks.firstOrNull()?.second?.isNetworkAvailable() ?: false

    override suspend fun showInterstitial(activity: Activity): Boolean {
        for ((name, net) in networks) {
            val shown = runCatching { net.showInterstitial(activity) }
                .onFailure { Log.w(TAG, "waterfall: $name interstitial threw", it) }
                .getOrDefault(false)
            if (shown) {
                Log.d(TAG, "waterfall: interstitial served by $name ✓")
                return true
            }
            Log.d(TAG, "waterfall: $name had no interstitial — trying next")
        }
        Log.w(TAG, "waterfall: no network had an interstitial")
        return false
    }

    override suspend fun showRewarded(activity: Activity, onReward: () -> Unit): Boolean {
        // جایزه باید دقیقاً یک‌بار داده شود، حتی اگر دو شبکه امتحان شوند.
        var rewarded = false
        val once = { if (!rewarded) { rewarded = true; onReward() } }

        for ((name, net) in networks) {
            val shown = runCatching { net.showRewarded(activity, once) }
                .onFailure { Log.w(TAG, "waterfall: $name rewarded threw", it) }
                .getOrDefault(false)
            if (shown) {
                Log.d(TAG, "waterfall: rewarded served by $name ✓")
                return true
            }
            Log.d(TAG, "waterfall: $name had no rewarded — trying next")
        }
        Log.w(TAG, "waterfall: no network had a rewarded ad")
        return false
    }

    override suspend fun onDrawCompleted() {
        // ⚠️ فقط **یک‌بار** شمارش می‌شود.
        // هر دو manager روی همان AdFrequencyPolicy singleton کار می‌کنند، پس اگر
        // به هر دو خبر بدهیم شمارندهٔ فال دوبرابر می‌شود و تبلیغ‌ها دوبرابر
        // زودتر ظاهر می‌شوند. یک شبکه کافی است.
        networks.firstOrNull()?.let { (_, net) -> runCatching { net.onDrawCompleted() } }
    }

    override fun warmUp() {
        networks.forEach { (name, net) ->
            runCatching { net.warmUp() }
                .onFailure { Log.w(TAG, "waterfall: $name warmUp threw", it) }
        }
    }

    override fun retryWarmUpIfNeeded() {
        networks.forEach { (_, net) -> runCatching { net.retryWarmUpIfNeeded() } }
    }
}
