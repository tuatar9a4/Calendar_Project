package com.devd.calenderbydw.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.devd.calenderbydw.data.local.entity.TaskDBEntity
import kotlinx.coroutines.flow.Flow

/**
 * repeatType 0:반복 없음,1: 매일, 2:매주, 3:매달, 4:매년
 */
@Dao
interface TaskDao {

    @Insert
    suspend fun insertTaskItem(item: TaskDBEntity): Long

    @Delete
    suspend fun deleteTaskItem(item: TaskDBEntity): Int

    @Update
    suspend fun updateTaskItem(item: TaskDBEntity): Int

    //특정 날의 일정
    @Query(
        "SELECT * FROM task_table WHERE " +
                "((year LIKE :year AND month LIKE :month AND day LIKE :day AND repeatType LIKE 0) OR" +
                "(month LIKE :month AND day LIKE :day AND repeatType LIKE 4) OR" +
                "(day LIKE :day AND repeatType LIKE 3) OR" +
                "(weekCount LIKE :weekCount AND repeatType LIKE 2) OR" +
                "(repeatType LIKE 1))" +
                " AND createDate < :searchDate"
    )
    fun getTaskSpecifyDay(
        year: String,
        month: String,
        day: String,
        weekCount: Int,
        searchDate: Long
    ): Flow<List<TaskDBEntity>>

    //매일 반복 하는 일정
    @Query("SELECT * FROM task_table WHERE repeatType LIKE 1 AND createDate >= :searchDate")
    suspend fun getTaskRepeatDay(searchDate: Long): List<TaskDBEntity>

    //해당 달에 해당하는 매달,매년 반복 일정
    @Query("SELECT * FROM task_table WHERE month Like :month AND (repeatType LIKE 3 OR repeatType LIKE 4) AND createDate >= :searchDate")
    suspend fun getTaskRepeatYearMonthSpecifyMonth(
        month: String,
        searchDate: Long
    ): List<TaskDBEntity>

    //해당 달에 해당하는 반복 없음
    @Query("SELECT * FROM task_table WHERE year LIKE :year  AND month Like :month  AND repeatType LIKE 0 AND createDate >= :searchDate")
    suspend fun getTaskSpecifyMonth(
        year: String,
        month: String,
        searchDate: Long
    ): List<TaskDBEntity>

    //해당 달의 매주 반복하는 일정들
    @Query("SELECT * FROM task_table WHERE repeatType LIKE 2 AND createDate >= :searchDate")
    suspend fun getTaskRepeatTotalWeek(searchDate: Long): List<TaskDBEntity>

    @Query("SELECT * FROM task_table WHERE year LIKE :year AND month LIKE :month AND day LIKE :day")
    suspend fun getTempTaskSpecifyDay(year: String, month: String, day: String): List<TaskDBEntity>

    @Query("SELECT * FROM task_table")
    fun getAllTask(): Flow<List<TaskDBEntity>>


}