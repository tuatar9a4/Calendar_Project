package com.devd.calenderbydw.ui.task.tasklist

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.devd.calenderbydw.data.local.calendar.CalendarDayData
import com.devd.calenderbydw.data.local.calendar.YearMonthDayData
import com.devd.calenderbydw.data.local.entity.TaskDBEntity
import com.devd.calenderbydw.repository.CalendarRepository
import com.devd.calenderbydw.repository.TaskRepository
import com.devd.calenderbydw.utils.Event
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
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
    val taskDateResult :LiveData<Event<Int>> get() = _taskDateResult
    var taskList : StateFlow<List<TaskDBEntity>>? = null

    val topDateAdapter = TaskListTopDateAdapter()
    var currentTopYearMonth = ""
    var selectYearToDay :YearMonthDayData? =null

    fun getCalendarList(encodeKey:String,year:Int,month:Int,day:Int){
        viewModelScope.launch {
            calendarRepository.getCalendarMergeDb(encodeKey,year).run {
                val fullDayList = ArrayList<CalendarDayData>()
                this.forEachIndexed { index, calendarData ->
                    fullDayList +=calendarData.dayList.filter { it.isCurrentMonth }
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
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = listOf()
            )
        }
    }

}