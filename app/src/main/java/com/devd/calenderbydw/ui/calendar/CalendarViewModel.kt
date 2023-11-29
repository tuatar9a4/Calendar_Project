package com.devd.calenderbydw.ui.calendar

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.devd.calenderbydw.data.local.calendar.YearMonthDayData
import com.devd.calenderbydw.data.local.entity.CalendarMonthEntity
import com.devd.calenderbydw.data.local.entity.TaskDBEntity
import com.devd.calenderbydw.data.remote.holiday.HolidayItem
import com.devd.calenderbydw.repository.CalendarRepository
import com.devd.calenderbydw.repository.TaskRepository
import com.devd.calenderbydw.utils.Event
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.Calendar
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class CalendarViewModel @Inject constructor(
    private val calendarRepository: CalendarRepository,
    private val taskRepository: TaskRepository
) : ViewModel() {

    private val _updateCalendarData = MutableLiveData<Event<Boolean>>()
    val updateCalendarData: LiveData<Event<Boolean>> get() = _updateCalendarData
    val calendarAdapter = CalendarMonthAdapter()
    var monthTaskList : SharedFlow<List<TaskDBEntity>>? = null
    var currentToday = YearMonthDayData()
    var currentPos = 0
    var firstUpdate = true
    private var startIndex = Calendar.getInstance().apply { time = Date() }.get(Calendar.YEAR)-1
    private var endIndex = Calendar.getInstance().apply { time = Date() }.get(Calendar.YEAR)
    fun getHolidayYear(isNextYear: Boolean, callYear: Int) {
        viewModelScope.launch {
            val calendar = Calendar.getInstance().apply {
                time = Date()
            }
            currentToday = YearMonthDayData(
                year = calendar.get(Calendar.YEAR),
                month = (calendar.get(Calendar.MONTH) + 1),
                day = calendar.get(Calendar.DAY_OF_MONTH)
            )
            if ((isNextYear && endIndex == callYear) || (!isNextYear && startIndex == callYear)) {
                calendarRepository.getCalendarDataInDB(
                    if (isNextYear) endIndex else startIndex - 2,
                    if (isNextYear) endIndex + 2 else startIndex
                ).run {
//                    taskRepository.getTaskItems()
                    changeTodayItem(this, currentToday.year, currentToday.month, currentToday.day)
                    if (calendarAdapter.itemCount == 0) {
                        calendarAdapter.submitList(this) {
                            _updateCalendarData.value = Event(firstUpdate)
                        }
                    } else {
                        if (isNextYear) {
                            calendarAdapter.submitList(calendarAdapter.currentList + this)
                        } else {
                            calendarAdapter.submitList(this + calendarAdapter.currentList)
                        }
                    }
                    if (isNextYear) {
                        endIndex += 3
                    } else {
                        startIndex -= 3
                    }
                }
            }
        }
    }

    var tempJob = SupervisorJob()
    fun setMonthTaskList(year:String,month:String,lastDay :Int){
        viewModelScope.launch {
            Timber.d("setMonthTaskList ${year}.${month}.${lastDay}")
            monthTaskList = taskRepository.getSpecifyMonthTaskItemList(year,month,lastDay).stateIn(
                scope = viewModelScope,
            )
            tempJob = SupervisorJob()
            templateCollectItem()
        }
    }
    private fun templateCollectItem(){
        CoroutineScope(Dispatchers.IO + tempJob).launch {
            Timber.d("collectLatest2 1111")
            monthTaskList?.collectLatest{
                Timber.d("collectLatest2 ?? ${it.size}")
                addTaskDataInItem(it)
                tempJob.cancel()
                Timber.d("collectLatest2 ?? 44444")
            }
            Timber.d("collectLatest2 ?? 33333")
        }
    }

    fun addTaskDataInItem(items :List<TaskDBEntity>){
        viewModelScope.launch {
//            items.forEach {
//                Timber.d("task List : ${it}")
//            }
            calendarAdapter.setTaskData(items,currentPos)
        }
    }

    //SimpleDateFormat 를 사용하기에는 메모리 소비가 너무 강함
    private fun getYearToDayFormat(year: Int, month: Int, day: Int): String {
        val calMonth = if (month.toString().length == 1) {
            "0${month}"
        } else {
            month.toString()
        }
        val calDay = if (day.toString().length == 1) {
            "0${day}"
        } else {
            day.toString()
        }
        return "${year}${calMonth}${calDay}"
    }

    private fun checkHolidayItem(
        holidayInfo: List<HolidayItem>, yearToDayStr: String
    ): HolidayItem? {
        val checkHoliday = holidayInfo.filter {
            it.holidayYearToday == yearToDayStr
        }
        return if (checkHoliday.isNotEmpty()) {
            checkHoliday[0]
        } else {
            null
        }
    }

    //달력이 다음 년도 밑 작년 년도에 가까워 졌을 때 공휴일을 받아 오기 위해 실행
    fun updateYearDb(encodeKey: String, year: Int) {
        viewModelScope.launch {
            calendarRepository.checkHolidayDBOfYear(year)?.map { it.toHolidayItem() }
                ?.let { holidayList ->
                    calendarAdapter.currentList.filter { it.year == year }.forEach { calendarData ->
                        calendarData.dayList.forEach { dayItem ->
                            checkHolidayItem(
                                holidayList, getYearToDayFormat(
                                    dayItem.year.toInt(), dayItem.month.toInt(), dayItem.day.toInt()
                                )
                            )?.let { holidayItem ->
                                dayItem.holidayName = holidayItem.holidayName
                                dayItem.isHoliday = holidayItem.isHolidayBoolean
                            } ?: kotlin.run {
                                dayItem.holidayName = null
                                dayItem.isHoliday = false
                            }
                        }
                    }
                } ?: kotlin.run {
                val holidayList = calendarRepository.getHolidayOfYearApi(encodeKey, year, true, 0)
                calendarAdapter.currentList.filter { it.year == year }.forEach { calendarData ->
                    calendarData.dayList.forEach { dayItem ->
                        checkHolidayItem(
                            holidayList, getYearToDayFormat(
                                dayItem.year.toInt(), dayItem.month.toInt(), dayItem.day.toInt()
                            )
                        )?.let { holidayItem ->
                            dayItem.holidayName = holidayItem.holidayName
                            dayItem.isHoliday = holidayItem.isHolidayBoolean
                        }
                    }
                }
                calendarRepository.updateCalendarData(calendarAdapter.currentList)
            }
        }
    }

    private fun changeTodayItem(
        changeList: List<CalendarMonthEntity>?,
        year: Int,
        month: Int,
        day: Int
    ) {
        changeList?.filter { it.year == year && it.month == month }
            ?.forEach { calendarData ->
                calendarData.dayList.forEach { dayItem ->
                    dayItem.toDay = (dayItem.day == day.toString() && dayItem.month == month.toString())
                }
            }
    }

    fun checkToday() {
        val calendar = Calendar.getInstance()
        calendar.time = Date()
        if ((currentToday.year == calendar.get(Calendar.YEAR) &&
                    currentToday.month == (calendar.get(Calendar.MONTH) + 1) &&
                    currentToday.day == calendar.get(Calendar.DAY_OF_MONTH)) ||
            calendarAdapter.currentList.isEmpty()
        ) {
            return
        }
        val originIndex = checkIndexYearMonth(currentToday.year, currentToday.month)
        currentToday = YearMonthDayData(
            year = calendar.get(Calendar.YEAR),
            month = (calendar.get(Calendar.MONTH) + 1),
            day = calendar.get(Calendar.DAY_OF_MONTH)
        )
        changeTodayItem(
            calendarAdapter.currentList,
            currentToday.year,
            currentToday.month,
            currentToday.day
        )
        val newIndex = checkIndexYearMonth(currentToday.year, currentToday.month)
        if (originIndex == newIndex) {
            calendarAdapter.notifyItemChanged(originIndex)
        } else {
            calendarAdapter.notifyItemChanged(originIndex)
            calendarAdapter.notifyItemChanged(newIndex)
        }
    }

    private fun checkIndexYearMonth(year: Int, month: Int) =
        calendarAdapter.currentList.indexOfFirst { it.year == year && it.month == month }

    fun getAdapterCurrentList(): List<CalendarMonthEntity> = calendarAdapter.currentList
}