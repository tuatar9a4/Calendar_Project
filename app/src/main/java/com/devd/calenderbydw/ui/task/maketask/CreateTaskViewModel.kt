package com.devd.calenderbydw.ui.task.maketask

import android.view.Gravity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
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
import com.devd.calenderbydw.utils.Event
import com.devd.calenderbydw.utils.getFullDay
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class CreateTaskViewModel @Inject constructor(
    private val taskRepository: TaskRepository,
) : ViewModel() {

    private val _insertResult = MutableLiveData<Event<Boolean>>()
    val insertResult: LiveData<Event<Boolean>> get() = _insertResult
    val selectDate = YearMonthDayData()
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
    private var modifyTaskItem: TaskDBEntity? = null
    lateinit var taskYearSheetList: List<BottomSheetItem>
    lateinit var taskMonthSheetList: List<BottomSheetItem>
    lateinit var taskDaySheetList: List<BottomSheetItem>


    fun setOriginDate(year: Int, month: Int, day: Int) {
        selectDate.year = year
        selectDate.month = month
        selectDate.day = day
        setDateSheetItems(year, month, day)
    }

    fun setModifyTaskInfo(taskItem: TaskDBEntity) {
        modifyTaskItem = taskItem
//        setTitle(taskItem.title)
//        setContents(taskItem.contents)
        setChangeRepeatState(taskItem.repeatType)
    }

    private fun setDateSheetItems(year: Int, month: Int, day: Int) {
        taskYearSheetList = getTaskYearSheetItems(year)
        taskMonthSheetList = getTaskMonthSheetItems(month)
        taskDaySheetList = getTaskDaySheetItems(year, month, day)
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
            _taskRepeatState.value = type
        }
    }

    fun setChangeSelectYear(year: String) {
        viewModelScope.launch {
            taskYearSheetList.forEach { item ->
                item.isCheck = item.text == year
            }
            selectDate.year = year.toInt()
            _taskYear.emit(year)
        }
    }

    fun setChangeSelectMonth(month: String) {
        viewModelScope.launch {
            taskMonthSheetList.forEach { item ->
                item.isCheck = item.text == month
            }
            selectDate.month = month.toInt()
            _taskYear.emit(month)
            taskDaySheetList =
                getTaskDaySheetItems(selectDate.year, selectDate.month, selectDate.day)
        }
    }

    fun setChangeSelectDay(day: String) {
        viewModelScope.launch {
            taskDaySheetList.forEach { item ->
                item.isCheck = item.text == day
            }
            selectDate.day = day.toInt()
            _taskYear.emit(day)
        }
    }

    fun insertTaskInDB() {
        viewModelScope.launch {
            val calendar = Calendar.getInstance().apply {
                set(
                    selectDate.year.toString().toInt(),
                    selectDate.month.toString().toInt() - 1,
                    selectDate.day.toString().toInt(),
                    0,
                    0,
                    0
                )
            }
            val startDate = calendar.time.time
            taskRepository.insertTaskItem(
                TaskDBEntity(
                    year = selectDate.year.toString(),
                    month = selectDate.month.toString(),
                    day = selectDate.day.toString(),
                    title = taskTitle.value,
                    contents = taskContents,
                    repeatType = taskRepeatState.value,
                    createDate = startDate,
                    weekCount = calendar.get(Calendar.DAY_OF_WEEK)
                )
            ).run {
                _insertResult.value = Event(true)
            }
        }
    }

    fun modifyTaskInDB() {
        viewModelScope.launch {
            modifyTaskItem?.let {
                val calendar = Calendar.getInstance().apply {
                    set(
                        selectDate.year.toString().toInt(),
                        selectDate.month.toString().toInt() - 1,
                        selectDate.day.toString().toInt(),
                        0,
                        0,
                        0
                    )
                }
                it.year = selectDate.year.toString()
                it.month = selectDate.month.toString()
                it.day = selectDate.day.toString()
                it.title = taskTitle.value
                it.contents = taskContents
                it.repeatType = taskRepeatState.value
                it.weekCount = calendar.get(Calendar.DAY_OF_WEEK)
                taskRepository.updateTaskItem(
                    it
                ).run {
                    _insertResult.value = Event(true)
                }
            }
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

    private fun getTaskDaySheetItems(
        currentYear: Int,
        currentMonth: Int,
        currentDay: Int
    ): List<BottomSheetItem> {
        val yearList = arrayListOf<BottomSheetItem>()
        val calendar = Calendar.getInstance()
        for (a in 1..calendar.getFullDay(currentYear, currentMonth)) {
            yearList.add(BottomSheetItem(a, a.toString(), a == currentDay, Gravity.CENTER))
        }
        return yearList
    }
}