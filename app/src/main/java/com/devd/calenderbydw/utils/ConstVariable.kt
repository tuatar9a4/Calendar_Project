package com.devd.calenderbydw.utils

import android.graphics.Color
import androidx.annotation.ColorInt
import androidx.annotation.IntRange
import org.joda.time.DateTime
import org.joda.time.DateTimeConstants

object ConstVariable {

    const val HOLIDAY_API_URL = "http://apis.data.go.kr/"
    const val CALENDAR_API_URL = "http://193.123.232.164:8087/"

    const val WEEK_SUN_DAY = 1
    const val WEEK_MON_DAY = 2
    const val WEEK_TUE_DAY = 3
    const val WEEK_WEN_DAY = 4
    const val WEEK_THU_DAY = 5
    const val WEEK_FRI_DAY = 6
    const val WEEK_SAT_DAY = 7

    const val ERROR_UK1001 = "UK1001"


    const val WEEKS_PER_MONTH = 6

    const val CREATE_TASK = 0
    const val MODIFY_TASK = 1

    const val WIDGET_SHOW_DATE= "showDate"

    const val HAPPY = "icon_sticker_happy.png"
    val STICKERS_TEMP_LIST = listOf<String>(
        "icon_sticker_happy.png"
    )
    const val WEATHER_TYPE_SUNNY = 0
    const val WEATHER_TYPE_SUNNY_TXT = "맑음"
    const val WEATHER_TYPE_CLOUDY = 1
    const val WEATHER_TYPE_CLOUDY_TXT = "흐림"
    const val WEATHER_TYPE_RAIN = 2
    const val WEATHER_TYPE_RAIN_TXT = "비"
    const val WEATHER_TYPE_SOSO = 3
    const val WEATHER_TYPE_SOSO_TXT = "눈"
    const val WEATHER_TYPE_ETC = 4
    const val FEEL_TYPE_GOOD = 0
    const val FEEL_TYPE_GOOD_TXT = "좋은"
    const val FEEL_TYPE_BAD = 1
    const val FEEL_TYPE_BAD_TXT = "나쁜"
    const val FEEL_TYPE_SOSO = 2
    const val FEEL_TYPE_SOSO_TXT = "그저 그런"
    const val FEEL_TYPE_HAPPY = 3
    const val FEEL_TYPE_HAPPY_TXT = "행복한"
    const val FEEL_TYPE_ETC = 4

}