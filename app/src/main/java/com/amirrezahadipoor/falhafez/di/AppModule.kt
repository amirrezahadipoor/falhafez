package com.amirrezahadipoor.falhafez.di

import android.content.Context
import androidx.room.Room
import com.amirrezahadipoor.falhafez.data.local.FalDatabase
import com.amirrezahadipoor.falhafez.data.local.DrawDao
import com.amirrezahadipoor.falhafez.data.local.FavoriteDao
import com.amirrezahadipoor.falhafez.data.local.PoemDao
import com.amirrezahadipoor.falhafez.data.repository.DrawRepositoryImpl
import com.amirrezahadipoor.falhafez.data.repository.FavoriteRepositoryImpl
import com.amirrezahadipoor.falhafez.data.repository.PoemRepositoryImpl
import com.amirrezahadipoor.falhafez.data.repository.SettingsRepositoryImpl
import com.amirrezahadipoor.falhafez.domain.repository.DrawRepository
import com.amirrezahadipoor.falhafez.domain.repository.FavoriteRepository
import com.amirrezahadipoor.falhafez.domain.repository.PoemRepository
import com.amirrezahadipoor.falhafez.domain.repository.SettingsRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): FalDatabase =
        Room.databaseBuilder(context, FalDatabase::class.java, "falhafez.db")
            .build()

    @Provides
    fun providePoemDao(db: FalDatabase): PoemDao = db.poemDao()

    @Provides
    fun provideDrawDao(db: FalDatabase): DrawDao = db.drawDao()

    @Provides
    fun provideFavoriteDao(db: FalDatabase): FavoriteDao = db.favoriteDao()

    @Provides
    @Singleton
    fun providePoemRepository(dao: PoemDao): PoemRepository = PoemRepositoryImpl(dao)

    @Provides
    @Singleton
    fun provideDrawRepository(drawDao: DrawDao, poemDao: PoemDao): DrawRepository =
        DrawRepositoryImpl(drawDao, poemDao)

    @Provides
    @Singleton
    fun provideFavoriteRepository(favoriteDao: FavoriteDao, poemDao: PoemDao): FavoriteRepository =
        FavoriteRepositoryImpl(favoriteDao, poemDao)

    @Provides
    @Singleton
    fun provideSettingsRepository(store: com.amirrezahadipoor.falhafez.data.settings.SettingsDataStore): SettingsRepository =
        SettingsRepositoryImpl(store)
}
