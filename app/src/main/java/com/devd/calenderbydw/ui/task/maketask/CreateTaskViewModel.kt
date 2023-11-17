package com.devd.calenderbydw.ui.task.maketask

import android.view.Gravity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.devd.calenderbydw.data.local.calendar.YearMonthDayData
import com.devd.calenderbydw.data.local.dialog.BottomSheetItem
import com.devd.calenderbydw.data.local.entity.TaskDBEntity
import com.devd.calenderbydw.data.local.entity.TaskDBEntity.Companion.DAILY_REPEAT
import com.devd.calenderbydw.data.local.entity.TaskDBEntity.Companion.MONTH_REPEAT
import com.devd.calenderbydw.data.local.entity.TaskDBEntity.Companion.NO_REPEAT
import com.devd.calenderbydw.data.local.entity.TaskDBEntity.Companion.WEEK_REPEAT
import com.devd.calenderbydw.data.local.entity.TaskDBEntity.Companion.YEAR_REPEAT
import com.devd.calenderbydw.repository.TaskRepository
import com.devd.calenderbydw.utils.getFullDay
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.Calendar
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class CreateTaskViewModel @Inject constructor(
    private val taskRepository: TaskRepository,
) : ViewModel() {
    val originDate = YearMonthDayData()
    private val _taskTitle = MutableStateFlow("")
    val taskTitle: StateFlow<String> = _taskTitle
    private val _taskYear = MutableSharedFlow<String>()
    val taskYear: SharedFlow<String> = _taskYear
    private val _taskMonth = MutableSharedFlow<String>()
    val taskMonth: SharedFlow<String> = _taskMonth
    private val _taskDay = MutableSharedFlow<String>()
    val taskDay: SharedFlow<String> = _taskDay
    private var _taskRepeatState = MutableStateFlow(NO_REPEAT)
    val taskRepeatState: StateFlow<Int> = _taskRepeatState
    private var taskContents: String? = null
    val taskRepeatSheetList = listOf(
        BottomSheetItem(NO_REPEAT, "반복없음", true, null),
        BottomSheetItem(DAILY_REPEAT, "매일", false, null),
        BottomSheetItem(WEEK_REPEAT, "매주", false, null),
        BottomSheetItem(MONTH_REPEAT, "매달", false, null),
        BottomSheetItem(YEAR_REPEAT, "매년", false, null),
    )
    lateinit var taskYearSheetList: List<BottomSheetItem>
    lateinit var taskMonthSheetList: List<BottomSheetItem>
    lateinit var taskDaySheetList: List<BottomSheetItem>


    fun setOriginDate(year: Int, month: Int, day: Int) {
        originDate.year = year
        originDate.month = month
        originDate.day = day
        setDateSheetItems(year,month,day)
    }
    private fun setDateSheetItems(year: Int, month: Int, day: Int){
        taskYearSheetList = getTaskYearSheetItems(year)
        taskMonthSheetList = getTaskMonthSheetItems(month)
        taskDaySheetList = getTaskDaySheetItems(year,month,day)
    }

    fun setTitle(title: String) {
        _taskTitle.value = title
    }

    fun setContents(contents: String?) {
        taskContents = contents?.ifEmpty { null }
    }

    fun setChangeRepeatState(type: Int) {
        viewModelScope.launch {
            taskRepeatSheetList.forEach { item ->
                item.isCheck = item.type == type
            }
            Timber.d("RepeatType 111=> ${type}")
            _taskRepeatState.value =type
        }
    }

    fun setChangeSelectYear(year:String){
        viewModelScope.launch {
            taskYearSheetList.forEach { item ->
                item.isCheck = item.text == year
            }
            originDate.year=year.toInt()
            _taskYear.emit(year)
        }
    }
    fun setChangeSelectMonth(month:String){
        viewModelScope.launch {
            taskMonthSheetList.forEach { item ->
                item.isCheck = item.text == month
            }
            originDate.year=month.toInt()
            _taskYear.emit(month)
            taskDaySheetList = getTaskDaySheetItems(originDate.year,originDate.month,originDate.day)
        }
    }
    fun setChangeSelectDay(day:String){
        viewModelScope.launch {
            taskDaySheetList.forEach { item ->
                item.isCheck = item.text == day
            }
            originDate.year=day.toInt()
            _taskYear.emit(day)
        }
    }

    fun insertTaskInDB() {
        viewModelScope.launch {
            val startDate = Calendar.getInstance().apply {
                set(
                    originDate.year.toString().toInt(),
                    originDate.month.toString().toInt(),
                    originDate.day.toString().toInt(),
                    0,
                    0,
                    0
                )
            }.time.time
            taskRepository.insertTaskItem(
                TaskDBEntity(
                    year = originDate.year.toString(),
                    month = originDate.month.toString(),
                    day = originDate.day.toString(),
                    title = taskTitle.value,
                    contents = taskContents,
                    repeatType = taskRepeatState.value,
                    createDate = startDate
                )
            )
        }
    }

    fun getTaskYearSheetItems(currentYear: Int): List<BottomSheetItem> {
        val yearList = arrayListOf<BottomSheetItem>()
        for (a in currentYear - 100..currentYear + 100) {
            yearList.add(BottomSheetItem(a, a.toString(), a == currentYear, Gravity.CENTER))
        }
        return yearList
    }
    private fun getTaskMonthSheetItems(currentMonth: Int): List<BottomSheetItem> {
        val yearList = arrayListOf<BottomSheetItem>()
        for (a in 1..12) {
            yearList.add(BottomSheetItem(a, a.toString(), a == currentMonth, Gravity.CENTER))
        }
        return yearList
    }
    private fun getTaskDaySheetItems(currentYear: Int,currentMonth :Int,currentDay:Int): List<BottomSheetItem> {
        val yearList = arrayListOf<BottomSheetItem>()
        val calendar = Calendar.getInstance()
        for (a in 1..calendar.getFullDay(currentYear,currentMonth)) {
            yearList.add(BottomSheetItem(a, a.toString(), a == currentDay, Gravity.CENTER))
        }
        return yearList
    }
}