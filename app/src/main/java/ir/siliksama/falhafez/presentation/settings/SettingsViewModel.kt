package ir.siliksama.falhafez.presentation.settings

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ir.siliksama.falhafez.core.sound.Sounds
import ir.siliksama.falhafez.core.theme.FalThemeId
import ir.siliksama.falhafez.core.util.ChannelStore
import ir.siliksama.falhafez.core.util.SupportStore
import ir.siliksama.falhafez.data.payments.PaymentGateway
import ir.siliksama.falhafez.domain.model.ChannelInfo
import ir.siliksama.falhafez.domain.model.SupportTier
import ir.siliksama.falhafez.domain.repository.DrawRepository
import ir.siliksama.falhafez.domain.repository.FavoriteRepository
import ir.siliksama.falhafez.domain.repository.SettingsRepository
import ir.siliksama.falhafez.domain.repository.SupportRepository
import ir.siliksama.falhafez.presentation.notifications.ReminderScheduler
import ir.siliksama.falhafez.presentation.share.ChannelPromoRenderer
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val supportRepository: SupportRepository,
    private val paymentGateway: PaymentGateway,
    private val adManager: ir.siliksama.falhafez.data.ads.AdManager,
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

    val supportTier: StateFlow<SupportTier> = supportRepository.tier
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SupportTier.NONE)

    val channelNetwork: StateFlow<String> = settingsRepository.channelNetwork
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "telegram")

    val channelHandle: StateFlow<String> = settingsRepository.channelHandle
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    val channelName: StateFlow<String> = settingsRepository.channelName
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    private val _purchasing = MutableStateFlow(false)
    val purchasing: StateFlow<Boolean> = _purchasing.asStateFlow()

    // ---- theme / display / sound ----
    fun setTheme(id: FalThemeId) {
        viewModelScope.launch { settingsRepository.setTheme(id) }
    }

    fun setFontSizeScale(scale: Float) {
        viewModelScope.launch { settingsRepository.setFontSizeScale(scale) }
    }

    fun setFontColor(key: String) {
        viewModelScope.launch { settingsRepository.setFontColor(key) }
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

    // ---- حمایت مالی ----
    fun purchase(activity: Activity, tier: SupportTier) {
        if (_purchasing.value) return
        _purchasing.value = true
        paymentGateway.purchase(activity, tier) {
            viewModelScope.launch {
                supportRepository.setTier(tier)
                // امتیاز سطح ویژه: قفلِ قالبِ یلدا برای PLUS و GOLD باز می‌شود
                if (tier == SupportTier.PLUS || tier == SupportTier.GOLD) {
                    settingsRepository.unlockTheme(FalThemeId.YALDA)
                }
                _purchasing.value = false
            }
        }
        viewModelScope.launch { _purchasing.value = false }
    }

    // ---- کانال اجتماعی ----
    fun setChannel(network: String, handle: String, name: String) {
        ChannelStore.info = ChannelInfo(network, handle, name)
        viewModelScope.launch { settingsRepository.setChannel(network, handle, name) }
    }

    /** ساخت و اشتراکِ عکس تبلیغاتی کانال کاربر. */
    fun shareChannelPromo(handle: String, name: String) {
        val info = ChannelInfo(channelNetwork.value, handle, name)
        if (!info.isSet) return
        viewModelScope.launch {
            val bitmap = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Default) {
                ChannelPromoRenderer.render(context.applicationContext, info)
            }
            val dir = File(context.cacheDir, "share").apply { mkdirs() }
            val file = File(dir, "channel_${System.currentTimeMillis()}.png")
            file.outputStream().use { bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, it) }
            runCatching {
                val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "image/png"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                context.startActivity(
                    Intent.createChooser(intent, "اشتراک عکس تبلیغاتی کانال")
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                )
            }
        }
    }

    // ---- داده‌ها ----
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
