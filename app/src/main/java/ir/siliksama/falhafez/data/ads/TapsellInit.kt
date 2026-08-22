package ir.siliksama.falhafez.data.ads

import android.os.Handler
import android.os.Looper
import android.util.Log
import ir.tapsell.mediation.Tapsell
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.withTimeoutOrNull
import java.util.concurrent.CopyOnWriteArrayList

private const val TAG = "FalHafezAds"

/**
 * دروازهٔ راه‌اندازیِ تپ‌سل.
 *
 * ## چرا لازم است؟
 * SDK تپ‌سل خودکار (با ContentProvider) راه می‌افتد، اما راه‌اندازی **آنی نیست**:
 * باید پیکربندیِ شبکه‌های واسطه را از سرور بگیرد. هر درخواستی که پیش از پایانِ
 * این مرحله فرستاده شود با `onFailure` برمی‌گردد.
 *
 * ## باگی که اینجا رفع شد (علتِ «هیچ تبلیغی نمایش داده نمی‌شود»)
 * `Tapsell.setInitializationListener` یک **setter تک‌خانه** است: هر بار صدا زدنش
 * listenerِ قبلی را *جایگزین* می‌کند، نه اینکه به فهرست اضافه کند.
 *
 * نسخهٔ قبلی این متد را در دو جا صدا می‌زد:
 *  ۱. `install()` هنگام شروعِ اپ
 *  ۲. **هر بار** که `whenReady { … }` صدا زده می‌شد (بنر، نیتیو، …)
 *
 * پس اولین بنری که ساخته می‌شد، listenerِ `install()` را پاک می‌کرد. بدتر: اگر
 * دو بنر پشتِ‌سرِ هم ساخته می‌شدند، بنرِ دوم callbackِ بنرِ اول را هم پاک می‌کرد و
 * آن بنر **هرگز** درخواستش را نمی‌فرستاد. و اگر SDK پیش از ثبتِ listener آماده
 * شده بود، هیچ‌کس خبردار نمی‌شد و همه‌چیز تا ابد منتظر می‌ماند.
 *
 * راهکار: **فقط یک‌بار** listener ثبت می‌شود (در `install()`)، و بقیه در یک
 * صفِ داخلی منتظر می‌مانند. ضمناً یک مهلتِ ایمنی هست تا اگر callback هرگز صدا
 * زده نشد (نسخه‌های مختلف SDK رفتارِ یکسانی ندارند)، درخواست‌ها به‌هرحال
 * فرستاده شوند — «تبلیغِ دیرهنگام» بی‌نهایت بهتر از «هیچ تبلیغی» است.
 */
object TapsellInit {

    private val ready = CompletableDeferred<Boolean>()
    private val waiters = CopyOnWriteArrayList<() -> Unit>()
    private val main = Handler(Looper.getMainLooper())

    /** مهلتِ ایمنی: پس از این مدت، آماده فرض می‌کنیم حتی اگر callback نیامده باشد. */
    private const val SAFETY_TIMEOUT_MS = 6_000L

    @Volatile
    var isReady: Boolean = false
        private set

    @Volatile
    private var installed = false

    /** چرا آماده شدیم: listener یا مهلتِ ایمنی. برای صفحهٔ عیب‌یابی. */
    @Volatile
    var readyReason: String = "—"
        private set

    /** تعداد درخواست‌هایی که هنوز در صف منتظرند. */
    val queuedCount: Int get() = waiters.size

    /** از `FalHafezApp.onCreate` صدا زده می‌شود — دقیقاً یک‌بار. */
    @Synchronized
    fun install() {
        if (installed) return
        installed = true

        runCatching {
            Tapsell.setInitializationListener {
                Log.i(TAG, "Tapsell SDK initialized ✓")
                markReady("listener")
            }
        }.onFailure {
            Log.w(TAG, "setInitializationListener failed", it)
        }

        // شبکهٔ ایران گاهی کند است و روی بعضی نسخه‌ها این callback اصلاً صدا زده
        // نمی‌شود. بدونِ این مهلت، اپ برای همیشه منتظر می‌ماند و کاربر هیچ‌وقت
        // تبلیغی نمی‌بیند — یعنی همان باگی که می‌خواهیم رفع کنیم.
        main.postDelayed({
            if (!isReady) {
                Log.w(TAG, "init callback never fired after ${SAFETY_TIMEOUT_MS}ms — proceeding anyway")
                markReady("timeout")
            }
        }, SAFETY_TIMEOUT_MS)
    }

    private fun markReady(reason: String) {
        if (isReady) return
        isReady = true
        readyReason = reason
        if (!ready.isCompleted) ready.complete(true)

        val pending = waiters.toList()
        waiters.clear()
        if (pending.isNotEmpty()) {
            Log.d(TAG, "releasing ${pending.size} queued ad request(s) [$reason]")
        }
        pending.forEach { block ->
            runCatching { block() }.onFailure { Log.w(TAG, "queued ad request threw", it) }
        }
    }

    /**
     * منتظرِ آماده‌شدنِ SDK می‌ماند (حداکثر [timeoutMs]).
     * همیشه `true` برمی‌گرداند تا درخواست دستِ‌کم یک شانس داشته باشد.
     */
    suspend fun await(timeoutMs: Long = 8_000L): Boolean {
        if (isReady) return true
        if (!installed) install()
        withTimeoutOrNull(timeoutMs) { ready.await() }
            ?: Log.w(TAG, "Tapsell init wait timed out after ${timeoutMs}ms — requesting anyway")
        return true
    }

    /**
     * نسخهٔ غیرمسدودکننده برای کدِ View (بنر/نیتیو).
     *
     * برخلافِ نسخهٔ قبل **هرگز** listener را دوباره ثبت نمی‌کند؛ فقط در صف
     * می‌نشیند. اجرای [block] همیشه روی نخِ اصلی است، چون سازندهٔ View را
     * می‌سازد و به تبلیغ وصلش می‌کند.
     */
    fun whenReady(block: () -> Unit) {
        if (isReady) {
            runOnMain(block)
            return
        }
        if (!installed) install()
        waiters += { runOnMain(block) }
    }

    private fun runOnMain(block: () -> Unit) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            runCatching { block() }.onFailure { Log.w(TAG, "ad block threw", it) }
        } else {
            main.post { runCatching { block() }.onFailure { Log.w(TAG, "ad block threw", it) } }
        }
    }
}
