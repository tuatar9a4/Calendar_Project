package com.devd.calenderbydw.data.local.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.devd.calenderbydw.data.remote.holiday.HolidayItem

@Entity(tableName = "holiday_table")
data class HolidayDbData(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "holidayYearToday")
    val holidayYearToday : Int,
    @ColumnInfo(name = "holidayName")
    val holidayName : String,
    @ColumnInfo(name = "holidayYear")
    val holidayYear : String,
    @ColumnInfo(name = "isHolidayRest")
    val isHolidayRest : Boolean,
){
    fun toHolidayItem()=HolidayItem(
        "1",
        holidayName,
        if(isHolidayRest) "Y" else "N",
        holidayYearToday.toString()
    )
}
