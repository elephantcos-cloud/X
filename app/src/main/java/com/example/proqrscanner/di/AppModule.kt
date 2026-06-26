package com.example.proqrscanner.di

import android.content.Context
import com.example.proqrscanner.data.repository.HistoryRepository
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
    fun provideHistoryRepository(@ApplicationContext context: Context): HistoryRepository {
        return HistoryRepository(context)
    }
}
