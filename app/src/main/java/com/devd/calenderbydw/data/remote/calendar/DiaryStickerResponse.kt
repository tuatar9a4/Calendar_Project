package com.devd.calenderbydw.data.remote.calendar

import com.google.gson.annotations.SerializedName

data class DiaryStickerResponse (
    @SerializedName("stickerName") val stickerName : String,
)