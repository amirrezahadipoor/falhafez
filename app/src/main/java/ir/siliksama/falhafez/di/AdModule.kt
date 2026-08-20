package ir.siliksama.falhafez.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import ir.siliksama.falhafez.data.ads.AdManager
import ir.siliksama.falhafez.data.ads.TapsellAdManager
import javax.inject.Singleton

/**
 * شبکهٔ تبلیغات = تپسل. تعویض/مدیتیشن فقط اینجا عوض می‌شود، نه در UI.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class AdModule {

    @Binds
    @Singleton
    abstract fun bindAdManager(impl: TapsellAdManager): AdManager
}
