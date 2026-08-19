package com.amirrezahadipoor.falhafez.data.ads

import android.app.Activity

/**
 * Ad abstraction layer — the UI never touches a concrete SDK. Swapping or
 * mediating Tapsell/AdMob only means replacing the implementation in
 * [com.amirrezahadipoor.falhafez.di.AdModule], not touching any screen.
 *
 * The app is offline-first: ads simply do not show when there is no network.
 */
interface AdManager {

    /** True when an ad network is configured and could serve. */
    val enabled: Boolean

    suspend fun isNetworkAvailable(): Boolean

    /**
     * Occasionally shows a full-screen interstitial (frequency-capped). Called
     * only AFTER the user finishes reading a fal and returns home — never during
     * the draw ritual or reveal. Returns true when an ad was actually shown.
     */
    suspend fun showInterstitial(activity: Activity): Boolean

    /**
     * Rewarded video. The caller passes [onReward], invoked exactly once if the
     * user watches the full ad and earns the reward. Returns true if shown.
     */
    suspend fun showRewarded(activity: Activity, onReward: () -> Unit): Boolean

    /** Notify the ad layer that a draw completed (drives frequency capping). */
    suspend fun onDrawCompleted()
}
