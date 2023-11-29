package com.devd.calenderbydw.ui.widget

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.view.View
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import com.devd.calenderbydw.R
import com.devd.calenderbydw.data.local.AppDatabase
import com.devd.calenderbydw.data.local.entity.CalendarDayEntity
import com.devd.calenderbydw.data.local.entity.TaskDBEntity
import com.devd.calenderbydw.di.calendarDataStore
import com.devd.calenderbydw.repository.CalendarDataStore
import com.devd.calenderbydw.repository.DataStoreKey
import com.devd.calenderbydw.repository.DataStoreKey.Companion.PREF_KEY_WIDGET_SHOW_TIME
import com.devd.calenderbydw.utils.ConstVariable.WEEK_SAT_DAY
import com.devd.calenderbydw.utils.ConstVariable.WEEK_SUN_DAY
import com.devd.calenderbydw.utils.ConstVariable.WIDGET_SHOW_DATE
import com.devd.calenderbydw.utils.changeWeekIntToString
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.Calendar
import java.util.Date

class WidgetAdapter : RemoteViewsService() {
    override fun onGetViewFactory(intent: Intent?): RemoteViewsFactory {
        return StackRemoteViewsFactory(this.applicationContext, intent)
    }
}

class StackRemoteViewsFactory(private val context: Context, private val intent: Intent?) :
    RemoteViewsService.RemoteViewsFactory {

    private lateinit var appDatabase: AppDatabase
    private lateinit var dataStore: CalendarDataStore
    private var widgetList: HashMap<String, List<CalendarDayEntity>> = hashMapOf()
    private var isDataLoading = false

    private var mAppWidgetId = 0

    private var todayDate = ""
    private var selectedDate: String? = null

    private var receiveTime: Long = 0L
    private var job = Job()
    private var coroutineScope = CoroutineScope(Dispatchers.IO + job)
    override fun onCreate() {
        dataStore = CalendarDataStore(context.calendarDataStore)
        if (intent != null) {
            mAppWidgetId = intent.getIntExtra(
                AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID
            )
            CoroutineScope(Dispatchers.IO).launch {
                Timber.d("onDataSetChnage showTimeDate22: ${intent.getLongExtra(WIDGET_SHOW_DATE, 0)}")
                receiveTime = dataStore.getPreferLong(PREF_KEY_WIDGET_SHOW_TIME)?:0L
                Timber.d("onDataSetChnage collect: ${receiveTime}")
            }
        }
        appDatabase = AppDatabase.buildDatabase(context)
        coroutineScope.launch {
            launch {
                dataStore.preferLongFlow(PREF_KEY_WIDGET_SHOW_TIME).collectLatest {
                    Timber.d("receiveTime collect222: ${receiveTime}")
                    it?.let {
                        receiveTime = it
                    }
                }
            }
            launch {
                dataStore.preferStringFlow(DataStoreKey.PREF_KET_WIDGET_CLICK_DATE).collectLatest {
                    Timber.d("selectedDate collect: ${receiveTime}")
                    selectedDate = it
                }
            }
        }
        val calendarDate = Calendar.getInstance().apply {
            time = Date(receiveTime)
        }
        setCalendarList(null, calendarDate.get(Calendar.YEAR))
        Timber.d("receiveTime collect22244: ${calendarDate.get(Calendar.YEAR)}")
    }

    override fun getViewAt(position: Int): RemoteViews {
        val rv = RemoteViews(context.packageName, R.layout.widget_calender_grid_item)
        val monthFirstDate = Calendar.getInstance().apply { time = Date(receiveTime) }
        Timber.d("getViewAt : ${monthFirstDate[Calendar.YEAR]}.${(monthFirstDate[Calendar.MONTH]+ 1)}")
//        Timber.d("WidgetTest getViewAt[${position}] ${monthFirstDate[Calendar.YEAR]}.${monthFirstDate[Calendar.MONTH] + 1}null? [${widgetList["${monthFirstDate[Calendar.YEAR]}.${monthFirstDate[Calendar.MONTH] + 1}"] == null}]")
//        Timber.d("onDataSetChnage ${monthFirstDate[Calendar.YEAR]}.${(monthFirstDate[Calendar.MONTH]+ 1)} => ${widgetList["${monthFirstDate[Calendar.YEAR]}.${monthFirstDate[Calendar.MONTH] + 1}"]}")
        widgetList["${monthFirstDate[Calendar.YEAR]}.${monthFirstDate[Calendar.MONTH]+ 1}"]?.let {
            when (position) {
                0, 1, 2, 3, 4, 5, 6 -> { // 요일 텍스트
                    setWeekContainer((position + 1).changeWeekIntToString(), rv)
                }

                else -> { //날짜 텍스트
                    setDayContainer(it[position - 7], rv)
                }
            }
            if (position > 6) {
                //보내는 intent
                val fillIntent = Intent()
                fillIntent.putExtra(
                    CalendarWidget.COLLECTION_VIEW_EXTRA,
                    "${it[position - 7].year}.${it[position - 7].month}.${it[position - 7].day}"
                )
                rv.setOnClickFillInIntent(R.id.calendarContainer, fillIntent)
            }
        }

        return rv
    }

    override fun onDataSetChanged() {
        val todayCalendar = Calendar.getInstance().apply { time = Date() }
        todayDate = "${todayCalendar.get(Calendar.YEAR)}.${todayCalendar.get(Calendar.MONTH) + 1}.${
            todayCalendar.get(Calendar.DAY_OF_MONTH)
        }"
        val monthFirstDate = Calendar.getInstance().apply { time = Date(receiveTime) }
        Timber.d("onDataSetChnage => ${widgetList.isEmpty()}")
        CoroutineScope(Dispatchers.IO).launch {
            updateCalendarTask(appDatabase.taskDao().getAllTaskForWidget())
            if (!widgetList.any { it.key == "${monthFirstDate.get(Calendar.YEAR) + 1}.1" } && !isDataLoading) {
                setCalendarList(true, (monthFirstDate.get(Calendar.YEAR) + 1))
            } else if (!widgetList.any { it.key == "${monthFirstDate.get(Calendar.YEAR) - 1}.12" } && !isDataLoading) {
                setCalendarList(false, (monthFirstDate.get(Calendar.YEAR) - 1))
            }
        }
    }

    override fun onDestroy() {
        job.cancel()
    }

    override fun getCount(): Int {
        val monthFirstDate = Calendar.getInstance().apply { time = Date(receiveTime) }
        widgetList["${monthFirstDate[Calendar.YEAR]}.${monthFirstDate[Calendar.MONTH] + 1}"]?.let {
            return it.size + 7
        } ?: kotlin.run {
            return 0
        }
    }

    private fun setWeekContainer(text: String, rv: RemoteViews) {
        rv.setViewVisibility(R.id.tvTask1, View.GONE)
        rv.setViewVisibility(R.id.tvTask2, View.GONE)
        rv.setViewVisibility(R.id.tvTaskCnt, View.GONE)
        rv.setViewVisibility(R.id.viewLine, View.VISIBLE)
        if (text == "Sun") {
            rv.setTextColor(R.id.tvDate, context.getColor(R.color.sunDayColor))
        } else if (text == "Sat") {
            rv.setTextColor(R.id.tvDate, context.getColor(R.color.satDayColor))
        }else {
            rv.setTextColor(R.id.tvDate, context.getColor(R.color.black))
        }
        rv.setTextViewText(R.id.tvDate, text)
    }

    private fun setDayContainer(item: CalendarDayEntity, rv: RemoteViews) {
        rv.setViewVisibility(R.id.tvTask1, View.INVISIBLE)
        rv.setViewVisibility(R.id.tvTask2, View.INVISIBLE)
        rv.setViewVisibility(R.id.tvTaskCnt, View.INVISIBLE)
        rv.setViewVisibility(R.id.viewLine, View.INVISIBLE)
        //날짜 입력
        rv.setTextViewText(R.id.tvDate, item.day)
        //오늘
        if (todayDate == "${item.year}.${item.month}.${item.day}") {
            rv.setInt(
                R.id.tvDate,
                "setBackgroundResource",
                R.drawable.widget_today_background_circle
            )
            rv.setTextColor(R.id.tvDate, context.getColor(R.color.white))
        } else {
            rv.setInt(
                R.id.tvDate,
                "setBackgroundResource",
                R.drawable.widget_basic_background_circle
            )
            if (!item.isCurrentMonth) { // 다른달
                rv.setTextColor(R.id.tvDate, context.getColor(R.color.widget_gray_day))
            } else if (item.isHoliday) { // 휴일
                rv.setTextColor(R.id.tvDate, context.getColor(R.color.sunDayColor))
            } else { //같은달
                when (item.weekCount) {
                    WEEK_SUN_DAY -> {
                        rv.setTextColor(R.id.tvDate, context.getColor(R.color.sunDayColor))
                    }

                    WEEK_SAT_DAY -> {
                        rv.setTextColor(R.id.tvDate, context.getColor(R.color.satDayColor))
                    }

                    else -> {
                        rv.setTextColor(R.id.tvDate, context.getColor(R.color.black))
                    }
                }
            }
        }
        //선택 배경
        selectedDate?.let {
            if (it == "${item.year}.${item.month}.${item.day}") {
                rv.setInt(
                    R.id.calendarContainer,
                    "setBackgroundResource",
                    R.drawable.widget_container_background_select
                )
            } else {
                rv.setInt(
                    R.id.calendarContainer,
                    "setBackgroundResource",
                    R.drawable.widget_container_background_basic
                )
            }
        }
        if (item.taskTitle.isNotEmpty()) {
            rv.setTextViewText(R.id.tvTask1, item.taskTitle)
            rv.setTextColor(R.id.tvTask1, Color.BLACK)
            rv.setViewVisibility(R.id.tvTask1, View.VISIBLE)
        } else {
            rv.setViewVisibility(R.id.tvTask1, View.INVISIBLE)
        }

        if (!item.taskSecondTitle.isNullOrEmpty()) {
            rv.setTextViewText(R.id.tvTask2, item.taskSecondTitle)
            rv.setTextColor(R.id.tvTask2, Color.BLACK)
            rv.setViewVisibility(R.id.tvTask2, View.VISIBLE)
        } else {
            rv.setViewVisibility(R.id.tvTask2, View.INVISIBLE)
        }

        if (item.taskCount > 2) {
            rv.setTextViewText(R.id.tvTaskCnt, "+${(item.taskCount - 2)}")
            rv.setViewVisibility(R.id.tvTaskCnt, View.VISIBLE)
        } else {
            rv.setViewVisibility(R.id.tvTaskCnt, View.INVISIBLE)
        }
        if (!item.holidayName.isNullOrEmpty()) {
            rv.setTextViewText(R.id.tvHoliday, item.holidayName)
            rv.setViewVisibility(R.id.tvHoliday, View.VISIBLE)
        } else {
            rv.setViewVisibility(R.id.tvHoliday, View.GONE)
        }
    }

    override fun getLoadingView(): RemoteViews? {
        return RemoteViews(context.packageName, R.layout.blank_layout)
    }

    override fun getViewTypeCount(): Int {
        return 1
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun hasStableIds(): Boolean {
        return true
    }

    private fun setCalendarList(isNextYear: Boolean?, startIndex: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            isDataLoading = true
            val taskData = appDatabase.taskDao().getAllTaskForWidget()
            val tempStartIndex =
                if (isNextYear == null) startIndex - 1 else if (isNextYear) startIndex else startIndex - 1
            val tempendIndex =
                if (isNextYear == null) startIndex + 1 else if (isNextYear) startIndex + 1 else startIndex
            appDatabase.calendarDao().getAllCalendarData(tempStartIndex, tempendIndex).let {
                it.forEach { monthData ->
                    val item = monthData.dayList.map { dayDate ->
                        val taskItem = taskData.filter { task ->
                            if (dayDate.dateTimeLong < task.createDate) {
                                false
                            } else {
                                when (task.repeatType) {
                                    TaskDBEntity.DAILY_REPEAT -> {
                                        true
                                    }

                                    TaskDBEntity.WEEK_REPEAT -> {
                                        dayDate.weekCount == task.weekCount
                                    }

                                    TaskDBEntity.MONTH_REPEAT -> {
                                        dayDate.day == task.day
                                    }

                                    TaskDBEntity.YEAR_REPEAT -> {
                                        dayDate.month == task.month && dayDate.day == task.day
                                    }

                                    else -> {
                                        dayDate.year == task.year && dayDate.month == task.month && dayDate.day == task.day
                                    }
                                }
                            }
                        }
                        taskItem.forEachIndexed { index, taskDBEntity ->
                            when (index) {
                                0 -> {
                                    dayDate.taskTitle = taskDBEntity.title
                                }

                                1 -> {
                                    dayDate.taskSecondTitle = taskDBEntity.title
                                }

                                else -> {
                                    return@forEachIndexed
                                }
                            }
                        }
                        dayDate.taskCount = taskItem.size
                        dayDate
                    }
                    widgetList["${monthData.year}.${monthData.month}"] = item
                }
            }
            isDataLoading = false
        }
    }

    private fun updateCalendarTask(taskData :List<TaskDBEntity>){
        widgetList.forEach { (_, calendarDayEntities) ->
            calendarDayEntities.forEach { dayDate ->
                val taskItem = taskData.filter { task ->
                    if (dayDate.dateTimeLong < task.createDate) {
                        false
                    } else {
                        when (task.repeatType) {
                            TaskDBEntity.DAILY_REPEAT -> {
                                true
                            }

                            TaskDBEntity.WEEK_REPEAT -> {
                                dayDate.weekCount == task.weekCount
                            }

                            TaskDBEntity.MONTH_REPEAT -> {
                                dayDate.day == task.day
                            }

                            TaskDBEntity.YEAR_REPEAT -> {
                                dayDate.month == task.month && dayDate.day == task.day
                            }

                            else -> {
                                dayDate.year == task.year && dayDate.month == task.month && dayDate.day == task.day
                            }
                        }
                    }
                }
                taskItem.forEachIndexed { index, taskDBEntity ->
                    when (index) {
                        0 -> {
                            dayDate.taskTitle = taskDBEntity.title
                        }

                        1 -> {
                            dayDate.taskSecondTitle = taskDBEntity.title
                        }

                        else -> {
                            return@forEachIndexed
                        }
                    }
                }
                dayDate.taskCount = taskItem.size
            }
        }
    }
}