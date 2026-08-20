package com.amirrezahadipoor.falhafez.domain.repository

import com.amirrezahadipoor.falhafez.core.theme.FalThemeId
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    val themeId: Flow<FalThemeId>
    val fontSizeScale: Flow<Float>
    val notificationsEnabled: Flow<Boolean>
    val seenOnboarding: Flow<Boolean>
    val rewardedExtraDraws: Flow<Int>
    val unlockedThemes: Flow<Set<String>>

    suspend fun setTheme(id: FalThemeId)
    suspend fun setFontSizeScale(scale: Float)
    suspend fun setNotificationsEnabled(enabled: Boolean)
    suspend fun setSeenOnboarding(seen: Boolean)
    suspend fun addRewardedDraw(count: Int)
    suspend fun unlockTheme(id: FalThemeId)
}
