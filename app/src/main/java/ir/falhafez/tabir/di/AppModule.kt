package ir.falhafez.tabir.di

import android.content.Context
import androidx.room.Room
import ir.falhafez.tabir.data.local.FalDatabase
import ir.falhafez.tabir.data.local.DrawDao
import ir.falhafez.tabir.data.local.FavoriteDao
import ir.falhafez.tabir.data.local.PoemDao
import ir.falhafez.tabir.data.repository.DrawRepositoryImpl
import ir.falhafez.tabir.data.repository.FavoriteRepositoryImpl
import ir.falhafez.tabir.data.repository.PoemRepositoryImpl
import ir.falhafez.tabir.data.repository.SettingsRepositoryImpl
import ir.falhafez.tabir.domain.repository.DrawRepository
import ir.falhafez.tabir.domain.repository.FavoriteRepository
import ir.falhafez.tabir.domain.repository.PoemRepository
import ir.falhafez.tabir.domain.repository.SettingsRepository
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
            .addMigrations(FalDatabase.MIGRATION_1_2)
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
    fun provideSettingsRepository(store: ir.falhafez.tabir.data.settings.SettingsDataStore): SettingsRepository =
        SettingsRepositoryImpl(store)
}
