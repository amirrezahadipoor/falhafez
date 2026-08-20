package ir.falhafez.tabir.di

import ir.falhafez.tabir.data.ads.AdManager
import ir.falhafez.tabir.data.ads.AdMobAdManager
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Swapping the ad network (or adding Tapsell mediation) is a one-line change here.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class AdModule {

    @Binds
    @Singleton
    abstract fun bindAdManager(impl: AdMobAdManager): AdManager
}
