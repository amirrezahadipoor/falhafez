package ir.siliksama.falhafez.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import ir.siliksama.falhafez.data.ads.AdManager
import ir.siliksama.falhafez.data.ads.AdiveryAdManager
import ir.siliksama.falhafez.data.ads.TapsellAdManager
import ir.siliksama.falhafez.data.ads.WaterfallAdManager
import javax.inject.Named
import javax.inject.Singleton

/**
 * دو شبکهٔ تبلیغاتی داریم — تپ‌سل و ادیوری (سرویسِ نمایشِ یکتانت) — و یک
 * [WaterfallAdManager] که بینشان آبشار می‌سازد.
 *
 * UI همچنان فقط [AdManager] را می‌بیند و از وجودِ دو شبکه بی‌خبر است؛ افزودن یا
 * حذفِ شبکه فقط همین فایل را عوض می‌کند.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class AdModule {

    @Binds
    @Singleton
    @Named("tapsell")
    abstract fun bindTapsell(impl: TapsellAdManager): AdManager

    @Binds
    @Singleton
    @Named("adivery")
    abstract fun bindAdivery(impl: AdiveryAdManager): AdManager

    /** آنچه بقیهٔ اپ تزریق می‌کند. */
    @Binds
    @Singleton
    abstract fun bindAdManager(impl: WaterfallAdManager): AdManager
}
