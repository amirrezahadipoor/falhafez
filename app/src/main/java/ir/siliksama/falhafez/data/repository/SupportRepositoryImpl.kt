package ir.siliksama.falhafez.data.repository

import ir.siliksama.falhafez.core.util.SupportStore
import ir.siliksama.falhafez.domain.model.SupportTier
import ir.siliksama.falhafez.domain.repository.SettingsRepository
import ir.siliksama.falhafez.domain.repository.SupportRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

/**
 * سطحِ حمایتِ کاربر.
 *
 * ⚠️ نکتهٔ مهم (رفعِ باگ): پیش‌تر اینجا `if (BuildConfig.PREMIUM_UNLOCKED) SupportTier.GOLD`
 * بود. آن پرچم از متغیرِ محیطیِ CI می‌آمد و اگر روی `"true"` تنظیم می‌شد، **هر کاربری**
 * در نسخهٔ منتشرشده سطحِ GOLD می‌گرفت و در نتیجه `adsRemoved == true` می‌شد و
 * **هیچ تبلیغی هرگز نمایش داده نمی‌شد**. این میان‌بُر کاملاً حذف شد؛
 * سطحِ حمایت فقط از خریدِ واقعی (یا بازیابیِ خرید) می‌آید.
 */
class SupportRepositoryImpl @Inject constructor(
    private val settingsRepository: SettingsRepository
) : SupportRepository {

    override val tier: Flow<SupportTier> =
        settingsRepository.supportTier
            .map { SupportTier.fromKey(it) }
            // آینهٔ درون‌حافظه‌ای همیشه هم‌گام بماند تا لایهٔ تبلیغات و رندرِ تصویر
            // بدونِ عبور دادن مقدار از هر صفحه، مقدارِ درست را ببینند.
            .onEach { SupportStore.tier = it }

    override suspend fun setTier(tier: SupportTier) {
        settingsRepository.setSupportTier(tier.key)
        SupportStore.tier = tier
    }

    /** بازگرداندن به حالتِ رایگان — مسیرِ خروجی که قبلاً وجود نداشت. */
    override suspend fun clearTier() {
        settingsRepository.setSupportTier(SupportTier.NONE.key)
        SupportStore.tier = SupportTier.NONE
    }
}
