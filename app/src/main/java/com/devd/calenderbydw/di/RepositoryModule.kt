package com.devd.calenderbydw.di

import com.devd.calenderbydw.data.local.dao.CalendarDao
import com.devd.calenderbydw.data.local.dao.HolidayDao
import com.devd.calenderbydw.data.local.dao.TaskDao
import com.devd.calenderbydw.data.remote.api.HolidayService
import com.devd.calenderbydw.repository.CalendarRepository
import com.devd.calenderbydw.repository.HolidayRepository
import com.devd.calenderbydw.repository.TaskRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import java.util.Calendar
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class RepositoryModule {

    //todo 공휴일 통합 앱개발 완성까지 사용하는 곳이 없다면 제거할 것!
    @Singleton
    @Provides
    fun provideHolidayRetrofit(@NetworkModule.HolidayServer holidayService: HolidayService,holidayDao: HolidayDao):HolidayRepository =
        HolidayRepository(holidayService,holidayDao)

    @Singleton
    @Provides
    fun provideCalendarRetrofit(@NetworkModule.HolidayServer holidayService: HolidayService,holidayDao: HolidayDao,calendarDao: CalendarDao): CalendarRepository =
        CalendarRepository(holidayService,holidayDao,calendarDao)

    @Singleton
    @Provides
    fun provideTaskRepository(taskDao: TaskDao) : TaskRepository =
        TaskRepository(taskDao)
}