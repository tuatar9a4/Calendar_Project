package com.devd.calenderbydw.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.devd.calenderbydw.data.local.entity.HolidayDbEntity

@Dao
interface HolidayDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHolidayItem(holidayItem : HolidayDbEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHolidayItemList(holidayItem : List<HolidayDbEntity>)

    @Query("SELECT * FROM holiday_table WHERE holidayYear LIKE :year")
    suspend fun selectHolidayItemOfYear(year : Int) : List<HolidayDbEntity>

}