package com.devd.calenderbydw.di

import android.content.Context
import com.devd.calenderbydw.data.local.AppDatabase
import com.devd.calenderbydw.data.local.dao.CalendarDao
import com.devd.calenderbydw.data.local.dao.DiaryDao
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


    @Singleton
    @Provides
    fun provideHolidayDao(appDatabase: AppDatabase) : HolidayDao =
        appDatabase.holidayDao()

    @Singleton
    @Provides
    fun provideTaskDao(appDatabase: AppDatabase) : TaskDao =
        appDatabase.taskDao()

    @Singleton
    @Provides
    fun provideCalendarDao(appDatabase: AppDatabase) : CalendarDao =
        appDatabase.calendarDao()

    @Singleton
    @Provides
    fun provideDiaryDao(appDatabase: AppDatabase) : DiaryDao =
        appDatabase.diaryDao()
}