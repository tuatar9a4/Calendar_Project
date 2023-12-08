package com.devd.calenderbydw.di

import com.devd.calenderbydw.data.remote.api.CalendarService
import com.devd.calenderbydw.data.remote.api.HolidayService
import com.devd.calenderbydw.utils.ConstVariable.CALENDAR_API_URL
import com.devd.calenderbydw.utils.ConstVariable.HOLIDAY_API_URL
import com.tickaroo.tikxml.TikXml
import com.tickaroo.tikxml.retrofit.TikXmlConverterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create
import java.util.concurrent.TimeUnit
import javax.inject.Qualifier
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class NetworkModule {

    @Qualifier
    @Retention(AnnotationRetention.BINARY)
    annotation class HolidayServer

    @Qualifier
    @Retention(AnnotationRetention.BINARY)
    annotation class CalendarServer

    @HolidayServer
    @Provides
    fun provideHolidayApiUrl() = HOLIDAY_API_URL

    @CalendarServer
    @Provides
    fun provideCalendarApiUrl() = CALENDAR_API_URL

    @Singleton
    @Provides
    fun provideOkhttpClient() : OkHttpClient {
        val httpLoginInterceptor = HttpLoggingInterceptor()
            .setLevel(HttpLoggingInterceptor.Level.BODY)
        return OkHttpClient.Builder()
            .addInterceptor(httpLoginInterceptor)
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .protocols(listOf(Protocol.HTTP_2,Protocol.HTTP_1_1))
            .build()
    }


    @HolidayServer
    @Singleton
    @Provides
    fun provideRetrofit(okHttpClient: OkHttpClient,@HolidayServer holidayUrl : String) :Retrofit{
        val parser = TikXml.Builder().exceptionOnUnreadXml(false).build()
        return Retrofit.Builder()
            .addConverterFactory(TikXmlConverterFactory.create(parser))
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpClient)
            .baseUrl(holidayUrl)
            .build()
    }

    @CalendarServer
    @Singleton
    @Provides
    fun provideCalendarRetrofit(okHttpClient: OkHttpClient,@CalendarServer calendarUrl : String) :Retrofit{
        val parser = TikXml.Builder().exceptionOnUnreadXml(false).build()
        return Retrofit.Builder()
            .addConverterFactory(TikXmlConverterFactory.create(parser))
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpClient)
            .baseUrl(calendarUrl)
            .build()
    }

    @HolidayServer
    @Singleton
    @Provides
    fun provideHolidayService(@HolidayServer retrofit: Retrofit): HolidayService =
        retrofit.create(HolidayService::class.java)

    @CalendarServer
    @Singleton
    @Provides
    fun provideCalendarService(@CalendarServer retrofit: Retrofit) : CalendarService =
        retrofit.create(CalendarService::class.java)


}