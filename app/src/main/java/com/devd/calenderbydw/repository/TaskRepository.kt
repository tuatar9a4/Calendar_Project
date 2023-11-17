package com.devd.calenderbydw.repository

import com.devd.calenderbydw.data.local.dao.TaskDao
import com.devd.calenderbydw.data.local.entity.TaskDBEntity
import com.devd.calenderbydw.utils.SafeNetCall
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import timber.log.Timber
import java.util.Calendar
import javax.inject.Inject

class TaskRepository @Inject constructor(
    private val taskDao: TaskDao
) : SafeNetCall() {


    suspend fun insertTaskItem(
        insertItem: TaskDBEntity
    ) {
        taskDao.insertTaskItem(insertItem).run {
            Timber.d("DBCHECK insertCheck => ${this}")
            Timber.d(
                "DBCHECK getTaskItem => ${
                    taskDao.getTempTaskSpecifyDay(
                        insertItem.year,
                        insertItem.month,
                        insertItem.day
                    )
                }"
            )
        }
    }

    suspend fun getTaskItems() = taskDao.getAllTask()

    fun getSpecifyDateTaskItems(year: String, month: String, day: String) =
        flow<List<TaskDBEntity>> {
            val calendar = Calendar.getInstance().apply { set(year.toInt(), month.toInt()-1, day.toInt()) }
            val dateLongTime = calendar.time.time
            val weekTime  =calendar.get(Calendar.DAY_OF_WEEK)
            val taskList = arrayListOf<TaskDBEntity>()
            Timber.d("DBCHECK inputDate : \n" +
                    "year : ${year}\n" +
                    "month : ${month}\n" +
                    "day : ${day}\n" +
                    "weekTime : ${weekTime}\n" +
                    "dateLongTime : ${dateLongTime}")
            val t1 = taskDao.getTaskSpecifyDay(year, month, day, dateLongTime)
            val t2 = taskDao.getTaskRepeatYear(month, day, dateLongTime)
            val t3 = taskDao.getTaskRepeatMonth(day, dateLongTime)
            val t4 = taskDao.getTaskRepeatWeek(weekTime, dateLongTime)
            val t5 = taskDao.getTaskRepeatDay(dateLongTime)
            Timber.d("DBCHECK getTaskSpecifyDay : ${t1}")
            Timber.d("DBCHECK getTaskRepeatYear : ${t2}")
            Timber.d("DBCHECK getTaskRepeatMonth : ${t3}")
            Timber.d("DBCHECK getTaskRepeatWeek : ${t4}")
            Timber.d("DBCHECK getTaskRepeatDay : ${t5}")
            taskList.addAll(taskDao.getTaskSpecifyDay(year, month, day, dateLongTime))
            taskList.addAll(taskDao.getTaskRepeatYear(month, day, dateLongTime))
            taskList.addAll(taskDao.getTaskRepeatMonth(day, dateLongTime))
            taskList.addAll(taskDao.getTaskRepeatWeek(weekTime, dateLongTime))
            taskList.addAll(taskDao.getTaskRepeatDay(dateLongTime))
            emit(taskList)
        }.catch { e ->

        }


}