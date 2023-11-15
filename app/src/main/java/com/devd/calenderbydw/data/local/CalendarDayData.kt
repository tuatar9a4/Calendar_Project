package com.devd.calenderbydw.data.local

data class CalendarData(
    val month :Int = -1,
    val dayList :List<CalendarDayData> = listOf(),
)

data class CalendarDayData(
    val day :String="",
    val isHoliday :Boolean = false,
    val holidayName : String? =null
)