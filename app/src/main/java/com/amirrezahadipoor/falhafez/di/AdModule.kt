package com.amirrezahadipoor.falhafez.di

import com.amirrezahadipoor.falhafez.data.ads.AdManager
import com.amirrezahadipoor.falhafez.data.ads.AdMobAdManager
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
