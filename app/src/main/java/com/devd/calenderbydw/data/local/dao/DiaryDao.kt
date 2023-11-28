package com.devd.calenderbydw.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.devd.calenderbydw.data.local.entity.DiaryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DiaryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDairy(item : DiaryEntity) :Long

    @Query("Select * from diary_table ORDER BY createDate DESC LIMIT :loadSize OFFSET :index * :loadSize")
    suspend fun getDiaryDataList(index:Int,loadSize :Int) : List<DiaryEntity>

}