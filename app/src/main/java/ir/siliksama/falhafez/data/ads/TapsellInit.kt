package ir.siliksama.falhafez.data.ads

import android.util.Log
import ir.tapsell.mediation.Tapsell
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.withTimeoutOrNull

private const val TAG = "FalHafezAds"

/**
 * دروازهٔ راه‌اندازیِ تپسل.
 *
 * ## چرا لازم است؟
 * SDK تپسل به‌صورت خودکار (ContentProvider) راه می‌افتد، اما راه‌اندازی **آنی نیست**:
 * SDK باید پیکربندیِ شبکه‌های واسطه (mediation) را از سرور بگیرد. هر درخواستِ تبلیغی که
 * **پیش از پایانِ این مرحله** فرستاده شود، با شکست برمی‌گردد (`onFailure`).
 *
 * قبلاً بنر در اولین composition و preload در سازندهٔ AdManager درخواست می‌فرستادند —
 * یعنی دقیقاً در ثانیهٔ اولِ اجرای اپ، پیش از آماده‌شدنِ SDK. نتیجه: **هیچ تبلیغی نمایش
 * داده نمی‌شد.**
 *
 * این شیء با `Tapsell.setInitializationListener` منتظرِ آماده‌شدنِ SDK می‌ماند و همهٔ
 * درخواست‌ها را پشتِ آن صف می‌کند.
 */
object TapsellInit {

    private val ready = CompletableDeferred<Boolean>()

    @Volatile
    var isReady: Boolean = false
        private set

    /** از `FalHafezApp.onCreate` صدا زده می‌شود. */
    fun install() {
        if (ready.isCompleted) return
        runCatching {
            Tapsell.setInitializationListener {
                Log.i(TAG, "Tapsell SDK initialized ✓")
                isReady = true
                ready.complete(true)
            }
        }.onFailure {
            // اگر listener به هر دلیلی ثبت نشد، اپ نباید بدونِ تبلیغ بماند:
            // بعد از مهلتِ زیر، درخواست‌ها به‌هرحال فرستاده می‌شوند.
            Log.w(TAG, "setInitializationListener failed; falling back to timeout", it)
        }
    }

    /**
     * منتظرِ آماده‌شدنِ SDK می‌ماند (حداکثر [timeoutMs]).
     * اگر مهلت تمام شود باز هم `true` برمی‌گرداند تا درخواست دستِ‌کم یک شانس داشته باشد —
     * چون ممکن است listener روی بعضی نسخه‌ها صدا زده نشود.
     */
    suspend fun await(timeoutMs: Long = 8_000L): Boolean {
        if (isReady) return true
        val result = withTimeoutOrNull(timeoutMs) { ready.await() }
        if (result == null) {
            Log.w(TAG, "Tapsell init wait timed out after ${timeoutMs}ms — requesting anyway")
        }
        return true
    }

    /**
     * نسخهٔ غیرمسدودکننده برای کدِ View (بنر/نیتیو): اگر SDK آماده باشد فوراً اجرا می‌شود،
     * وگرنه [block] پس از آماده‌شدن روی همان callback اجرا می‌شود.
     */
    fun whenReady(block: () -> Unit) {
        if (isReady) {
            block()
            return
        }
        runCatching {
            Tapsell.setInitializationListener {
                isReady = true
                if (!ready.isCompleted) ready.complete(true)
                block()
            }
        }.onFailure {
            Log.w(TAG, "whenReady: listener failed — running immediately", it)
            block()
        }
    }
}
