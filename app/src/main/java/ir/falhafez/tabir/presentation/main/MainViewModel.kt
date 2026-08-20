package ir.falhafez.tabir.presentation.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.compose.ui.graphics.Color
import ir.falhafez.tabir.core.designsystem.FalFontColors
import ir.falhafez.tabir.core.sound.Sounds
import ir.falhafez.tabir.domain.repository.SettingsRepository
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
    settingsRepository: SettingsRepository
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
    }
}
