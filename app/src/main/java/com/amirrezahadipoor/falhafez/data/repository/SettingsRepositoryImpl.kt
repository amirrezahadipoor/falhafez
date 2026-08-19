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

    override suspend fun setTheme(id: FalThemeId) = dataStore.setTheme(id)
    override suspend fun setFontSizeScale(scale: Float) = dataStore.setFontSizeScale(scale)
    override suspend fun setNotificationsEnabled(enabled: Boolean) = dataStore.setNotificationsEnabled(enabled)
    override suspend fun setSeenOnboarding(seen: Boolean) = dataStore.setSeenOnboarding(seen)
    override suspend fun addRewardedDraw(count: Int) = dataStore.addRewardedDraws(count)
}
