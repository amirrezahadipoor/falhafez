package ir.siliksama.falhafez.data.repository

import ir.siliksama.falhafez.core.util.SupportStore
import ir.siliksama.falhafez.domain.model.SupportTier
import ir.siliksama.falhafez.domain.repository.SettingsRepository
import ir.siliksama.falhafez.domain.repository.SupportRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class SupportRepositoryImpl @Inject constructor(
    private val settingsRepository: SettingsRepository
) : SupportRepository {

    override val tier: Flow<SupportTier> =
        settingsRepository.supportTier.map { SupportTier.fromKey(it) }

    override suspend fun setTier(tier: SupportTier) {
        settingsRepository.setSupportTier(tier.key)
        SupportStore.tier = tier
    }
}
