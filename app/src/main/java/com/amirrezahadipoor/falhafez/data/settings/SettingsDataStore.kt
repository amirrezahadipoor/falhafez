package com.amirrezahadipoor.falhafez.data.settings

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.amirrezahadipoor.falhafez.core.theme.FalThemeId
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.settingsDataStore by preferencesDataStore(name = "fal_settings")

@Singleton
class SettingsDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private object Keys {
        val THEME = stringPreferencesKey("theme_id")
        val FONT_SCALE = floatPreferencesKey("font_scale")
        val NOTIFICATIONS = booleanPreferencesKey("notifications_enabled")
        val SEEN_ONBOARDING = booleanPreferencesKey("seen_onboarding")
        val REWARDED_DRAWS = intPreferencesKey("rewarded_extra_draws")
        val UNLOCKED_THEMES = stringSetPreferencesKey("unlocked_themes")
    }

    val themeId: Flow<FalThemeId> =
        context.settingsDataStore.data.map { FalThemeId.fromId(it[Keys.THEME]) }

    val fontSizeScale: Flow<Float> =
        context.settingsDataStore.data.map { it[Keys.FONT_SCALE] ?: 1f }

    val notificationsEnabled: Flow<Boolean> =
        context.settingsDataStore.data.map { it[Keys.NOTIFICATIONS] ?: false }

    val seenOnboarding: Flow<Boolean> =
        context.settingsDataStore.data.map { it[Keys.SEEN_ONBOARDING] ?: false }

    val rewardedExtraDraws: Flow<Int> =
        context.settingsDataStore.data.map { it[Keys.REWARDED_DRAWS] ?: 0 }

    val unlockedThemes: Flow<Set<String>> =
        context.settingsDataStore.data.map { it[Keys.UNLOCKED_THEMES] ?: emptySet() }

    suspend fun setTheme(id: FalThemeId) =
        context.settingsDataStore.edit { it[Keys.THEME] = id.id }

    suspend fun setFontSizeScale(scale: Float) =
        context.settingsDataStore.edit { it[Keys.FONT_SCALE] = scale }

    suspend fun setNotificationsEnabled(enabled: Boolean) =
        context.settingsDataStore.edit { it[Keys.NOTIFICATIONS] = enabled }

    suspend fun setSeenOnboarding(seen: Boolean) =
        context.settingsDataStore.edit { it[Keys.SEEN_ONBOARDING] = seen }

    suspend fun addRewardedDraws(count: Int) =
        context.settingsDataStore.edit { prefs ->
            val current = prefs[Keys.REWARDED_DRAWS] ?: 0
            prefs[Keys.REWARDED_DRAWS] = current + count
        }

    suspend fun unlockTheme(id: String) =
        context.settingsDataStore.edit { prefs ->
            val current = prefs[Keys.UNLOCKED_THEMES] ?: emptySet()
            prefs[Keys.UNLOCKED_THEMES] = current + id
        }
}
