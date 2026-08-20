package ir.siliksama.falhafez.core.util

import ir.siliksama.falhafez.domain.model.ChannelInfo
import ir.siliksama.falhafez.domain.model.SupportTier

/**
 * In-memory mirrors of the DataStore-backed settings, kept in sync by the app
 * ViewModels so non-Compose code (share-image renderer, ad layer) can read them
 * without threading values through every screen.
 */
object SupportStore {
    @Volatile var tier: SupportTier = SupportTier.NONE
}

object ChannelStore {
    @Volatile var info: ChannelInfo? = null
}
