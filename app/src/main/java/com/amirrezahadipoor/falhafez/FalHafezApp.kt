package com.amirrezahadipoor.falhafez

import android.app.Application
import com.amirrezahadipoor.falhafez.data.local.seed.CorpusSeeder
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
        // Ads are the only network-touching component; everything else stays offline.
        MobileAds.initialize(this) { }
    }
}
