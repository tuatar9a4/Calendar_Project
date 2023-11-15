package com.devd.calenderbydw.ui.calendar

import android.annotation.SuppressLint
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.devd.calenderbydw.data.local.CalendarData
import com.devd.calenderbydw.data.local.CalendarDayData
import com.devd.calenderbydw.data.local.db.HolidayDbData
import com.devd.calenderbydw.data.remote.CallResult
import com.devd.calenderbydw.data.remote.holiday.HolidayItem
import com.devd.calenderbydw.repository.HolidayRepository
import com.devd.calenderbydw.utils.Event
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class CalendarViewModel @Inject constructor(
    private val holidayRepository: HolidayRepository
) : ViewModel() {

    private val _calendarLiveData = MutableLiveData<Event<List<CalendarData>>>()
    val calendarLiveData: LiveData<Event<List<CalendarData>>> get() = _calendarLiveData

    fun getHolidayYear(encodeKey: String) {
        viewModelScope.launch {
            val calendar = Calendar.getInstance()
            calendar.time = Date()
            holidayRepository.getHolidayOfYearInDB(encodeKey, calendar.get(Calendar.YEAR)).run {
                if (this.isNotEmpty()) {
                    when (this[0]) {
                        is HolidayDbData -> {
                            val item =this
                            getCalendarDate( item.map { (it as HolidayDbData).toHolidayItem() })
//                            holidayRepository.getHolidayOfYear(encodeKey, calendar.get(Calendar.YEAR),true,item.size)
                        }
                        is HolidayItem -> {
                            getCalendarDate( this.map { it as HolidayItem })

                        }
                    }
                }else{
                    getCalendarDate( listOf())
                }

            }
        }
    }

    /**
     * calendar.get(Calendar.DAY_OF_WEEK)               : 그 주의 요일 변환 일:1 ~ 토:7
     * calendar.getActualMaximum(Calendar.DAY_OF_MONTH) : 해당 월의 날짜 수
     */
    @SuppressLint("SimpleDateFormat")
    private fun getCalendarDate(holidayInfo: List<HolidayItem>) {
        Timber.d("Calendar holidayCheck holidayInfo: ${holidayInfo}")
        viewModelScope.launch {
            val calendar = Calendar.getInstance()
            val monthDataList = arrayListOf<CalendarData>()
            for (monthCount in -200..200) {
                calendar.time = Date()
                calendar.add(Calendar.MONTH, monthCount)
                val tempMonth = calendar.get(Calendar.MONTH) + 1
                val calendarList = arrayListOf<CalendarDayData>()
//                val weeks = getMonthWeekCount(calendar)
                calendar.set(Calendar.DAY_OF_MONTH, 1)   // 달의 첫번째로 이동
                val lastDay = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
                val firstWeek = calendar.get(Calendar.DAY_OF_WEEK)
                if(firstWeek !=1){
                    calendar.add(Calendar.DAY_OF_MONTH,(1-firstWeek))
                    for(i in 0 until  firstWeek-1){
                        val holidayItem = checkHolidayItem(holidayInfo,calendar)
                        holidayItem?.let {
                            calendarList.add(
                                CalendarDayData(
                                    day = calendar.get(Calendar.DAY_OF_MONTH).toString(),
                                    holidayName = it.holidayName,
                                    isHoliday = it.isHolidayBoolean
                                )
                            )
                        }?: kotlin.run {
                            calendarList.add(
                                CalendarDayData(
                                    day = calendar.get(Calendar.DAY_OF_MONTH).toString()
                                )
                            )
                        }
                        calendar.add(Calendar.DAY_OF_MONTH,1)
                    }
                }
                for( i in 1 .. lastDay){
                    val holidayItem = checkHolidayItem(holidayInfo,calendar)
                    holidayItem?.let {
                        calendarList.add(
                            CalendarDayData(
                                day = calendar.get(Calendar.DAY_OF_MONTH).toString(),
                                holidayName = it.holidayName,
                                isHoliday = it.isHolidayBoolean
                            )
                        )
                    }?: kotlin.run {
                        calendarList.add(
                            CalendarDayData(
                                day = calendar.get(Calendar.DAY_OF_MONTH).toString()
                            )
                        )
                    }
                    calendar.add(Calendar.DAY_OF_MONTH,1)
                }
                calendar.set(Calendar.DAY_OF_MONTH, lastDay)
                val lastWeek = calendar.get(Calendar.DAY_OF_WEEK)
                if(lastWeek!=7){
                    for(i in 7 downTo  lastWeek+1){
                        calendar.add(Calendar.DAY_OF_MONTH,1)
                        val holidayItem = checkHolidayItem(holidayInfo,calendar)
                        holidayItem?.let {
                            calendarList.add(
                                CalendarDayData(
                                    day = calendar.get(Calendar.DAY_OF_MONTH).toString(),
                                    holidayName = it.holidayName,
                                    isHoliday = it.isHolidayBoolean
                                )
                            )
                        }?: kotlin.run {
                            calendarList.add(
                                CalendarDayData(
                                    day = calendar.get(Calendar.DAY_OF_MONTH).toString()
                                )
                            )
                        }
                    }
                }
                monthDataList.add(
                    CalendarData(
                        month = tempMonth,
                        dayList = calendarList
                    )
                )
            }
            Timber.d("Calendar holidayCheck end")
            _calendarLiveData.value = Event(monthDataList)
//        monthDataList.forEach {
//            Timber.d("Calendar List ${it}")
//        }
        }
    }

    private fun getMonthWeekCount(calendar: Calendar): Int {
        val dayCount = calendar.getActualMaximum(Calendar.DAY_OF_MONTH) // 해당 월의 날짜수
        calendar.set(Calendar.DAY_OF_MONTH, dayCount)                       // 해당 월의 마지막 날짜로 이동
        val dayWeek = calendar.get(Calendar.DAY_OF_WEEK)                // 요일 반환
//        Timber.d("Calendar [${calendar.get(Calendar.MONTH) + 1}] 월 주차 수  : ${(dayCount - dayWeek + 13) / 7}")
        return (dayCount - dayWeek + 13) / 7
    }

    private fun getYearToDayFormat(calendar : Calendar) :String{
        val calMonth = if((calendar.get(Calendar.MONTH)+1).toString().length==1){
            "0${calendar.get(Calendar.MONTH)+1}"
        }else{
            (calendar.get(Calendar.MONTH)+1).toString()
        }
        val calDay = if(calendar.get(Calendar.DAY_OF_MONTH).toString().length==1){
            "0${calendar.get(Calendar.DAY_OF_MONTH)}"
        }else{
            calendar.get(Calendar.DAY_OF_MONTH).toString()
        }
        return "${calendar.get(Calendar.MONTH)}${calMonth}${calDay}"
    }

    private fun checkHolidayItem(holidayInfo: List<HolidayItem> , calendar: Calendar):HolidayItem?{
        val checkHoliday = holidayInfo.filter {
            it.holidayYearToday == getYearToDayFormat(calendar)
        }
        return if(checkHoliday.isNotEmpty()){
            checkHoliday[0]
        }else{
            null
        }
    }
}