package com.devd.calenderbydw.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.devd.calenderbydw.data.local.entity.TaskDBEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {

    @Insert
    suspend fun insertTaskItem(item : TaskDBEntity) : Long


    @Query("SELECT * FROM task_table WHERE year LIKE :year AND month LIKE :month AND day LIKE :day")
    fun getTaskSpecifyDay(year:Int,month:Int,day:Int) : Flow<List<TaskDBEntity>>

    @Query("SELECT * FROM task_table")
    suspend fun getAllTask() :List<TaskDBEntity>


}