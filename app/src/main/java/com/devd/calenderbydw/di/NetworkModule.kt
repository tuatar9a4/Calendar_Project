package com.devd.calenderbydw.di

import com.devd.calenderbydw.data.remote.api.HolidayService
import com.devd.calenderbydw.utils.ConstVariable.HOLIDAY_API_URL
import com.google.gson.GsonBuilder
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
import javax.inject.Qualifier
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class NetworkModule {

    @Qualifier
    @Retention(AnnotationRetention.BINARY)
    annotation class HolidayServer

    @HolidayServer
    @Provides
    fun provideHolidayApiUrl() = HOLIDAY_API_URL

    @Singleton
    @Provides
    fun provideOkhttpClient() : OkHttpClient {
        val httpLoginInterceptor = HttpLoggingInterceptor()
            .setLevel(HttpLoggingInterceptor.Level.BODY)
        return OkHttpClient.Builder()
            .addInterceptor(httpLoginInterceptor)
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

    @HolidayServer
    @Singleton
    @Provides
    fun provideHolidayService(@HolidayServer retrofit: Retrofit): HolidayService =
        retrofit.create(HolidayService::class.java)

}