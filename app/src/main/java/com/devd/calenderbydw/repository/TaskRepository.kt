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

    suspend fun deleteTaskItem(id: Int) {
        taskDao.deleteQueryTaskItem(id).run {
            Timber.d("DeleteResult [${id}]-> ${this}")
        }
//        taskDao.deleteTaskItem(TaskDBEntity(id)).run {
//            Timber.d("DeleteResult [${id}]-> ${this}")
//        }
    }

    suspend fun updateTaskItem(taskItem: TaskDBEntity): Int {
        taskDao.updateTaskItem(taskItem).run {
            Timber.d("UpdateResult [${taskItem.id}]-> ${this}")
            return this
        }
    }

    fun getTaskItems() = taskDao.getAllTask()

    fun getSpecifyDateTaskItems(
        year: String,
        month: String,
        day: String
    ): Flow<List<TaskDBEntity>> {
        val calendar =
            Calendar.getInstance().apply { set(year.toInt(), month.toInt() - 1, day.toInt()) }
        val weekTime = calendar.get(Calendar.DAY_OF_WEEK)
        val dateLongTime = calendar.time.time
        return taskDao.getTaskSpecifyDay(year, month, day, weekTime, dateLongTime)
    }

    //
    fun getSpecifyMonthTaskItemList(year: String, month: String,lastDay:Int): Flow<List<TaskDBEntity>> {
        val calendarTime = Calendar.getInstance().apply {
            set(year.toInt(), month.toInt() - 1, lastDay, 24, 60, 59)
        }.time.time
        return taskDao.getTaskSpecifyMonth(year, month, calendarTime)
    }
}