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
 * Locally hardcoded frequency capping (no remote config, per the offline-first
 * requirement). Shows an interstitial at most once every [INTERSTITIAL_EVERY] draws.
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
            prefs[Keys.DRAWS_SINCE_LAST] = (prefs[Keys.DRAWS_SINCE_LAST] ?: 0) + 1
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
        const val INTERSTITIAL_EVERY = 4
    }
}
