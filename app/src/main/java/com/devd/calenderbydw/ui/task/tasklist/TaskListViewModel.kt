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
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TaskListViewModel @Inject constructor(
    private val calendarRepository: CalendarRepository,
    private val taskRepository: TaskRepository
) : ViewModel() {

    private val _taskDateResult = MutableLiveData<Event<Int>>()
    val taskDateResult :LiveData<Event<Int>> get() = _taskDateResult
    var taskList : StateFlow<List<TaskDBEntity>>? = null

    val topDateAdapter = TaskListTopDateAdapter()
    val scheduleItemAdapter = TaskListScheduleItemAdapter()
    var currentTopYearMonth = ""
    var selectYearToDay :YearMonthDayData? =null

    private val _taskListDate = MutableLiveData<Event<List<TaskDBEntity>>>()
    val taskListDate : LiveData<Event<List<TaskDBEntity>>> get() = _taskListDate
    private var startIndex = 2021
    fun getCalendarList(encodeKey:String,year:Int,month:Int,day:Int){
        viewModelScope.launch {
            calendarRepository.getCalendarDataInDB(startIndex,startIndex+2).run {
                val fullDayList = ArrayList<CalendarDayEntity>()
                this.forEachIndexed { index, calendarData ->
                    fullDayList +=calendarData.dayList.filter { it.isCurrentMonth }
                }
                val allTask = taskRepository.getTaskItems()
                fullDayList.forEach { calendar ->
                    calendar.existsTask = allTask.any{ task ->
                        if(calendar.dateTimeLong < task.createDate){
                            false
                        }else{
                            when(task.repeatType){
                                DAILY_REPEAT->{
                                    true
                                }
                                WEEK_REPEAT->{
                                    calendar.weekCount==task.weekCount
                                }
                                MONTH_REPEAT->{
                                    calendar.day == task.day
                                }
                                YEAR_REPEAT->{
                                    calendar.month == task.month && calendar.day == task.day
                                }
                                else->{
                                    calendar.year == task.year && calendar.month == task.month && calendar.day == task.day
                                }
                            }
                        }
                    }
                }
                topDateAdapter.submitList(fullDayList)
                _taskDateResult.value= Event(
                    fullDayList.indexOfFirst { it.year == year.toString() && it.month ==month.toString() && it.day == day.toString() }
                )
            }
        }
    }

    fun getSelectDateTaskList(year:String,month:String,day:String){
        viewModelScope.launch {
            taskList = taskRepository.getSpecifyDateTaskItems(year, month, day).stateIn(
                scope = this,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = listOf()
            )
        }
    }
    fun getSelectDateTaskListInit(year:String,month:String,day:String){
        viewModelScope.launch {
            getSelectDateTaskList(year,month,day)
            taskRepository.getSpecifyDateTaskItemsInit(year, month, day).run {
                _taskListDate.value = Event(this)
            }
        }
    }

}