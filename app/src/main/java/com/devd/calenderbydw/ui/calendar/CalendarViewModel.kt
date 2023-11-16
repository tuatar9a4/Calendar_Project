package com.devd.calenderbydw.ui.calendar

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.devd.calenderbydw.data.local.calendar.CalendarData
import com.devd.calenderbydw.data.local.calendar.YearMonthDayData
import com.devd.calenderbydw.data.remote.holiday.HolidayItem
import com.devd.calenderbydw.repository.CalendarRepository
import com.devd.calenderbydw.utils.Event
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class CalendarViewModel @Inject constructor(
    private val calendarRepository: CalendarRepository
) : ViewModel() {

    private val _updateCalendarData = MutableLiveData<Event<Boolean>>()
    val updateCalendarData: LiveData<Event<Boolean>> get() = _updateCalendarData
    val calendarAdapter = CalendarMonthAdapter()
    private var currentToday = YearMonthDayData()

    fun getHolidayYear(encodeKey: String) {
        viewModelScope.launch {
            val calendar = Calendar.getInstance()
            calendar.time = Date()
            currentToday = YearMonthDayData(
                year = calendar.get(Calendar.YEAR),
                month = (calendar.get(Calendar.MONTH) + 1),
                day = calendar.get(Calendar.DAY_OF_MONTH)
            )
            calendarRepository.getCalendarMergeDb(encodeKey, calendar.get(Calendar.YEAR)).run {
                changeTodayItem(this, currentToday.year, currentToday.month, currentToday.day)
                calendarAdapter.submitList(this) {
                    _updateCalendarData.value = Event(true)
                }
            }
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
                                    calendarData.year, calendarData.month, dayItem.day.toInt()
                                )
                            )?.let { holidayItem ->
                                dayItem.holidayName = holidayItem.holidayName
                                dayItem.isHoliday = holidayItem.isHolidayBoolean
                            }
                        }
                    }
                } ?: kotlin.run {
                val holidayList = calendarRepository.getHolidayOfYear(encodeKey, year, true, 0)
                calendarAdapter.currentList.filter { it.year == year }.forEach { calendarData ->
                    calendarData.dayList.forEach { dayItem ->
                        checkHolidayItem(
                            holidayList, getYearToDayFormat(
                                calendarData.year, calendarData.month, dayItem.day.toInt()
                            )
                        )?.let { holidayItem ->
                            dayItem.holidayName = holidayItem.holidayName
                            dayItem.isHoliday = holidayItem.isHolidayBoolean
                        }
                    }
                }
            }
        }
    }
    private fun changeTodayItem(changeList: List<CalendarData>?, year: Int, month: Int, day: Int) {
        changeList?.filter { it.year == year && it.month == month }
            ?.forEach { calendarData ->
                calendarData.dayList.forEach { dayItem ->
                    dayItem.toDay = dayItem.day == day.toString()
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

    fun getAdapterCurrentList(): List<CalendarData> = calendarAdapter.currentList
}