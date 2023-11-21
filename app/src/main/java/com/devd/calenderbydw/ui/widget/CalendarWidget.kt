package com.devd.calenderbydw.ui.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.RemoteViews
import com.devd.calenderbydw.MainActivity
import com.devd.calenderbydw.R
import com.devd.calenderbydw.data.local.AppDatabase
import com.devd.calenderbydw.data.local.calendar.YearMonthDayData
import com.devd.calenderbydw.data.local.dao.TaskDao
import com.devd.calenderbydw.data.local.entity.CalendarDayEntity
import com.devd.calenderbydw.data.local.entity.TaskDBEntity
import com.devd.calenderbydw.databinding.CalendarWidgetBinding
import com.devd.calenderbydw.repository.TaskRepository
import com.devd.calenderbydw.utils.ConstVariable.WIDGET_SHOW_DATE
import com.devd.calenderbydw.utils.SharedDataUtils
import com.google.android.gms.tasks.Task
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import okhttp3.internal.wait
import org.joda.time.DateTime
import timber.log.Timber
import java.util.Calendar
import java.util.Date
import javax.inject.Inject
import kotlin.properties.Delegates

/**
 * Implementation of App Widget functionality.
 */
@AndroidEntryPoint
class CalendarWidget : AppWidgetProvider() {

    @Inject
    lateinit var appDatabase: AppDatabase

    private val selectDate = YearMonthDayData()
    private var calendarData = listOf<CalendarDayEntity>()

    companion object {
        const val COLLECTION_VIEW_EXTRA = "com.devd.calenderbydw.COLLECTION_VIEW_EXTRA"
        const val RECEIVE_ADAPTER = "AdapterData"
        private var currentReceiveDate = ""

    }

    init {
        Timber.d("CheckData init :${calendarData}")
    }
    //위젯이 설치 될 때 마다 호출되는 함수
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        // There may be multiple widgets active, so update all of them
        super.onUpdate(context, appWidgetManager, appWidgetIds)
        appWidgetIds.forEach { ids ->
            Timber.d("checkData -> onUpdate")
            updateAppWidget(context, appWidgetManager, ids)
        }
    }

    override fun onEnabled(context: Context?) {
        super.onEnabled(context)
        val calendar = Calendar.getInstance().apply {
            time = Date()
        }
        Timber.d("checkData -> onEnabled")
        getCalendarDate(context,calendar.get(Calendar.YEAR))
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        super.onReceive(context, intent)
        if ("MoveToday" == intent?.action) {
            context?.let {
                val originalDate = DateTime(System.currentTimeMillis())
                val newDate = originalDate.withDayOfMonth(1)
                SharedDataUtils.setClickDate(it, "${originalDate.toString("YYYY.MM.dd.E")}")
                setCalendarMillis(it, newDate.millis)
                val manager = AppWidgetManager.getInstance(it)
                manager.notifyAppWidgetViewDataChanged(
                    manager.getAppWidgetIds(ComponentName(it, CalendarWidget::class.java)),
                    R.id.gvCalendar
                )
                this.onUpdate(
                    it,
                    manager,
                    manager.getAppWidgetIds(ComponentName(it, CalendarWidget::class.java))
                )
            }
        } else if ("PreMonth" == intent?.action) {
            context?.let {
                val originalDate = DateTime(getCalendarSharedData(it))
                val newDate = originalDate.minusMonths(1)
                setCalendarMillis(it, newDate.millis)
                val manager = AppWidgetManager.getInstance(it)
                manager.notifyAppWidgetViewDataChanged(
                    manager.getAppWidgetIds(ComponentName(it, CalendarWidget::class.java)),
                    R.id.gvCalendar
                )
                this.onUpdate(
                    it,
                    manager,
                    manager.getAppWidgetIds(ComponentName(it, CalendarWidget::class.java))
                )
                Log.d("도원", "pre onUpdate ${getCalendarSharedData(it)}")
            }
        } else if ("NextMonth" == intent?.action) {
            context?.let {
                val originalDate = DateTime(getCalendarSharedData(it))
                val newDate = originalDate.plusMonths(1)
                setCalendarMillis(it, newDate.millis)
                val manager = AppWidgetManager.getInstance(it)
                manager.notifyAppWidgetViewDataChanged(
                    manager.getAppWidgetIds(ComponentName(it, CalendarWidget::class.java)),
                    R.id.gvCalendar
                )
                this.onUpdate(
                    it,
                    manager,
                    manager.getAppWidgetIds(ComponentName(it, CalendarWidget::class.java))
                )
                Log.d("도원", "next onUpdate ${getCalendarSharedData(it)}")
            }
        } else if (RECEIVE_ADAPTER == intent?.action) {
            context?.let {
                val receiveDate = intent.getStringExtra(COLLECTION_VIEW_EXTRA)
                Log.d("도원", " Code : $receiveDate  =>> currentReceiveDate : $currentReceiveDate")
                if (currentReceiveDate == receiveDate) {
                    val mainIntent = Intent(it, MainActivity::class.java)
                    mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    mainIntent.putExtra("sendDate", currentReceiveDate)
                    it.startActivity(mainIntent)
                } else {
                    if (receiveDate != null) {
                        SharedDataUtils.setClickDate(it, receiveDate)
                        currentReceiveDate = receiveDate
                        val manager = AppWidgetManager.getInstance(it)
                        manager.notifyAppWidgetViewDataChanged(
                            manager.getAppWidgetIds(ComponentName(it, CalendarWidget::class.java)),
                            R.id.gvCalendar
                        )
                        this.onUpdate(
                            it,
                            manager,
                            manager.getAppWidgetIds(ComponentName(it, CalendarWidget::class.java))
                        )
                    }
                }

            }
        } else if ("Recycler" == intent?.action || AppWidgetManager.ACTION_APPWIDGET_UPDATE == intent?.action) {
            context?.let {
                val manager = AppWidgetManager.getInstance(it)
                manager.notifyAppWidgetViewDataChanged(
                    manager.getAppWidgetIds(ComponentName(it, CalendarWidget::class.java)),
                    R.id.gvCalendar
                )
                this.onUpdate(
                    it,
                    manager,
                    manager.getAppWidgetIds(ComponentName(it, CalendarWidget::class.java))
                )
            }
        }
    }

    private fun updateAppWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int
    ) {
        // Construct the RemoteViews object
        val views = RemoteViews(context.packageName, R.layout.calendar_widget)
        //위젯 클릭 기능
        widgetClickFunc(views, context)
        //그리드 뷰에 어댑터 셋팅
        widgetCalendarGridSetting(views, context, appWidgetId)
        //저장된 달력 시간 저장
        val showDateTime = DateTime(getCalendarSharedData(context))
        //상단 날짜 저장
        views.setTextViewText(
            R.id.tvTopDate,
            "${showDateTime.year}.${showDateTime.monthOfYear}.${showDateTime.dayOfMonth}"
        )
        //업데이트 요청
        appWidgetManager.updateAppWidget(appWidgetId, views)
    }

    private fun widgetClickFunc(views: RemoteViews, context: Context) {
        views.setOnClickPendingIntent(
            R.id.btnPreMonth,
            getPendingSelfIntent(context, "PreMonth", "??")
        )
        views.setOnClickPendingIntent(
            R.id.btnNextMonth,
            getPendingSelfIntent(context, "NextMonth", "??")
        )
        views.setOnClickPendingIntent(
            R.id.ivMoveToToday,
            getPendingSelfIntent(context, "MoveToday", "??")
        )
        views.setOnClickPendingIntent(
            R.id.ivSearchDate,
            getPendingSelfIntent(context, "Recycler", "??")
        )
    }

    private fun widgetCalendarGridSetting(views: RemoteViews, context: Context, widgetId: Int) {
        //gvCalendar 달력 뷰에 데이터 보내기
        val intent = Intent(context, WidgetAdapter::class.java)
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId)
        intent.putExtra(WIDGET_SHOW_DATE, getCalendarSharedData(context))
        Timber.d("CheckData calendarData :${calendarData}")
        intent.putExtra("calendarDate", Gson().toJson(calendarData))
        views.setRemoteAdapter(R.id.gvCalendar, intent)
        views.setEmptyView(R.id.gvCalendar, R.id.tvTask1)

        //어댑터 터치 기능 셋팅
        val gridIntent = Intent(context, CalendarWidget::class.java)
        gridIntent.action = RECEIVE_ADAPTER
        gridIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId)
        gridIntent.data = Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME))
        val pendingIntent =
            PendingIntent.getBroadcast(context, 0, gridIntent, PendingIntent.FLAG_MUTABLE)
        views.setPendingIntentTemplate(R.id.gvCalendar, pendingIntent)
    }

    private fun getPendingSelfIntent(context: Context, code1: String, code: String): PendingIntent {
        val intent = Intent(context, CalendarWidget::class.java)
        intent.action = code1
        intent.putExtra("date", code)
        return PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)
    }

    private fun getCalendarSharedData(context: Context): Long {
        var showTime by Delegates.notNull<Long>()
        showTime = if (SharedDataUtils.getDateMillis(context) == 0L) {
            SharedDataUtils.setDateMillis(context, System.currentTimeMillis())
            System.currentTimeMillis()
        } else {
            SharedDataUtils.getDateMillis(context)
        }
        return showTime
    }

    private fun setCalendarMillis(context: Context, millis: Long) {
        SharedDataUtils.setDateMillis(context, millis)
    }

    private fun getCalendarDate(context:Context?,checkYear: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            //일정과 달력 데이터를 가져와서 merge후 calendarData에 담는다
            val taskList = appDatabase.taskDao().getAllTaskForWidget()
            val tempCalendarData = appDatabase.calendarDao()
                .getAllCalendarData(checkYear, checkYear)
                .filter { it.month == Calendar.MONTH + 1 }
            if (tempCalendarData.isNotEmpty()) {
                tempCalendarData[0].dayList.forEach { dayDate ->
                    val taskItem = taskList.filter { task ->
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
                Timber.d("checkData -> isnotEmptY???context : ${context}???????2222 : ${tempCalendarData[0].dayList} ")
                calendarData = tempCalendarData[0].dayList
                context?.let {
                    val manager = AppWidgetManager.getInstance(it)
                    manager.notifyAppWidgetViewDataChanged(
                        manager.getAppWidgetIds(ComponentName(it, CalendarWidget::class.java)),
                        R.id.gvCalendar
                    )
                    onUpdate(
                        it,
                        manager,
                        manager.getAppWidgetIds(ComponentName(it, CalendarWidget::class.java))
                    )
                }
            }else{
                Timber.d("checkData -> isnotEmptY??????????")
            }
        }
    }
}



