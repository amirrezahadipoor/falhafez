package ir.siliksama.falhafez.presentation.main

import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import ir.siliksama.falhafez.core.designsystem.FalFontColors
import ir.siliksama.falhafez.core.sound.Sounds
import ir.siliksama.falhafez.core.util.ChannelStore
import ir.siliksama.falhafez.core.util.SupportStore
import ir.siliksama.falhafez.data.updates.UpdateChecker
import ir.siliksama.falhafez.domain.model.ChannelInfo
import ir.siliksama.falhafez.domain.model.SupportTier
import ir.siliksama.falhafez.domain.model.UpdateCheckResult
import ir.siliksama.falhafez.domain.repository.SettingsRepository
import ir.siliksama.falhafez.domain.repository.SupportRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    settingsRepository: SettingsRepository,
    supportRepository: SupportRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    /** Global reading-font scale, applied across the whole app (sp-based). */
    val fontSizeScale: StateFlow<Float> = settingsRepository.fontSizeScale
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 1f)

    /** User's chosen reading-font color (null = follow the active theme). */
    val fontColor: StateFlow<Color?> = settingsRepository.fontColor
        .map { FalFontColors.toColor(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _pendingUpdate = MutableStateFlow<UpdateCheckResult.Available?>(null)
    val pendingUpdate: StateFlow<UpdateCheckResult.Available?> = _pendingUpdate.asStateFlow()

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
        // چک خودکار بروزرسانی — فقط یک‌بار در روز، کاملاً بی‌صدا (آفلاین = هیچ)
        viewModelScope.launch {
            val today = System.currentTimeMillis() / 86_400_000L
            val last = settingsRepository.lastUpdateCheckDay.first()
            if (today > last) {
                settingsRepository.setLastUpdateCheckDay(today)
                val result = UpdateChecker.check()
                if (result is UpdateCheckResult.Available) {
                    _pendingUpdate.value = result
                }
            }
        }
    }

    fun dismissUpdate() {
        _pendingUpdate.value = null
    }
}
