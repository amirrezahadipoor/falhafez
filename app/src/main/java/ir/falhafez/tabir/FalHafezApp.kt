package ir.falhafez.tabir

import android.app.Application
import ir.falhafez.tabir.core.sound.Sounds
import ir.falhafez.tabir.data.local.seed.CorpusSeeder
import com.google.android.gms.ads.MobileAds
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class FalHafezApp : Application() {

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    @Inject
    lateinit var corpusSeeder: CorpusSeeder

    override fun onCreate() {
        super.onCreate()
        // Seed the bundled poem corpus on first launch (fully offline).
        appScope.launch { corpusSeeder.seedIfNeeded() }
        // Ads are the only network-touching component. On Iranian devices without
        // Google Play Services this must fail silently — the app keeps working offline.
        runCatching { MobileAds.initialize(this) { } }
        Sounds.init(this)
    }

    companion object {
        /** Test/screenshot hook: skips onboarding when launched with `--ez fal_screenshot true`. */
        @Volatile
        var skipOnboardingForScreenshot = false
    }
}
