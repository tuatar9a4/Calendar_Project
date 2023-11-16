package com.devd.calenderbydw.di

import android.content.Context
import com.devd.calenderbydw.data.local.AppDatabase
import com.devd.calenderbydw.data.local.dao.HolidayDao
import com.devd.calenderbydw.data.local.dao.TaskDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class DatabaseModule {

    @Singleton
    @Provides
    fun provideAppDatabase(@ApplicationContext context: Context) : AppDatabase =
        AppDatabase.buildDatabase(context)


    @Provides
    fun provideHolidayDao(appDatabase: AppDatabase) : HolidayDao =
        appDatabase.holidayDao()

    @Provides
    fun provideTaskDao(appDatabase: AppDatabase) : TaskDao =
        appDatabase.taskDao()
}