package ir.siliksama.falhafez

import android.app.Application

import ir.siliksama.falhafez.core.sound.Sounds
import ir.siliksama.falhafez.core.util.SupportStore
import ir.siliksama.falhafez.data.local.seed.CorpusSeeder
import ir.siliksama.falhafez.domain.repository.SupportRepository
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class FalHafezApp : Application() {

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    @Inject
    lateinit var corpusSeeder: CorpusSeeder

    @Inject
    lateinit var supportRepository: SupportRepository

    override fun onCreate() {
        super.onCreate()
        // Seed the bundled poem corpus on first launch (fully offline).
        appScope.launch { corpusSeeder.seedIfNeeded() }
        Sounds.init(this)
        // سطح حمایت را زود بارگذاری کن تا اگر کاربر خرید کرده، از همان لحظهٔ اول تبلیغی نمایش داده نشود.
        appScope.launch {
            runCatching { SupportStore.tier = supportRepository.tier.first() }
        }
        // تپسل به‌صورت خودکار (ContentProvider) با کلیدِ مانیفست راه‌اندازی می‌شود — بدون نیاز به کد.
    }

    companion object {
        /** Test/screenshot hook: skips onboarding when launched with `--ez fal_screenshot true`. */
        @Volatile
        var skipOnboardingForScreenshot = false
    }
}
