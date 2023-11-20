package com.devd.calenderbydw.repository

import android.annotation.SuppressLint
import com.devd.calenderbydw.data.local.dao.CalendarDao
import com.devd.calenderbydw.data.local.dao.HolidayDao
import com.devd.calenderbydw.data.local.entity.CalendarDayEntity
import com.devd.calenderbydw.data.local.entity.CalendarMonthEntity
import com.devd.calenderbydw.data.remote.CallResult
import com.devd.calenderbydw.data.remote.api.HolidayService
import com.devd.calenderbydw.data.remote.holiday.HolidayItem
import com.devd.calenderbydw.di.NetworkModule
import com.devd.calenderbydw.utils.SafeNetCall
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Date

class CalendarRepository(
    @NetworkModule.HolidayServer private val holidayService: HolidayService,
    private val holidayDao: HolidayDao,
    private val calendarDao: CalendarDao
) : SafeNetCall() {

    suspend fun getHolidayOfYearApi(
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

    suspend fun getCalendarDataInDB(
        startIndex: Int,
        endIndex: Int
    ): List<CalendarMonthEntity> {
        return getCalendarDate(startIndex, endIndex)
    }
    suspend fun getCalendarAllData() = calendarDao.getAllCalendar()
    //달력 생성
    suspend fun insertCalendarDateInDB(holidayList : List<HolidayItem>,currentYear:Int,progressCount: (pro: Int) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            val calendar = Calendar.getInstance()
            var proValue = 0
            val monthDataList = arrayListOf<CalendarMonthEntity>()
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            for (monthCount in -1200..1200) {
                if (proValue != (((monthCount + 1200) / 2400f) * 100).toInt()) {
                    proValue = (((monthCount + 1200) / 2400f) * 100).toInt()
                    progressCount(if (proValue == 100) 99 else proValue)
                }
                val dayDataList = arrayListOf<CalendarDayEntity>()
                calendar.time = Date()
                calendar.add(Calendar.MONTH, monthCount)                             // 해당 달로 이동
                val tempMonth = calendar.get(Calendar.MONTH) + 1                // 이번 년도 값
                val tempYear = calendar.get(Calendar.YEAR)                      // 이번 달 값
                calendar.set(Calendar.DAY_OF_MONTH, 1)                              // 달의 첫번째로 이동
                val lastDay = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)  // 달의 날짜 수
                val firstWeek = calendar.get(Calendar.DAY_OF_WEEK)              // 날짜의 요일
                //지난달 날짜 구하기
                if (firstWeek != 1) {
                    calendar.add(Calendar.DAY_OF_MONTH, (1 - firstWeek))
                    for (i in 0 until firstWeek - 1) {
                        addCalendarEntityList(
                            inputList = dayDataList,
                            calendar = calendar,
                            day = calendar.get(Calendar.DAY_OF_MONTH).toString(),
                            isCurrentMonth = false
                        )
                        calendar.add(Calendar.DAY_OF_MONTH, 1)
                    }
                }
                for (i in 1..lastDay) {
                    addCalendarEntityList(
                        inputList = dayDataList,
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
                        addCalendarEntityList(
                            inputList = dayDataList,
                            calendar = calendar,
                            day = calendar.get(Calendar.DAY_OF_MONTH).toString(),
                            isCurrentMonth = false
                        )
                    }
                }
                monthDataList.add(
                    CalendarMonthEntity(
                        year = tempYear,
                        month = tempMonth,
                        dayList = dayDataList
                    )
                )
            }
            //DB정보에 공휴일 정보 넣기
            val mergeHoliday = if (holidayList.isNotEmpty()) {
                monthDataList.map {
                    if (currentYear == it.year){
                        it.dayList.forEach { dayData ->
                            var tempHolidayName: String? = null
                            var tempIsHoliday = false
                            checkHolidayItem(
                                holidayList, getYearToDayFormat(
                                    year = dayData.year.toInt(),
                                    month = dayData.month.toInt(),
                                    day = dayData.day.toInt()
                                )
                            )?.let {
                                tempHolidayName = it.holidayName
                                tempIsHoliday = it.isHolidayBoolean
                            }
                            dayData.holidayName = tempHolidayName
                            dayData.isHoliday = tempIsHoliday
                        }
                    }
                    it
                }
            } else {
                monthDataList
            }
            calendarDao.insertCalendarItemList(mergeHoliday).run {
                CoroutineScope(Dispatchers.Main).launch {
                    progressCount(100)
                }
            }
        }
    }

    suspend fun updateCalendarChangeHoliday(calendarList:List<CalendarMonthEntity>, holidayList: List<HolidayItem>,currentYear: Int){
        val mergeHoliday = if (holidayList.isNotEmpty()) {
            calendarList.map {
                if (currentYear == it.year){
                    it.dayList.forEach { dayData ->
                        var tempHolidayName: String? = null
                        var tempIsHoliday = false
                        checkHolidayItem(
                            holidayList, getYearToDayFormat(
                                year = dayData.year.toInt(),
                                month = dayData.month.toInt(),
                                day = dayData.day.toInt()
                            )
                        )?.let {
                            tempHolidayName = it.holidayName
                            tempIsHoliday = it.isHolidayBoolean
                        }
                        dayData.holidayName = tempHolidayName
                        dayData.isHoliday = tempIsHoliday
                    }
                }
                it
            }
        } else {
            calendarList
        }
        updateCalendarData(mergeHoliday)
    }

    suspend fun updateCalendarData(calendarList:List<CalendarMonthEntity>){
        calendarDao.updateCalendarDataAddHoliday(calendarList)
    }

    private fun addCalendarEntityList(
        inputList: ArrayList<CalendarDayEntity>,
        day: String,
        calendar: Calendar,
        isCurrentMonth: Boolean
    ) {
        inputList.add(
            CalendarDayEntity(
                year = calendar.get(Calendar.YEAR).toString(),
                month =(calendar.get(Calendar.MONTH) + 1).toString(),
                day = day,
                weekCount = calendar.get(Calendar.DAY_OF_WEEK),
                isCurrentMonth = isCurrentMonth,
                dateTimeLong = calendar.timeInMillis
            )
        )
    }

    suspend fun checkHolidayDBOfYear(year: Int) =
        holidayDao.selectHolidayItemOfYear(year).ifEmpty { null }

    /**
     * calendar.get(Calendar.DAY_OF_WEEK)               : 그 주의 요일 변환 일:1 ~ 토:7
     * calendar.getActualMaximum(Calendar.DAY_OF_MONTH) : 해당 월의 날짜 수
     */
    @SuppressLint("SimpleDateFormat")
    private suspend fun getCalendarDate(
        startYear: Int,
        endYear: Int
    ): List<CalendarMonthEntity> {
        val calendarData = calendarDao.getAllCalendarData(startYear, endYear)
//        calendarData.forEach {
//            it.dayList.forEach { day ->
//                Timber.d(" calendarData : ${day.year} :${day.month} : ${day.day} : ${day.holidayName} : ${day.isHoliday}")
//            }
//        }
        return calendarData
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