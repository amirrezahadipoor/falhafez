package ir.siliksama.falhafez.presentation.home

import android.app.Activity
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ir.siliksama.falhafez.core.sound.Sounds
import ir.siliksama.falhafez.core.util.SupportStore
import ir.siliksama.falhafez.core.theme.FalThemeId
import ir.siliksama.falhafez.data.ads.AdManager
import ir.siliksama.falhafez.data.payments.PaymentGateway
import ir.siliksama.falhafez.domain.model.DrawEntry
import ir.siliksama.falhafez.domain.model.FalCategory
import ir.siliksama.falhafez.domain.model.ChannelInfo
import ir.siliksama.falhafez.domain.model.DrawAccess
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
import ir.siliksama.falhafez.domain.usecase.PersonalizeTafsir
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

/**
 * دو فالِ رایگان در هر روز.
 * پس از آن، فالِ بیشتر با «گشودنِ فال» ممکن است (پشتِ صحنه: ویدیوی جایزه‌ای).
 * آفلاین و حمایت‌کننده: نامحدود و بدونِ هیچ شرطی.
 */
const val DAILY_FREE_LIMIT = 2
private const val COOLDOWN_MS = 8_000L

enum class DrawStage { NIYYAT, DRAWING, REVEAL, INTERPRETATION }

data class HomeUiState(
    val question: String = "",
    val category: FalCategory = FalCategory.NONE,
    val falSource: Poet? = Poet.HAFEZ,
    val sourceCount: Int = 495,
    val stage: DrawStage = DrawStage.NIYYAT,
    val lastDraw: DrawEntry? = null,
    /** تفسیرِ شخصی‌شدهٔ فالِ جاری — معنای اصیلِ شعر به‌علاوهٔ قابِ متناسب با نیّت. */
    val personalTafsir: String? = null,
    val dailyFal: Poem? = null,
    /**
     * تفسیرِ فالِ روز، با قابِ متناسبِ همان روز. بذر عددِ روز است، پس متن در تمامِ
     * ۲۴ ساعت ثابت می‌ماند و فردا عوض می‌شود — مثل خودِ غزلِ روز.
     */
    val dailyTafsir: String? = null,
    val cooldownActive: Boolean = false,
    val remainingToday: Int = DAILY_FREE_LIMIT,
    val access: DrawAccess = DrawAccess.FREE_QUOTA,
    val busy: Boolean = false,
    val supportOpen: Boolean = false
) {
    /** آفلاین یا حمایت‌کننده → هیچ سقفی در کار نیست. */
    val unlimited: Boolean get() = access.isUnlimited

    val canDraw: Boolean
        get() = !busy && stage != DrawStage.DRAWING && !cooldownActive &&
            (unlimited || remainingToday > 0)

    /** سهمیه تمام شده و کاربر باید فال را «بگشاید». */
    val needsUnlock: Boolean get() = access == DrawAccess.NEEDS_UNLOCK

    /** آیا اصلاً نشانگرِ سهمیه را نشان بدهیم؟ */
    val showsQuota: Boolean get() = !unlimited
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
    private val personalizeTafsir: PersonalizeTafsir,
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
            // سطحِ حمایت را پیش از هر چیز بخوان تا لایهٔ تبلیغات با مقدارِ درست گرم شود.
            SupportStore.tier = supportRepository.tier.first()
            recomputeRemaining()

            // گرم‌کردنِ تبلیغات فقط پس از دانستنِ وضعیتِ اشتراک (رفعِ شرطِ رقابتی).
            adManager.warmUp()

            counts = runCatching {
                Poet.falSources.associateWith { poemRepository.countForPoet(it) }
            }.getOrDefault(emptyMap())
            _uiState.update { it.copy(sourceCount = counts[it.falSource]?.takeIf { c -> c > 0 } ?: 495) }
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
            counts = runCatching {
                Poet.falSources.associateWith { poemRepository.countForPoet(it) }
            }.getOrDefault(counts)
            val count = when (source) {
                null -> counts.values.sum()
                else -> counts[source] ?: 0
            }
            _uiState.update { it.copy(falSource = source, sourceCount = count) }
        }
    }

    /**
     * تازه‌سازیِ سهمیه هنگام برگشتِ اپ به پیش‌زمینه.
     * مهم است چون هم روز ممکن است عوض شده باشد و هم وضعیتِ شبکه
     * (آفلاین→آنلاین) که مستقیماً حالتِ دسترسی را تغییر می‌دهد.
     */
    fun refreshQuota() {
        viewModelScope.launch { recomputeRemaining() }
    }

    fun draw() {
        val s = _uiState.value
        if (!s.canDraw) return
        viewModelScope.launch { performDraw(bypassCooldown = false) }
    }

    /**
     * «گشودنِ فالِ دیگر» پس از پایانِ سهمیهٔ روزانه.
     *
     * منطق به ترتیبِ اولویت:
     *  1. حمایت‌کننده یا آفلاین → مستقیم فال می‌گیرد (اصلاً به اینجا نمی‌رسد، ولی محکم‌کاری).
     *  2. آنلاین → ویدیوی جایزه‌ای؛ فال پس از دریافتِ پاداش باز می‌شود.
     *  3. اگر تبلیغی در دسترس نبود → **فال را به کاربر می‌دهیم.**
     *     نبودنِ موجودیِ تبلیغ مشکلِ ماست، نه کاربر؛ او نباید پشتِ درِ بسته بماند.
     */
    fun requestExtraDraw(activity: Activity) {
        viewModelScope.launch {
            if (currentAccess().isUnlimited) {
                performDraw(bypassCooldown = true)
                return@launch
            }
            val shown = adManager.showRewarded(activity) {
                viewModelScope.launch { grantUnlockedDraw() }
            }
            if (!shown) {
                // هیچ تبلیغی نبود → فال را رایگان باز کن.
                grantUnlockedDraw()
            }
        }
    }

    /** رد کردنِ درنگِ کوتاهِ بینِ دو فال. */
    fun requestSkipCooldown(activity: Activity) {
        viewModelScope.launch {
            val access = currentAccess()
            if (access.isUnlimited || supportRepository.tier.first().instantDraw) {
                performDraw(bypassCooldown = true)
                return@launch
            }
            val shown = adManager.showRewarded(activity) {
                viewModelScope.launch { performDraw(bypassCooldown = true) }
            }
            if (!shown) performDraw(bypassCooldown = true)
        }
    }

    /** یک فالِ اضافه که خارج از سهمیه حساب می‌شود. */
    private suspend fun grantUnlockedDraw() {
        bonusDraws += 1
        performDraw(bypassCooldown = true)
    }

    private suspend fun performDraw(bypassCooldown: Boolean) {
        val s = _uiState.value
        if (s.busy || s.stage == DrawStage.DRAWING) return
        if (!bypassCooldown && s.cooldownActive) return
        _uiState.update { it.copy(busy = true, stage = DrawStage.DRAWING) }
        Sounds.draw()

        val question = s.question.trim().ifBlank { null }
        // حفاظِ کراش: اگر فال به هر دلیلی ساخته نشد، بی‌صدا به حالتِ نیّت برمی‌گردیم.
        val result = runCatching { drawFal(question, s.category, s.falSource) }.getOrNull()
        if (result == null) {
            _uiState.update { it.copy(busy = false, stage = DrawStage.NIYYAT) }
            return
        }
        favoriteTargetId.value = result.poem.id
        val instant = supportRepository.tier.first().instantDraw

        // تفسیر را به نیّت و دستهٔ کاربر گره می‌زنیم — بدونِ تغییرِ معنای اصیلِ شعر.
        val personal = runCatching {
            personalizeTafsir(
                poem = result.poem,
                question = result.question,
                category = result.category,
                seed = result.id
            )
        }.getOrNull()

        _uiState.update {
            it.copy(
                lastDraw = result,
                personalTafsir = personal,
                dailyFal = null,
                dailyTafsir = null,
                cooldownActive = !instant,
                busy = false
            )
        }
        recomputeRemaining()
        adManager.onDrawCompleted()

        viewModelScope.launch {
            delay(COOLDOWN_MS)
            _uiState.update { it.copy(cooldownActive = false) }
        }
    }

    /**
     * فال‌هایی که با «گشودن» گرفته شده‌اند و نباید از سهمیهٔ رایگان کم شوند.
     * فقط در طولِ همین نشست معتبر است (با بستنِ اپ صفر می‌شود).
     */
    private var bonusDraws: Int = 0

    /** وضعیتِ فعلیِ دسترسی: حمایت < آفلاین < سهمیه < نیاز به گشودن. */
    private suspend fun currentAccess(): DrawAccess {
        if (supportRepository.tier.first().adsRemoved) return DrawAccess.UNLIMITED_SUPPORTER
        // آفلاین = نامحدود. دیوان روی خودِ دستگاه است؛ نبودِ اینترنت نباید فال را ببندد.
        if (!adManager.isNetworkAvailable()) return DrawAccess.UNLIMITED_OFFLINE
        val used = (drawRepository.countSince(startOfToday()) - bonusDraws).coerceAtLeast(0)
        return if (used < DAILY_FREE_LIMIT) DrawAccess.FREE_QUOTA else DrawAccess.NEEDS_UNLOCK
    }

    private suspend fun recomputeRemaining() {
        val access = currentAccess()
        val remaining = if (access.isUnlimited) {
            Int.MAX_VALUE
        } else {
            val used = (drawRepository.countSince(startOfToday()) - bonusDraws).coerceAtLeast(0)
            (DAILY_FREE_LIMIT - used).coerceAtLeast(0)
        }
        _uiState.update { it.copy(remainingToday = remaining, access = access) }
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

    /**
     * بازگشتِ سیستم (دکمه/حرکتِ بازگشت گوشی) — بدون تبلیغ.
     * ترتیب: دیالوگِ حمایت ← فالِ روز ← تفسیر/رونمایی. true یعنی هندل شد.
     */
    fun onSystemBack(): Boolean {
        val s = _uiState.value
        return when {
            s.supportOpen -> {
                closeSupport()
                true
            }
            s.dailyFal != null -> {
                closeDailyFal()
                true
            }
            s.stage == DrawStage.INTERPRETATION || s.stage == DrawStage.REVEAL -> {
                _uiState.update { it.copy(stage = DrawStage.NIYYAT, lastDraw = null, personalTafsir = null) }
                favoriteTargetId.value = null
                true
            }
            // در حین انیمیشنِ گشودن دیوان، بازگشت را می‌بلعیم تا اپ بسته نشود.
            s.stage == DrawStage.DRAWING -> true
            else -> false
        }
    }

    fun closeSupport() {
        _purchasing.value = false
        _uiState.update { it.copy(supportOpen = false) }
    }

    /**
     * بازیابیِ خریدِ پیشین از کافه‌بازار.
     * هنگام شروعِ اپ و نیز با دکمهٔ «بازیابی خرید» صدا زده می‌شود؛ هم حمایتِ ازدست‌رفته را
     * برمی‌گرداند و هم سطحِ نادرستِ باقی‌مانده روی دستگاه را اصلاح می‌کند.
     */
    fun restorePurchases(context: android.content.Context, notify: Boolean = false) {
        paymentGateway.restorePurchases(context) { restored ->
            viewModelScope.launch {
                when {
                    // null یعنی «نتوانستیم بررسی کنیم» → وضعیت را دست نمی‌زنیم.
                    restored == null ->
                        if (notify) Toast.makeText(context, "بررسی خرید ممکن نشد", Toast.LENGTH_SHORT).show()

                    restored == SupportTier.NONE -> {
                        if (supportRepository.tier.first() != SupportTier.NONE) {
                            supportRepository.clearTier()
                            recomputeRemaining()
                        }
                        if (notify) Toast.makeText(context, "خریدی یافت نشد", Toast.LENGTH_SHORT).show()
                    }

                    else -> {
                        supportRepository.setTier(restored)
                        recomputeRemaining()
                        if (notify) {
                            Toast.makeText(context, "حمایتِ شما بازیابی شد: ${restored.faName}", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }
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
                // فالِ روز پرسشی ندارد (برای همه یکی است)، پس قاب از روی عددِ روز
                // ساخته می‌شود؛ نه از روی نیّتِ شخصی.
                val personal = runCatching {
                    personalizeTafsir(
                        poem = poem,
                        question = null,
                        category = FalCategory.NONE,
                        seed = DailyFalUseCase.todayDayNumber()
                    )
                }.getOrNull()
                _uiState.update { it.copy(dailyFal = poem, dailyTafsir = personal) }
            }
        }
    }

    fun closeDailyFal() {
        _uiState.update { it.copy(dailyFal = null, dailyTafsir = null) }
        favoriteTargetId.value = _uiState.value.lastDraw?.poem?.id
    }

    fun onToggleFavorite() {
        val poemId = favoriteTargetId.value ?: return
        viewModelScope.launch { favoriteRepository.toggle(poemId) }
    }

    fun dismissAndMaybeAd(activity: Activity) {
        _uiState.update { it.copy(stage = DrawStage.NIYYAT, lastDraw = null, personalTafsir = null) }
        favoriteTargetId.value = null
        viewModelScope.launch {
            // تبلیغِ بین‌صفحه‌ای فقط وقتی: حمایت‌کننده نیست و آنلاین است.
            // (AdManager هم خودش دوباره همین دو شرط را بررسی می‌کند — دفاع در عمق.)
            if (currentAccess().adsApply) adManager.showInterstitial(activity)
        }
    }

    fun dismissOnly() {
        _uiState.update { it.copy(stage = DrawStage.NIYYAT, lastDraw = null, personalTafsir = null) }
        favoriteTargetId.value = null
    }
}
