package com.devd.calenderbydw.data.remote.api

import com.devd.calenderbydw.data.remote.holiday.HolidayResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface HolidayService {

    @GET("B090041/openapi/service/SpcdeInfoService/getRestDeInfo")
    suspend fun getHolidayOfMonth(
        @Query("serviceKey", encoded = true) serviceKey: String,
        @Query("solYear") year: Int,
        @Query("numOfRows") numOfRows: Int = 100,
    ): HolidayResponse

}