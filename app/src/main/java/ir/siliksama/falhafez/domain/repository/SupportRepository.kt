package ir.siliksama.falhafez.domain.repository

import ir.siliksama.falhafez.domain.model.SupportTier
import kotlinx.coroutines.flow.Flow

interface SupportRepository {
    val tier: Flow<SupportTier>
    suspend fun setTier(tier: SupportTier)

    /**
     * پاک‌کردنِ سطحِ حمایت (بازگشت به رایگان).
     * بدونِ این مسیر، هر دستگاهی که یک‌بار سطحِ حمایت گرفته بود — چه با خریدِ آزمایشی
     * و چه با بیلدِ debug — برای همیشه «مشترک» می‌ماند و تبلیغاتش خاموش می‌شد.
     */
    suspend fun clearTier()
}
