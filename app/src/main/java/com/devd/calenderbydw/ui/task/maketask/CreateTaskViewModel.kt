package com.devd.calenderbydw.ui.task.maketask

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.devd.calenderbydw.data.local.entity.TaskDBEntity
import com.devd.calenderbydw.repository.TaskRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CreateTaskViewModel @Inject constructor(
    private val taskRepository: TaskRepository,
) : ViewModel() {

    private val _taskTitle = MutableStateFlow("")
    val taskTitle :StateFlow<String> =_taskTitle

    private var taskContents : String = ""


    fun setTitle(title: String){
        _taskTitle.value = title
    }

    fun setContents(contents: String){
        taskContents = contents
    }

    fun insertTaskInDB(
        year : Int,
        month : Int,
        day :Int,
        title :String,
        contents :String,
        repeatSate :Int
    ){
        viewModelScope.launch {
            taskRepository.insertTaskItem(
                TaskDBEntity(
                    year = year,
                    month =  month,
                    day = day,
                    title = title,
                    contents = contents,
                    repeatType = repeatSate
                )
            )
        }
    }
}