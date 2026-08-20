package com.amirrezahadipoor.falhafez.presentation.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.amirrezahadipoor.falhafez.core.sound.Sounds
import com.amirrezahadipoor.falhafez.domain.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
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

    init {
        viewModelScope.launch {
            Sounds.enabled = settingsRepository.soundEnabled.first()
            Sounds.hapticsEnabled = settingsRepository.hapticsEnabled.first()
        }
    }
}
