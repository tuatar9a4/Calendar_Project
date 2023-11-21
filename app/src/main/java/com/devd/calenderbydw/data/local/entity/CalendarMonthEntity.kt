package com.devd.calenderbydw.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import com.google.gson.Gson

@Entity(
    indices = [Index(value = ["year","month"])],
    tableName = "calendar_table"
)
data class CalendarMonthEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo("id")
    val id: Int = 0,
    @ColumnInfo("year")
    val year: Int = -1,
    @ColumnInfo("month")
    val month: Int = -1,
    @ColumnInfo("dayList")
    val dayList: List<CalendarDayEntity> = listOf(),
){
}

/**
 * weekCount : 일:1 ~ 토:7
 */

data class CalendarDayEntity(
    @ColumnInfo("year")
    val year: String = "",
    @ColumnInfo("month")
    val month: String = "",
    @ColumnInfo("day")
    val day: String = "",
    @ColumnInfo("isCurrentMonth")
    val isCurrentMonth: Boolean = false,
    @ColumnInfo("weekCount")
    val weekCount: Int = -1,
    @ColumnInfo("toDay")
    var toDay: Boolean = false,
    @ColumnInfo("isHoliday")
    var isHoliday: Boolean = false,
    @ColumnInfo("holidayName")
    var holidayName: String? = null,
    @ColumnInfo("existsTask")
    var existsTask: Boolean = false,
    @ColumnInfo("taskTitle")
    var taskTitle: String = "",
    @ColumnInfo("stickerId")
    var stickerId: String = "",
    @ColumnInfo("dateTimeLong")
    var dateTimeLong: Long = 0L
)

class CalendarDayConverters{
    @TypeConverter
    fun fromCalendarDayEntityHolder(array : List<CalendarDayEntity>) : String{
        return Gson().toJson(array)
    }

    @TypeConverter
    fun toCalendarDayEntityHolder(str: String): List<CalendarDayEntity> {
        return Gson().fromJson(str, Array<CalendarDayEntity>::class.java).toList()
    }
}