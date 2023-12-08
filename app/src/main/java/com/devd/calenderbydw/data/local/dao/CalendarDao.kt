package com.devd.calenderbydw.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.devd.calenderbydw.data.local.entity.CalendarMonthEntity

@Dao
interface CalendarDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCalendarItemList(holidayItem : List<CalendarMonthEntity>) : List<Long>

    @Query("SELECT * FROM calendar_table WHERE year >= :startYear AND year <= :endYear")
    suspend fun getAllCalendarData(startYear:Int,endYear:Int) : List<CalendarMonthEntity>

    @Query("SELECT * FROM calendar_table WHERE year >= :startYear AND year <= :endYear")
    fun getAllCalendarData22(startYear:Int,endYear:Int) : List<CalendarMonthEntity>

    @Query("SELECT SUM(LENGTH('id')) FROM calendar_table ")
    suspend fun getCalendarDataSize() : Long?

    @Query("Select * from calendar_table")
    suspend fun getAllCalendar() : List<CalendarMonthEntity>

    @Update
    suspend fun updateCalendarDataAddHoliday(list: List<CalendarMonthEntity>)
}