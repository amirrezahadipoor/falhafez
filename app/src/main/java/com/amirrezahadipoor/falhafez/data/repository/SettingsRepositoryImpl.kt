package com.amirrezahadipoor.falhafez.data.repository

import com.amirrezahadipoor.falhafez.core.theme.FalThemeId
import com.amirrezahadipoor.falhafez.data.settings.SettingsDataStore
import com.amirrezahadipoor.falhafez.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SettingsRepositoryImpl @Inject constructor(
    private val dataStore: SettingsDataStore
) : SettingsRepository {

    override val themeId: Flow<FalThemeId> = dataStore.themeId
    override val fontSizeScale: Flow<Float> = dataStore.fontSizeScale
    override val notificationsEnabled: Flow<Boolean> = dataStore.notificationsEnabled
    override val seenOnboarding: Flow<Boolean> = dataStore.seenOnboarding
    override val rewardedExtraDraws: Flow<Int> = dataStore.rewardedExtraDraws
    override val unlockedThemes: Flow<Set<String>> = dataStore.unlockedThemes
    override val soundEnabled: Flow<Boolean> = dataStore.soundEnabled
    override val hapticsEnabled: Flow<Boolean> = dataStore.hapticsEnabled
    override val fontColor: Flow<String> = dataStore.fontColor

    override suspend fun setTheme(id: FalThemeId) {
        dataStore.setTheme(id)
    }

    override suspend fun setFontSizeScale(scale: Float) {
        dataStore.setFontSizeScale(scale)
    }

    override suspend fun setNotificationsEnabled(enabled: Boolean) {
        dataStore.setNotificationsEnabled(enabled)
    }

    override suspend fun setSeenOnboarding(seen: Boolean) {
        dataStore.setSeenOnboarding(seen)
    }

    override suspend fun addRewardedDraw(count: Int) {
        dataStore.addRewardedDraws(count)
    }

    override suspend fun unlockTheme(id: FalThemeId) {
        dataStore.unlockTheme(id.id)
    }

    override suspend fun setSoundEnabled(enabled: Boolean) {
        dataStore.setSoundEnabled(enabled)
    }

    override suspend fun setHapticsEnabled(enabled: Boolean) {
        dataStore.setHapticsEnabled(enabled)
    }

    override suspend fun setFontColor(key: String) {
        dataStore.setFontColor(key)
    }
}
