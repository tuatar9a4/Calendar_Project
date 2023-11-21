package com.devd.calenderbydw.repository

import android.provider.CalendarContract.CalendarEntity
import androidx.lifecycle.asLiveData
import com.devd.calenderbydw.data.local.dao.TaskDao
import com.devd.calenderbydw.data.local.entity.TaskDBEntity
import com.devd.calenderbydw.utils.SafeNetCall
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import timber.log.Timber
import java.util.Calendar
import javax.inject.Inject

class TaskRepository @Inject constructor(
    private val taskDao: TaskDao
) : SafeNetCall() {


    suspend fun insertTaskItem(
        insertItem: TaskDBEntity
    ) {
        taskDao.insertTaskItem(insertItem)
    }
    suspend fun deleteTaskItem(id:Int){
        taskDao.deleteTaskItem(TaskDBEntity(id)).run {
            Timber.d("DeleteResult [${id}]-> ${this}")
        }
    }

    suspend fun updateTaskItem(taskItem :TaskDBEntity):Int{
        taskDao.updateTaskItem(taskItem).run {
            Timber.d("DeleteResult [${taskItem.id}]-> ${this}")
            return this
        }
    }

    fun getTaskItems() = taskDao.getAllTask()

   fun getSpecifyDateTaskItems(year: String, month: String, day: String) :Flow<List<TaskDBEntity>>{
       val calendar = Calendar.getInstance().apply { set(year.toInt(), month.toInt()-1, day.toInt()) }
       val weekTime  =calendar.get(Calendar.DAY_OF_WEEK)
       val dateLongTime = calendar.time.time
       return taskDao.getTaskSpecifyDay(year, month, day,weekTime, dateLongTime)
   }
//
    suspend fun getSpecifyMonthTaskItemList(year: String, month: String)= flow<List<TaskDBEntity>>{
        val calendarTime = Calendar.getInstance().apply {
            set(year.toInt(),month.toInt()-1,1,0,0,0)
        }.time.time
        val taskList = arrayListOf<TaskDBEntity>()

//            val t1 =taskDao.getTaskRepeatDay(calendarTime)
//            val t2 =taskDao.getTaskRepeatTotalWeek(calendarTime)
//            val t3 =taskDao.getTaskRepeatYearMonthSpecifyMonth(month,calendarTime)
//            val t4 =taskDao.getTaskSpecifyMonth(year,month,calendarTime)
//            Timber.d("DBCHECK getTaskRepeatDay1698796800 003 < 1698796800 423${calendarTime}: ${t1}")
//            Timber.d("DBCHECK getTaskRepeatTotalWeek : ${t2}")
//            Timber.d("DBCHECK getTaskRepeatYearMonthSpecifyMonth : ${t3}")
//            Timber.d("DBCHECK getTaskSpecifyMonth : ${t4}")
//        taskList.addAll(taskDao.getTaskRepeatDay(calendarTime))
        taskList.addAll(taskDao.getTaskRepeatTotalWeek(calendarTime))
        taskList.addAll(taskDao.getTaskRepeatYearMonthSpecifyMonth(month,calendarTime))
        taskList.addAll(taskDao.getTaskSpecifyMonth(year,month,calendarTime))
        emit(taskList)
    }

    suspend fun getSpecifyMonthTaskItemsInit(year: String, month: String,):List<TaskDBEntity>{
        val calendarTime = Calendar.getInstance().apply {
            set(year.toInt(),month.toInt()-1,1,0,0,0)
        }.time.time
        val taskList = arrayListOf<TaskDBEntity>()
        taskList.addAll(taskDao.getTaskRepeatDay(calendarTime))
        taskList.addAll(taskDao.getTaskRepeatTotalWeek(calendarTime))
        taskList.addAll(taskDao.getTaskRepeatYearMonthSpecifyMonth(month,calendarTime))
        taskList.addAll(taskDao.getTaskSpecifyMonth(year,month,calendarTime))
        return taskList
    }
}