package com.devd.calenderbydw.data.remote.api

import com.devd.calenderbydw.data.remote.calendar.DiaryStickerResponse
import retrofit2.http.GET

interface CalendarService {

    @GET("calendar/diary/sticker")
    suspend fun getDiaryStickerList() : List<DiaryStickerResponse>
}