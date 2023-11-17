package com.devd.calenderbydw.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.devd.calenderbydw.data.local.entity.TaskDBEntity
import kotlinx.coroutines.flow.Flow

/**
 * repeatType 0:반복 없음,1: 매일, 2:매주, 3:매달, 4:매년
 */
@Dao
interface TaskDao {

    @Insert
    suspend fun insertTaskItem(item : TaskDBEntity) : Long
    //반복이 없는 특정 날의 일정
    @Query("SELECT * FROM task_table WHERE year LIKE :year AND month LIKE :month AND day LIKE :day AND repeatType LIKE 0 AND createDate < :searchDate")
    suspend fun getTaskSpecifyDay(year:String,month:String,day:String,searchDate:Long) : List<TaskDBEntity>
    //매년 반복 하는 특정 날의 일정
    @Query("SELECT * FROM task_table WHERE month LIKE :month AND day LIKE :day AND repeatType LIKE 4 AND createDate < :searchDate")
    suspend fun getTaskRepeatYear(month:String,day:String,searchDate:Long) : List<TaskDBEntity>
    //매달 반복 하는 특정 날의 일정
    @Query("SELECT * FROM task_table WHERE day LIKE :day AND repeatType LIKE 3 AND createDate < :searchDate")
    suspend fun getTaskRepeatMonth(day:String,searchDate:Long) : List<TaskDBEntity>
    //매주 반복 하는 특정 날의 일정
    @Query("SELECT * FROM task_table WHERE weekCount LIKE :weekCount AND repeatType LIKE 2 AND createDate < :searchDate")
    suspend fun getTaskRepeatWeek(weekCount:Int,searchDate:Long) : List<TaskDBEntity>
    //매일 반복 하는 일정
    @Query("SELECT * FROM task_table WHERE repeatType LIKE 1 AND createDate < :searchDate")
    suspend fun getTaskRepeatDay(searchDate:Long) : List<TaskDBEntity>
    //해당 달에 해당하는 매달,매년 반복 일정과 반복 없는 일정
    @Query("SELECT * FROM task_table WHERE month Like :month AND (repeatType LIKE 3 OR repeatType LIKE 4 OR repeatType LIKE 0) AND createDate < :searchDate")
    suspend fun getTaskSpecifyMonth(month:String, searchDate:Long) : List<TaskDBEntity>
    @Query("SELECT * FROM task_table WHERE year LIKE :year AND month LIKE :month AND day LIKE :day")
    suspend fun getTempTaskSpecifyDay(year:String,month:String,day:String) : List<TaskDBEntity>
    @Query("SELECT * FROM task_table")
    suspend fun getAllTask() :List<TaskDBEntity>


}