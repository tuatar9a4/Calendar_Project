package com.devd.calenderbydw.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.devd.calenderbydw.data.local.db.HolidayDbData

@Dao
interface HolidayDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHolidayItem(holidayItem : HolidayDbData)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHolidayItemList(holidayItem : List<HolidayDbData>)

    @Query("SELECT * FROM holiday_table WHERE holidayYear LIKE :year")
    suspend fun selectHolidayItemOfYear(year : Int) : List<HolidayDbData>

}