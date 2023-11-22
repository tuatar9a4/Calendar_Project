package com.devd.calenderbydw.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.devd.calenderbydw.repository.CalendarDataStore
import com.devd.calenderbydw.repository.CalendarPreferenceInterface
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

val Context.calendarDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "com.hanryubank.fansingapp.datastore"
)

@Module
@InstallIn(SingletonComponent::class)
abstract class DataStoreModule {

    @Binds
    @Singleton
    abstract fun bindCalendarDataStoreRepository(
        calendarDataStore: CalendarDataStore
    ) : CalendarPreferenceInterface

    companion object{
        @Provides
        @Singleton
        fun provideDataStoreRepository(
            @ApplicationContext context :Context
        ):DataStore<Preferences> {
            return context.calendarDataStore
        }
    }
}