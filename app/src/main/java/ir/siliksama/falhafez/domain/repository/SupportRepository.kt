package ir.siliksama.falhafez.domain.repository

import ir.siliksama.falhafez.domain.model.SupportTier
import kotlinx.coroutines.flow.Flow

interface SupportRepository {
    val tier: Flow<SupportTier>
    suspend fun setTier(tier: SupportTier)
}
