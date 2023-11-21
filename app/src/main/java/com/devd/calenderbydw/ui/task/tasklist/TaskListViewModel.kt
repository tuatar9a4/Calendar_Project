package com.devd.calenderbydw.ui.task.tasklist

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.devd.calenderbydw.data.local.calendar.YearMonthDayData
import com.devd.calenderbydw.data.local.entity.CalendarDayEntity
import com.devd.calenderbydw.data.local.entity.TaskDBEntity
import com.devd.calenderbydw.data.local.entity.TaskDBEntity.Companion.DAILY_REPEAT
import com.devd.calenderbydw.data.local.entity.TaskDBEntity.Companion.MONTH_REPEAT
import com.devd.calenderbydw.data.local.entity.TaskDBEntity.Companion.WEEK_REPEAT
import com.devd.calenderbydw.data.local.entity.TaskDBEntity.Companion.YEAR_REPEAT
import com.devd.calenderbydw.repository.CalendarRepository
import com.devd.calenderbydw.repository.TaskRepository
import com.devd.calenderbydw.utils.Event
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class TaskListViewModel @Inject constructor(
    private val calendarRepository: CalendarRepository,
    private val taskRepository: TaskRepository
) : ViewModel() {

    private val _taskDateResult = MutableLiveData<Event<Int>>()
    val taskDateResult: LiveData<Event<Int>> get() = _taskDateResult
    var taskList: StateFlow<List<TaskDBEntity>>? = null

    val topDateAdapter = TaskListTopDateAdapter()
    val scheduleItemAdapter = TaskListScheduleItemAdapter()
    var allTaskList : StateFlow<List<TaskDBEntity>>? = null
    var currentTopYearMonth = ""
    var selectYearToDay: YearMonthDayData? = null

    private val _taskListDate = MutableLiveData<Event<List<TaskDBEntity>>>()
    val taskListDate: LiveData<Event<List<TaskDBEntity>>> get() = _taskListDate
    var startIndex = 2023
    var endIndex = 2024
    fun getCalendarList(year: Int, month: Int, day: Int, isNextYear: Boolean, callYear: Int) {
        viewModelScope.launch {
            if((isNextYear && callYear>=endIndex) || (!isNextYear &&callYear<=startIndex)){
                calendarRepository.getCalendarDataInDB(
                    if(isNextYear) endIndex else startIndex,
                    if(isNextYear) endIndex+2 else startIndex -2
                ).run {
                    val fullDayList = ArrayList<CalendarDayEntity>()
                    this.forEachIndexed { index, calendarData ->
                        fullDayList += calendarData.dayList.filter { it.isCurrentMonth }
                    }
                    if(isNextYear){
                        endIndex+=3
                    }else{
                        startIndex-=3
                    }
                    if(topDateAdapter.itemCount == 0){
                        topDateAdapter.submitList(fullDayList){
                            _taskDateResult.value = Event(
                                fullDayList.indexOfFirst { it.year == year.toString() && it.month == month.toString() && it.day == day.toString() }
                            )
                        }
                    }else if(isNextYear){
                        topDateAdapter.submitList(topDateAdapter.currentList+fullDayList)
                    }else{
                        topDateAdapter.submitList(fullDayList+topDateAdapter.currentList)
                    }
                }
            }
        }
    }

    fun updateTaskState(){
        viewModelScope.launch {
            allTaskList = taskRepository.getTaskItems().stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(4000),
                initialValue = listOf()
            )
        }
    }

    fun deleteTaskItemInDB(id:Int){
        viewModelScope.launch {
            taskRepository.deleteTaskItem(id)
        }
    }
    fun getSelectDateTaskList(year: String, month: String, day: String) {
        viewModelScope.launch {
            taskList = taskRepository.getSpecifyDateTaskItems(year, month, day).stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = listOf()
            )
            taskList?.collectLatest {
                scheduleItemAdapter.submitList(it)
            }
        }
    }

}