package com.devd.calenderbydw.ui.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.RemoteViews
import com.devd.calenderbydw.MainActivity
import com.devd.calenderbydw.R
import com.devd.calenderbydw.repository.CalendarDataStore
import com.devd.calenderbydw.repository.DataStoreKey.Companion.PREF_KET_WIDGET_CLICK_DATE
import com.devd.calenderbydw.repository.DataStoreKey.Companion.PREF_KEY_WIDGET_SHOW_TIME
import com.devd.calenderbydw.utils.ConstVariable.WIDGET_SHOW_DATE
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.Calendar
import java.util.Date
import javax.inject.Inject

/**
 * Implementation of App Widget functionality.
 */
@AndroidEntryPoint
class CalendarWidget : AppWidgetProvider() {

    @Inject
    lateinit var dataStore: CalendarDataStore
    private var showTimeDate :Long? = null
    companion object {
        const val COLLECTION_VIEW_EXTRA = "com.devd.calenderbydw.COLLECTION_VIEW_EXTRA"
        const val RECEIVE_ADAPTER = "AdapterData"
        private var currentReceiveDate = ""

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
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        super.onReceive(context, intent)
        if ("MoveToday" == intent?.action) {
            context?.let {
                CoroutineScope(Dispatchers.IO).launch {
                    val calendar = Calendar.getInstance().apply {
                        time = Date()
                    }
                    dataStore.setPreferLong(PREF_KEY_WIDGET_SHOW_TIME,calendar.time.time)
                    requestUpdateWidget(it)
                }
            }
        } else if ("PreMonth" == intent?.action) {
            context?.let {
                CoroutineScope(Dispatchers.IO).launch {
                    changeWidgetTime(-1)
                    requestUpdateWidget(it)
                }
            }
        } else if ("NextMonth" == intent?.action) {
            context?.let {
                CoroutineScope(Dispatchers.IO).launch {
                    changeWidgetTime(1)
                    requestUpdateWidget(it)
                }
            }
        } else if (RECEIVE_ADAPTER == intent?.action) {
            context?.let {
                val receiveDate = intent.getStringExtra(COLLECTION_VIEW_EXTRA)
                if (currentReceiveDate == receiveDate) {
                    val mainIntent = Intent(it, MainActivity::class.java)
                    mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    mainIntent.putExtra("sendDate", currentReceiveDate)
                    it.startActivity(mainIntent)
                } else {
                    if (receiveDate != null) {
                        CoroutineScope(Dispatchers.IO).launch {
                            changeSelectTime(receiveDate)
                            requestUpdateWidget(it)
                        }
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
        CoroutineScope(Dispatchers.IO).launch {
            dataStore.getPreferLong(PREF_KEY_WIDGET_SHOW_TIME)?.let {
                showTimeDate=it
            }?: kotlin.run {
                dataStore.setPreferLong(PREF_KEY_WIDGET_SHOW_TIME,System.currentTimeMillis())
                showTimeDate=System.currentTimeMillis()
            }
            showTimeDate?.let {
                val showDateTime = Calendar.getInstance().apply {
                    time = Date(it)
                }
                Timber.d("Check? :${showTimeDate}=> ${showDateTime.get(Calendar.YEAR)}.${showDateTime.get(Calendar.MONTH)+1}")
                //상단 날짜 저장
                views.setTextViewText(
                    R.id.tvTopDate,
                    "${showDateTime.get(Calendar.YEAR)}.${showDateTime.get(Calendar.MONTH)+1}"
                )
            }
            //업데이트 요청
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
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
        intent.putExtra(WIDGET_SHOW_DATE, showTimeDate)
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

    private fun requestUpdateWidget(context: Context) {
        val manager = AppWidgetManager.getInstance(context)
        val widgetIds = manager.getAppWidgetIds(ComponentName(context, CalendarWidget::class.java))
        manager.notifyAppWidgetViewDataChanged(widgetIds, R.id.gvCalendar)
        onUpdate(context, manager, widgetIds)
    }

    private suspend fun changeWidgetTime(isChangeNum:Int){
        val widgetShowTime = dataStore.getPreferLong(PREF_KEY_WIDGET_SHOW_TIME)
        widgetShowTime?.let {
            val calendar = Calendar.getInstance().apply {
                time = Date(it)
            }
            calendar.add(Calendar.MONTH,isChangeNum)
            showTimeDate=calendar.time.time
            dataStore.setPreferLong(PREF_KEY_WIDGET_SHOW_TIME,calendar.time.time)
        }?: kotlin.run {
            dataStore.setPreferLong(PREF_KEY_WIDGET_SHOW_TIME,System.currentTimeMillis())
        }
    }

    private suspend fun changeSelectTime(selectDate :String){
        dataStore.setPreferString(PREF_KET_WIDGET_CLICK_DATE,selectDate)
        currentReceiveDate = selectDate
    }
}



