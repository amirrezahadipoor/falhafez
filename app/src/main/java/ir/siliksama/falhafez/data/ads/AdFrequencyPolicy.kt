package ir.siliksama.falhafez.data.ads

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

private val Context.adPolicyDataStore by preferencesDataStore(name = "ad_policy")

/**
 * سقفِ فرکانسِ محلی (بدون remote config — مطابق اصلِ آفلاین‌بودنِ اپ).
 * بین‌صفحه‌ای حداکثر هر [INTERSTITIAL_EVERY] فال یک‌بار.
 */
@Singleton
class AdFrequencyPolicy @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private object Keys {
        val DRAWS_SINCE_LAST = intPreferencesKey("draws_since_last_interstitial")
    }

    suspend fun onDrawCompleted() {
        context.adPolicyDataStore.edit { prefs ->
            val current = prefs[Keys.DRAWS_SINCE_LAST] ?: 0
            // سقف‌گذاری: کاربری که ۲۰ فالِ آفلاین گرفته، هنگام آنلاین‌شدن
            // نباید پشتِ‌سرِ هم تبلیغ ببیند.
            prefs[Keys.DRAWS_SINCE_LAST] = (current + 1).coerceAtMost(INTERSTITIAL_EVERY + 2)
        }
    }

    suspend fun shouldShowInterstitial(): Boolean {
        val n = context.adPolicyDataStore.data.first()[Keys.DRAWS_SINCE_LAST] ?: 0
        return n >= INTERSTITIAL_EVERY
    }

    suspend fun recordShown() {
        context.adPolicyDataStore.edit { prefs -> prefs[Keys.DRAWS_SINCE_LAST] = 0 }
    }

    companion object {
        const val INTERSTITIAL_EVERY = 3
    }
}
