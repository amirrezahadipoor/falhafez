package ir.siliksama.falhafez.presentation.home

import android.app.Activity
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ir.siliksama.falhafez.core.sound.Sounds
import ir.siliksama.falhafez.core.theme.FalThemeId
import ir.siliksama.falhafez.data.ads.AdManager
import ir.siliksama.falhafez.data.payments.PaymentGateway
import ir.siliksama.falhafez.domain.model.DrawEntry
import ir.siliksama.falhafez.domain.model.FalCategory
import ir.siliksama.falhafez.domain.model.ChannelInfo
import ir.siliksama.falhafez.domain.model.Poem
import ir.siliksama.falhafez.domain.model.Poet
import ir.siliksama.falhafez.domain.model.SupportTier
import ir.siliksama.falhafez.domain.repository.DrawRepository
import ir.siliksama.falhafez.domain.repository.PoemRepository
import ir.siliksama.falhafez.domain.repository.FavoriteRepository
import ir.siliksama.falhafez.domain.repository.SettingsRepository
import ir.siliksama.falhafez.domain.repository.SupportRepository
import ir.siliksama.falhafez.domain.usecase.DailyFalUseCase
import ir.siliksama.falhafez.domain.usecase.DrawFalUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

private const val DAILY_FREE_LIMIT = 2
private const val COOLDOWN_MS = 8_000L

enum class DrawStage { NIYYAT, DRAWING, REVEAL, INTERPRETATION }

data class HomeUiState(
    val question: String = "",
    val category: FalCategory = FalCategory.NONE,
    val falSource: Poet? = Poet.HAFEZ,
    val sourceCount: Int = 495,
    val stage: DrawStage = DrawStage.NIYYAT,
    val lastDraw: DrawEntry? = null,
    val dailyFal: Poem? = null,
    val cooldownActive: Boolean = false,
    val remainingToday: Int = DAILY_FREE_LIMIT,
    val busy: Boolean = false,
    val supportOpen: Boolean = false
) {
    val canDraw: Boolean
        get() = !busy && !cooldownActive && remainingToday > 0 && stage != DrawStage.DRAWING
}

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val drawFal: DrawFalUseCase,
    private val dailyFal: DailyFalUseCase,
    private val drawRepository: DrawRepository,
    private val poemRepository: PoemRepository,
    private val favoriteRepository: FavoriteRepository,
    private val settingsRepository: SettingsRepository,
    private val supportRepository: SupportRepository,
    private val adManager: AdManager,
    private val paymentGateway: PaymentGateway
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    /** The poem whose favorite state we're showing (last draw or the daily fal). */
    private val favoriteTargetId = MutableStateFlow<Long?>(null)

    val themeId: StateFlow<FalThemeId> = settingsRepository.themeId
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), FalThemeId.TAZHIB)

    val supportTier: StateFlow<SupportTier> = supportRepository.tier
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SupportTier.NONE)

    /** کانال اجتماعیِ کاربر — برای نمایش و تبلیغ روی فال‌های اشتراکی. */
    val channel: StateFlow<ChannelInfo?> =
        combine(
            settingsRepository.channelNetwork,
            settingsRepository.channelHandle,
            settingsRepository.channelName
        ) { network, handle, name -> ChannelInfo(network, handle, name) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val adsRemoved: StateFlow<Boolean> = supportTier
        .map { it.adsRemoved }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val isFavorite: StateFlow<Boolean> = favoriteTargetId
        .flatMapLatest { id ->
            if (id == null) flowOf(false) else favoriteRepository.observeIsFavorite(id)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    private var counts: Map<Poet, Int> = emptyMap()

    private val _purchasing = MutableStateFlow(false)
    val purchasing: StateFlow<Boolean> = _purchasing.asStateFlow()

    init {
        viewModelScope.launch {
            recomputeRemaining()
            counts = Poet.entries.associateWith { poemRepository.countForPoet(it) }
            _uiState.update { it.copy(sourceCount = counts[it.falSource] ?: 495) }
        }
    }

    fun onQuestionChange(value: String) = _uiState.update { it.copy(question = value) }

    fun onCategorySelect(category: FalCategory) = _uiState.update {
        it.copy(category = if (it.category == category) FalCategory.NONE else category)
    }

    /** Fal source: حافظ / سعدی / مولانا / خیام / همهٔ مجموعه‌ها. */
    fun onSourceSelect(source: Poet?) {
        viewModelScope.launch {
            // شمارنده‌ها را تازه بگیر (در اولین اجرا ممکن است هنوز کامل seed نشده باشند)
            counts = Poet.entries.associateWith { poemRepository.countForPoet(it) }
            val count = when (source) {
                null -> counts.values.sum()
                else -> counts[source] ?: 0
            }
            _uiState.update { it.copy(falSource = source, sourceCount = count) }
        }
    }

    /** سهمیهٔ روزانه را تازه‌سازی کن (هنگام برگشت اپ به پیش‌زمینه — مثلاً بعد از نیمه‌شب). */
    fun refreshQuota() {
        viewModelScope.launch { recomputeRemaining() }
    }

    fun draw() {
        val s = _uiState.value
        if (!s.canDraw) return
        viewModelScope.launch { performDraw(bypassCooldown = false) }
    }

    /** فالِ بعد از سهمیهٔ رایگان — با تماشای ویدیوی پاداشی (هر ویدیو = یک فال). */
    fun requestExtraDraw(activity: Activity) {
        viewModelScope.launch {
            val shown = adManager.showRewarded(activity) {
                viewModelScope.launch { performDraw(bypassCooldown = true) }
            }
            if (!shown) {
                Toast.makeText(activity, "تبلیغ در دسترس نیست — اینترنت را چک کنید", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /** Rewarded hook — skips the 8s repeat cooldown and draws immediately (GOLD: free). */
    fun requestSkipCooldown(activity: Activity) {
        viewModelScope.launch {
            if (supportRepository.tier.first().instantDraw) {
                performDraw(bypassCooldown = true)
            } else {
                val shown = adManager.showRewarded(activity) {
                    viewModelScope.launch { performDraw(bypassCooldown = true) }
                }
                if (!shown) {
                    Toast.makeText(activity, "تبلیغ در دسترس نیست — اینترنت را چک کنید", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private suspend fun performDraw(bypassCooldown: Boolean) {
        val s = _uiState.value
        if (s.busy || s.stage == DrawStage.DRAWING) return
        if (!bypassCooldown && s.cooldownActive) return
        _uiState.update { it.copy(busy = true, stage = DrawStage.DRAWING) }
        Sounds.draw()

        val question = s.question.trim().ifBlank { null }
        val result = drawFal(question, s.category, s.falSource)
        favoriteTargetId.value = result?.poem?.id
        val instant = supportRepository.tier.first().instantDraw
        _uiState.update {
            it.copy(lastDraw = result, dailyFal = null, cooldownActive = !instant, busy = false)
        }
        recomputeRemaining()
        adManager.onDrawCompleted()

        viewModelScope.launch {
            delay(COOLDOWN_MS)
            _uiState.update { it.copy(cooldownActive = false) }
        }
    }

    private suspend fun recomputeRemaining() {
        val subscribed = supportRepository.tier.first().adsRemoved
        val remaining = if (subscribed) {
            Int.MAX_VALUE
        } else {
            val used = drawRepository.countSince(startOfToday())
            (DAILY_FREE_LIMIT - used).coerceAtLeast(0)
        }
        _uiState.update { it.copy(remainingToday = remaining) }
    }

    private fun startOfToday(): Long {
        val c = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return c.timeInMillis
    }

    fun onDrawingFinished() {
        Sounds.reveal()
        _uiState.update { it.copy(stage = DrawStage.REVEAL) }
    }

    fun onReadInterpretation() = _uiState.update { it.copy(stage = DrawStage.INTERPRETATION) }

    fun openSupport() = _uiState.update { it.copy(supportOpen = true) }

    fun closeSupport() {
        _purchasing.value = false
        _uiState.update { it.copy(supportOpen = false) }
    }

    /** خرید حمایت مالی (Poolakey/کافه‌بازار) — تبلیغات را برای همیشه حذف می‌کند. */
    fun purchase(activity: Activity, tier: SupportTier) {
        if (_purchasing.value) return
        _purchasing.value = true
        val started = paymentGateway.purchase(activity, tier) {
            viewModelScope.launch {
                supportRepository.setTier(tier)
                recomputeRemaining()
                _purchasing.value = false
            }
        }
        if (!started) _purchasing.value = false
    }

    /** فالِ روز — deterministic, shared by everyone on the same day. */
    fun openDailyFal() {
        viewModelScope.launch {
            val poem = dailyFal.today()
            if (poem != null) {
                favoriteTargetId.value = poem.id
                _uiState.update { it.copy(dailyFal = poem) }
            }
        }
    }

    fun closeDailyFal() {
        _uiState.update { it.copy(dailyFal = null) }
        favoriteTargetId.value = _uiState.value.lastDraw?.poem?.id
    }

    fun onToggleFavorite() {
        val poemId = favoriteTargetId.value ?: return
        viewModelScope.launch { favoriteRepository.toggle(poemId) }
    }

    fun dismissAndMaybeAd(activity: Activity) {
        _uiState.update { it.copy(stage = DrawStage.NIYYAT, lastDraw = null) }
        favoriteTargetId.value = null
        viewModelScope.launch {
            // با حمایت مالی، هرگز تبلیغ بین‌صفحه‌ای نمایش داده نمی‌شود.
            if (!supportRepository.tier.first().adsRemoved) adManager.showInterstitial(activity)
        }
    }

    fun dismissOnly() {
        _uiState.update { it.copy(stage = DrawStage.NIYYAT, lastDraw = null) }
        favoriteTargetId.value = null
    }
}
