package ir.siliksama.falhafez

import android.app.Application
import android.util.Log

import ir.siliksama.falhafez.core.sound.Sounds
import ir.siliksama.falhafez.core.util.SupportStore
import ir.siliksama.falhafez.data.ads.AdConfig
import ir.siliksama.falhafez.data.local.seed.CorpusSeeder
import ir.siliksama.falhafez.domain.repository.SupportRepository
import ir.tapsell.mediation.Tapsell
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

    override fun onCreate() {
        super.onCreate()
        installCrashGuard()
        // Seed the bundled poem corpus on first launch (fully offline).
        appScope.launch { corpusSeeder.seedIfNeeded() }
        Sounds.init(this)
        // سطح حمایت را زود بارگذاری کن تا اگر کاربر خرید کرده، از همان لحظهٔ اول تبلیغی نمایش داده نشود.
        appScope.launch {
            runCatching { SupportStore.tier = supportRepository.tier.first() }
        }

        // راه‌اندازی صریح SDK تپسل در آغاز برنامه
        runCatching {
            Tapsell.initialize(this, AdConfig.TAPSELL_APP_KEY)
            Log.d("FalHafez", "Tapsell SDK initialized in Application.onCreate")
        }.onFailure { Log.w("FalHafez", "Tapsell init failed in Application.onCreate", it) }
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
