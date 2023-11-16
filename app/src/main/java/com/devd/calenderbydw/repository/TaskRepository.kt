package com.devd.calenderbydw.repository

import com.devd.calenderbydw.data.local.dao.TaskDao
import com.devd.calenderbydw.data.local.entity.TaskDBEntity
import com.devd.calenderbydw.utils.SafeNetCall
import timber.log.Timber
import javax.inject.Inject

class TaskRepository @Inject constructor(
    private val taskDao: TaskDao
) : SafeNetCall() {


    suspend fun insertTaskItem(
      insertItem : TaskDBEntity
    ){
        taskDao.insertTaskItem(insertItem).run {
            Timber.d("DBCHECK insertCheck => ${this}")
            Timber.d("DBCHECK getTaskItem => ${getTaskItem()}")
        }
    }

    suspend fun getTaskItem()= taskDao.getAllTask()


}