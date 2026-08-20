package ir.falhafez.tabir.data.ads

import android.app.Activity
import android.content.Context
import android.net.ConnectivityManager
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

/**
 * Google AdMob implementation of [AdManager]. This is the buildable reference
 * network; Tapsell is added as a mediated source via the adapter documented in
 * docs/TAPSELL_INTEGRATION.md.
 */
@Singleton
class AdMobAdManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val frequencyPolicy: AdFrequencyPolicy
) : AdManager {

    override val enabled = true

    override suspend fun isNetworkAvailable(): Boolean {
        val cm = context.getSystemService(ConnectivityManager::class.java) ?: return false
        val info = cm.activeNetworkInfo ?: return false
        return info.isConnectedOrConnecting
    }

    override suspend fun onDrawCompleted() {
        frequencyPolicy.onDrawCompleted()
    }

    override suspend fun showInterstitial(activity: Activity): Boolean {
        if (!isNetworkAvailable()) return false
        if (!frequencyPolicy.shouldShowInterstitial()) return false

        val ad = loadInterstitial() ?: return false
        withContext(Dispatchers.Main) {
            ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdFailedToShowFullScreenContent(error: AdError) { /* no-op */ }
                override fun onAdDismissedFullScreenContent() { /* no-op */ }
            }
            ad.show(activity)
        }
        frequencyPolicy.recordShown()
        return true
    }

    override suspend fun showRewarded(activity: Activity, onReward: () -> Unit): Boolean {
        if (!isNetworkAvailable()) return false

        val ad = loadRewarded() ?: return false
        withContext(Dispatchers.Main) {
            var earned = false
            ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdFailedToShowFullScreenContent(error: AdError) { /* no-op */ }
                override fun onAdDismissedFullScreenContent() {
                    if (earned) onReward()
                }
            }
            ad.show(activity) { _ -> earned = true }
        }
        return true
    }

    private suspend fun loadInterstitial(): InterstitialAd? =
        suspendCancellableCoroutine { cont ->
            InterstitialAd.load(
                context,
                AdConfig.INTERSTITIAL_ID,
                AdRequest.Builder().build(),
                object : InterstitialAdLoadCallback() {
                    override fun onAdLoaded(ad: InterstitialAd) { cont.resume(ad) }
                    override fun onAdFailedToLoad(error: LoadAdError) { cont.resume(null) }
                }
            )
        }

    private suspend fun loadRewarded(): RewardedAd? =
        suspendCancellableCoroutine { cont ->
            RewardedAd.load(
                context,
                AdConfig.REWARDED_ID,
                AdRequest.Builder().build(),
                object : RewardedAdLoadCallback() {
                    override fun onAdLoaded(ad: RewardedAd) { cont.resume(ad) }
                    override fun onAdFailedToLoad(error: LoadAdError) { cont.resume(null) }
                }
            )
        }
}
