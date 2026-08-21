package ir.siliksama.falhafez.domain.repository

import ir.siliksama.falhafez.core.theme.FalThemeId
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    val themeId: Flow<FalThemeId>
    val fontSizeScale: Flow<Float>
    val notificationsEnabled: Flow<Boolean>
    val seenOnboarding: Flow<Boolean>
    val rewardedExtraDraws: Flow<Int>
    val soundEnabled: Flow<Boolean>
    val hapticsEnabled: Flow<Boolean>
    val fontColor: Flow<String>
    val supportTier: Flow<String>
    val channelNetwork: Flow<String>
    val channelHandle: Flow<String>
    val channelName: Flow<String>
    val lastUpdateCheckDay: Flow<Long>

    suspend fun setTheme(id: FalThemeId)
    suspend fun setFontSizeScale(scale: Float)
    suspend fun setNotificationsEnabled(enabled: Boolean)
    suspend fun setSeenOnboarding(seen: Boolean)
    suspend fun addRewardedDraw(count: Int)
    suspend fun setSoundEnabled(enabled: Boolean)
    suspend fun setHapticsEnabled(enabled: Boolean)
    suspend fun setFontColor(key: String)
    suspend fun setSupportTier(key: String)
    suspend fun setChannel(network: String, handle: String, name: String)
    suspend fun setLastUpdateCheckDay(day: Long)
}
