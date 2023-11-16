package com.devd.calenderbydw.repository

import android.annotation.SuppressLint
import com.devd.calenderbydw.data.local.calendar.CalendarData
import com.devd.calenderbydw.data.local.calendar.CalendarDayData
import com.devd.calenderbydw.data.local.dao.HolidayDao
import com.devd.calenderbydw.data.remote.CallResult
import com.devd.calenderbydw.data.remote.api.HolidayService
import com.devd.calenderbydw.data.remote.holiday.HolidayItem
import com.devd.calenderbydw.di.NetworkModule
import com.devd.calenderbydw.utils.SafeNetCall
import kotlinx.coroutines.Dispatchers
import java.util.Calendar
import java.util.Date

class CalendarRepository(
    @NetworkModule.HolidayServer private val holidayService: HolidayService,
    private val holidayDao: HolidayDao
) : SafeNetCall() {

    suspend fun getHolidayOfYear(
        serviceKey: String,
        year: Int,
        checkUpdate: Boolean,
        dbHolidaySize: Int
    ): List<HolidayItem> {
        safeApiCall(Dispatchers.IO) {
            holidayService.getHolidayOfMonth(serviceKey, year)
        }.run {
            return when (this) {
                is CallResult.Success -> {
                    if (checkUpdate) {
                        if (dbHolidaySize != this.data.body.items.item.size) {
                            holidayDao.insertHolidayItemList(this.data.body.items.item.map { it.toHolidayDbItem() })
                        }
                    } else {
                        holidayDao.insertHolidayItemList(this.data.body.items.item.map { it.toHolidayDbItem() })
                    }
                    this.data.body.items.item
                }

                else -> {
                    listOf()
                }
            }
        }
    }

    suspend fun getCalendarMergeDb(serviceKey: String, year: Int): List<CalendarData> {
        val holidayData = holidayDao.selectHolidayItemOfYear(year)
        return if(holidayData.isNotEmpty()){
            getCalendarDate(holidayData.map { it.toHolidayItem() })
        }else{
            getHolidayOfYear(serviceKey, year, false, 0).run {
                return getCalendarDate(this)
            }
        }
//        return holidayData.ifEmpty {
//            getHolidayOfYear(serviceKey, year, false, 0)
//        }
    }

    suspend fun checkHolidayDBOfYear(year: Int) =
        holidayDao.selectHolidayItemOfYear(year).ifEmpty { null }

    /**
     * calendar.get(Calendar.DAY_OF_WEEK)               : 그 주의 요일 변환 일:1 ~ 토:7
     * calendar.getActualMaximum(Calendar.DAY_OF_MONTH) : 해당 월의 날짜 수
     */
    @SuppressLint("SimpleDateFormat")
    private suspend fun getCalendarDate(holidayInfo: List<HolidayItem>) : ArrayList<CalendarData> {
        val calendar = Calendar.getInstance()
        val monthDataList = arrayListOf<CalendarData>()
        for (monthCount in -200..200) {
            calendar.time = Date() //오늘로 설정
            calendar.add(Calendar.MONTH, monthCount) // 해당 달로 이동
            val tempMonth = calendar.get(Calendar.MONTH) + 1    // 이번 년도 값
            val tempYear = calendar.get(Calendar.YEAR)         //이번 달 값
            val calendarList = arrayListOf<CalendarDayData>()
            calendar.set(Calendar.DAY_OF_MONTH, 1)   // 달의 첫번째로 이동
            val lastDay = calendar.getActualMaximum(Calendar.DAY_OF_MONTH) //달의 날짜 수
            val firstWeek = calendar.get(Calendar.DAY_OF_WEEK)  //날짜의 요일
            //지난달 날짜 구하기
            if (firstWeek != 1) {
                calendar.add(Calendar.DAY_OF_MONTH, (1 - firstWeek))
                for (i in 0 until firstWeek - 1) {
                    addCalendarList(
                        inputList = calendarList,
                        holidayInfo = holidayInfo,
                        calendar = calendar,
                        day = calendar.get(Calendar.DAY_OF_MONTH).toString(),
                        isCurrentMonth = false
                    )
                    calendar.add(Calendar.DAY_OF_MONTH, 1)
                }
            }
            for (i in 1..lastDay) {
                addCalendarList(
                    inputList = calendarList,
                    holidayInfo = holidayInfo,
                    calendar = calendar,
                    day = i.toString(),
                    isCurrentMonth = true
                )
                if (i != lastDay) {
                    calendar.add(Calendar.DAY_OF_MONTH, 1)
                } //마지막 날에 add 1 하면 다음달로 넘어가 버린다.
            }
            calendar.set(Calendar.DAY_OF_MONTH, lastDay)
            val lastWeek = calendar.get(Calendar.DAY_OF_WEEK)
            if (lastWeek != 7) {
                for (i in 7 downTo lastWeek + 1) {
                    calendar.add(Calendar.DAY_OF_MONTH, 1)
                    addCalendarList(
                        inputList = calendarList,
                        holidayInfo = holidayInfo,
                        calendar = calendar,
                        day = calendar.get(Calendar.DAY_OF_MONTH).toString(),
                        isCurrentMonth = false
                    )
                }
            }
            monthDataList.add(
                CalendarData(
                    year = tempYear, month = tempMonth, dayList = calendarList
                )
            )
        }
        calendar.time = Date()
        return monthDataList
    }

    private fun addCalendarList(
        inputList: ArrayList<CalendarDayData>,
        holidayInfo: List<HolidayItem>,
        day: String,
        calendar: Calendar,
        isCurrentMonth: Boolean
    ) {
        val tempY = calendar.get(Calendar.YEAR)
        val tempM = calendar.get(Calendar.MONTH) + 1
        var tempHolidayName: String? = null
        var tempIsHoliday = false
        checkHolidayItem(
            holidayInfo, getYearToDayFormat(
                year = tempY,
                month = tempM,
                day = calendar.get(Calendar.DAY_OF_MONTH)
            )
        )?.let {
            tempHolidayName = it.holidayName
            tempIsHoliday= it.isHolidayBoolean
        }
        inputList.add(
            CalendarDayData(
                year = tempY.toString(),
                month = tempM.toString(),
                day = day,
                weekCount = calendar.get(Calendar.DAY_OF_WEEK),
                isCurrentMonth = isCurrentMonth,
                holidayName = tempHolidayName,
                isHoliday = tempIsHoliday
            )
        )
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

}