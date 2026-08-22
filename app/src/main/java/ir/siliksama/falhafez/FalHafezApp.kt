package ir.siliksama.falhafez

import android.app.Application
import android.util.Log

import ir.siliksama.falhafez.core.sound.Sounds
import ir.siliksama.falhafez.core.util.SupportStore
import ir.siliksama.falhafez.data.ads.AdManager
import ir.siliksama.falhafez.data.ads.TapsellInit
import ir.siliksama.falhafez.data.local.seed.CorpusSeeder
import ir.siliksama.falhafez.domain.repository.SupportRepository
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class FalHafezApp : Application() {

    private val crashHandler = CoroutineExceptionHandler { _, throwable ->
        Log.e("FalHafez", "Uncaught coroutine exception", throwable)
    }
    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO + crashHandler)

    @Inject
    lateinit var corpusSeeder: CorpusSeeder

    @Inject
    lateinit var supportRepository: SupportRepository

    @Inject
    lateinit var adManager: AdManager

    override fun onCreate() {
        super.onCreate()
        installCrashGuard()
        // Seed the bundled poem corpus on first launch (fully offline).
        appScope.launch { corpusSeeder.seedIfNeeded() }
        Sounds.init(this)

        // تپسل خودش با ContentProvider راه می‌افتد، اما راه‌اندازی **آنی نیست**.
        // این listener را همین اول ثبت می‌کنیم تا لایهٔ تبلیغات بداند کِی اجازهٔ
        // درخواست دارد. بدونِ آن، اولین درخواست‌ها پیش از آماده‌شدنِ SDK می‌رفتند
        // و همیشه شکست می‌خوردند — علتِ «هیچ تبلیغی نمایش داده نمی‌شود».
        TapsellInit.install()

        // سطح حمایت را زود بارگذاری کن تا اگر کاربر خرید کرده، از همان لحظهٔ اول تبلیغی نمایش داده نشود.
        appScope.launch {
            runCatching { SupportStore.tier = supportRepository.tier.first() }
            // گرم‌کردنِ تبلیغات فقط پس از دانستنِ سطحِ حمایت (رفعِ شرطِ رقابتی).
            runCatching { adManager.warmUp() }
        }
    }

    /**
     * حفاظِ کراش: خطاهای uncaught (روی هر نخ — از جمله کوروتین‌های viewModelScope)
     * را می‌بلعد تا فال و روندِ کاربر هرگز با دیالوگِ «اپ متوقف شد» بسته نشود.
     * فقط خطاهای غیرقابل‌بازیابی (OOM / StackOverflow) به سیستم سپرده می‌شوند.
     */
    private fun installCrashGuard() {
        val previous = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            try {
                if (throwable is OutOfMemoryError || throwable is StackOverflowError) {
                    previous?.uncaughtException(thread, throwable)
                } else {
                    Log.e("FalHafez", "Uncaught exception on ${thread.name}", throwable)
                }
            } catch (_: Throwable) {
                previous?.uncaughtException(thread, throwable)
            }
        }
    }

    companion object {
        /** Test/screenshot hook: skips onboarding when launched with `--ez fal_screenshot true`. */
        @Volatile
        var skipOnboardingForScreenshot = false
    }
}
