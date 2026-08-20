package ir.siliksama.falhafez.presentation.main

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ir.siliksama.falhafez.core.designsystem.FalFontColors
import ir.siliksama.falhafez.core.sound.Sounds
import ir.siliksama.falhafez.core.util.ChannelStore
import ir.siliksama.falhafez.core.util.SupportStore
import ir.siliksama.falhafez.domain.model.ChannelInfo
import ir.siliksama.falhafez.domain.model.SupportTier
import ir.siliksama.falhafez.domain.repository.SettingsRepository
import ir.siliksama.falhafez.domain.repository.SupportRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    settingsRepository: SettingsRepository,
    supportRepository: SupportRepository
) : ViewModel() {

    /** Global reading-font scale, applied across the whole app (sp-based). */
    val fontSizeScale: StateFlow<Float> = settingsRepository.fontSizeScale
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 1f)

    /** User's chosen reading-font color (null = follow the active theme). */
    val fontColor: StateFlow<Color?> = settingsRepository.fontColor
        .map { FalFontColors.toColor(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    init {
        viewModelScope.launch {
            Sounds.enabled = settingsRepository.soundEnabled.first()
            Sounds.hapticsEnabled = settingsRepository.hapticsEnabled.first()
        }
        viewModelScope.launch {
            supportRepository.tier.collect { SupportStore.tier = it }
        }
        viewModelScope.launch {
            val network = settingsRepository.channelNetwork.first()
            val handle = settingsRepository.channelHandle.first()
            val name = settingsRepository.channelName.first()
            ChannelStore.info = ChannelInfo(network, handle, name)
        }
    }
}
