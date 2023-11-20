package com.devd.calenderbydw.data.local.calendar

import com.devd.calenderbydw.data.local.entity.CalendarDayEntity

data class CalendarData(
    val year :Int = -1,
    val month :Int = -1,
    val dayList :List<CalendarDayData> = listOf(),
)

/**
 * weekCount : 일:1 ~ 토:7
 */
data class CalendarDayData(
    val year: String = "",
    val month: String = "",
    val day: String = "",
    val isCurrentMonth: Boolean = false,
    val weekCount: Int = -1,
    var toDay: Boolean = false,
    var isHoliday: Boolean = false,
    var holidayName: String? = null,
    var existsTask: Boolean = false,
    var dateTimeLong :Long =0L
){
    fun toCalendarDBEntity() = CalendarDayEntity(
        year = year,
        month = month,
        day = day,
        isCurrentMonth = isCurrentMonth,
        weekCount = weekCount,
        toDay = toDay,
        isHoliday = isHoliday,
        holidayName = holidayName,
        existsTask = existsTask,
        dateTimeLong = dateTimeLong,
    )
}