package ir.falhafez.tabir.presentation.settings

import android.app.Activity
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import android.content.Intent
import androidx.core.content.FileProvider
import ir.falhafez.tabir.core.sound.Sounds
import ir.falhafez.tabir.core.theme.FalThemeId
import ir.falhafez.tabir.data.ads.AdManager
import ir.falhafez.tabir.domain.repository.DrawRepository
import ir.falhafez.tabir.domain.repository.FavoriteRepository
import ir.falhafez.tabir.domain.repository.SettingsRepository
import ir.falhafez.tabir.presentation.notifications.ReminderScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val adManager: AdManager,
    private val drawRepository: DrawRepository,
    private val favoriteRepository: FavoriteRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    val themeId: StateFlow<FalThemeId> = settingsRepository.themeId
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), FalThemeId.TAZHIB)

    val fontSizeScale: StateFlow<Float> = settingsRepository.fontSizeScale
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 1f)

    val notificationsEnabled: StateFlow<Boolean> = settingsRepository.notificationsEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val unlockedThemes: StateFlow<Set<String>> = settingsRepository.unlockedThemes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    val soundEnabled: StateFlow<Boolean> = settingsRepository.soundEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val hapticsEnabled: StateFlow<Boolean> = settingsRepository.hapticsEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val fontColor: StateFlow<String> = settingsRepository.fontColor
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "theme")

    fun setTheme(id: FalThemeId) {
        viewModelScope.launch { settingsRepository.setTheme(id) }
    }

    fun setFontSizeScale(scale: Float) {
        viewModelScope.launch { settingsRepository.setFontSizeScale(scale) }
    }

    fun setNotifications(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setNotificationsEnabled(enabled)
            ReminderScheduler.schedule(context, enabled)
        }
    }

    fun setSound(enabled: Boolean) {
        Sounds.enabled = enabled
        viewModelScope.launch { settingsRepository.setSoundEnabled(enabled) }
    }

    fun setHaptics(enabled: Boolean) {
        Sounds.hapticsEnabled = enabled
        viewModelScope.launch { settingsRepository.setHapticsEnabled(enabled) }
    }

    fun setFontColor(key: String) {
        viewModelScope.launch { settingsRepository.setFontColor(key) }
    }

    /** Rewarded unlock for premium themes (شب یلدا …). */
    fun requestUnlockTheme(activity: Activity, id: FalThemeId) {
        viewModelScope.launch {
            adManager.showRewarded(activity) {
                viewModelScope.launch {
                    settingsRepository.unlockTheme(id)
                    settingsRepository.setTheme(id)
                }
            }
        }
    }

    /** Exports history + favorites to a JSON file and shares it (data portability). */
    fun exportData() {
        viewModelScope.launch {
            val history = drawRepository.observeHistory().first()
            val favorites = favoriteRepository.favoriteIds()
            val json = buildString {
                append("{\n  \"exportedAt\": ").append(System.currentTimeMillis()).append(",\n")
                append("  \"history\": [")
                history.forEachIndexed { i, d ->
                    append(if (i == 0) "\n" else ",\n")
                    append("    {\"poemId\": ").append(d.poem.id)
                    append(", \"poet\": \"").append(d.poem.poet.faName)
                    append("\", \"question\": \"").append(d.question ?: "").append("\"}")
                }
                append("\n  ],\n  \"favorites\": ").append(favorites.joinToString(prefix = "[", postfix = "]")).append("\n}")
            }
            val dir = File(context.cacheDir, "backup").apply { mkdirs() }
            val file = File(dir, "fal_backup_${System.currentTimeMillis()}.json")
            file.writeText(json)
            runCatching {
                val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "application/json"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                context.startActivity(
                    Intent.createChooser(intent, "پشتیبان‌گیری از داده‌ها")
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                )
            }
        }
    }
}
