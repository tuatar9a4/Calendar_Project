package com.devd.calenderbydw.di

import com.devd.calenderbydw.data.local.dao.HolidayDao
import com.devd.calenderbydw.data.remote.api.HolidayService
import com.devd.calenderbydw.repository.HolidayRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class RepositoryModule {

    @Singleton
    @Provides
    fun provideHolidayRetrofit(@NetworkModule.HolidayServer holidayService: HolidayService,holidayDao: HolidayDao):HolidayRepository =
        HolidayRepository(holidayService,holidayDao)

}