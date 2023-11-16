package com.devd.calenderbydw.ui.task.tasklist

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.devd.calenderbydw.data.local.calendar.CalendarData
import com.devd.calenderbydw.data.local.calendar.CalendarDayData
import com.devd.calenderbydw.repository.CalendarRepository
import com.devd.calenderbydw.utils.Event
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class TaskListViewModel @Inject constructor(
    private val calendarRepository: CalendarRepository
) : ViewModel() {

    private val _taskListDates = MutableLiveData<Event<Int>>()
    val taskListDates :LiveData<Event<Int>> get() = _taskListDates

    val topDateAdapter = TaskListTopDateAdapter()
    var currentTopYearMonth = ""

    fun getCalendarList(encodeKey:String,year:Int,month:Int,day:Int){
        viewModelScope.launch {
            calendarRepository.getCalendarMergeDb(encodeKey,year).run {
                val fullDayList = ArrayList<CalendarDayData>()
                this.forEachIndexed { index, calendarData ->
                    fullDayList +=calendarData.dayList.filter { it.isCurrentMonth }
                }
                topDateAdapter.submitList(fullDayList)
                _taskListDates.value= Event(
                    fullDayList.indexOfFirst { it.year == year.toString() && it.month ==month.toString() && it.day == day.toString() }
                )
                Timber.d("fullDayList ${fullDayList.size}")
            }
        }
    }

}