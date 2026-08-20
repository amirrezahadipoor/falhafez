package ir.siliksama.falhafez.presentation.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ir.siliksama.falhafez.domain.repository.PoemRepository
import ir.siliksama.falhafez.domain.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class SplashUiState(
    val ready: Boolean = false,
    val seenOnboarding: Boolean = false
)

@HiltViewModel
class SplashViewModel @Inject constructor(
    poemRepository: PoemRepository,
    settingsRepository: SettingsRepository
) : ViewModel() {

    val state: StateFlow<SplashUiState> =
        combine(poemRepository.observeCount(), settingsRepository.seenOnboarding) { count, seen ->
            SplashUiState(ready = count > 0, seenOnboarding = seen)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SplashUiState())
}
