package ir.siliksama.falhafez.di

import android.content.Context
import androidx.room.Room
import ir.siliksama.falhafez.data.local.FalDatabase
import ir.siliksama.falhafez.data.local.DrawDao
import ir.siliksama.falhafez.data.local.FavoriteDao
import ir.siliksama.falhafez.data.local.PoemDao
import ir.siliksama.falhafez.data.repository.DrawRepositoryImpl
import ir.siliksama.falhafez.data.repository.FavoriteRepositoryImpl
import ir.siliksama.falhafez.data.repository.PoemRepositoryImpl
import ir.siliksama.falhafez.data.payments.BazaarPaymentGateway
import ir.siliksama.falhafez.data.payments.PaymentGateway
import ir.siliksama.falhafez.data.repository.SettingsRepositoryImpl
import ir.siliksama.falhafez.data.repository.SupportRepositoryImpl
import ir.siliksama.falhafez.domain.repository.DrawRepository
import ir.siliksama.falhafez.domain.repository.FavoriteRepository
import ir.siliksama.falhafez.domain.repository.PoemRepository
import ir.siliksama.falhafez.domain.repository.SettingsRepository
import ir.siliksama.falhafez.domain.repository.SupportRepository
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
    fun provideSettingsRepository(store: ir.siliksama.falhafez.data.settings.SettingsDataStore): SettingsRepository =
        SettingsRepositoryImpl(store)
}
