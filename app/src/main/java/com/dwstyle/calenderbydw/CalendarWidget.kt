package com.dwstyle.calenderbydw

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.RemoteViews
import com.dwstyle.calenderbydw.CalendarWidget.Companion.RECEIVE_ADAPTER
import com.dwstyle.calenderbydw.adapters.WidgetAdapter
import com.dwstyle.calenderbydw.utils.SharedDataUtils
import org.joda.time.DateTime
import kotlin.properties.Delegates

/**
 * Implementation of App Widget functionality.
 */
class CalendarWidget : AppWidgetProvider() {

    companion object{
        const val COLLECTION_VIEW_EXTRA="com.dwstyle.calenderbydw.COLLECTION_VIEW_EXTRA"
        const val RECEIVE_ADAPTER="AdapterData"
        private var currentReceiveDate =""

    }

    //위젯이 설치 될 때 마다 호출되는 함수
    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        // There may be multiple widgets active, so update all of them
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
        super.onUpdate(context, appWidgetManager, appWidgetIds)

    }

    override fun onEnabled(context: Context) {
        // Enter relevant functionality for when the first widget is created
    }

    override fun onDisabled(context: Context) {
        // Enter relevant functionality for when the last widget is disabled
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        super.onReceive(context, intent)
        if ("MoveToday" == intent?.action){
            context?.let {
                val originalDate = DateTime(System.currentTimeMillis())
                val newDate = originalDate.withDayOfMonth(1)
                SharedDataUtils.setClickDate(it,"${originalDate.toString("YYYY.MM.dd.E")}")
                setCalendarMillis(it,newDate.millis)
                val manager =AppWidgetManager.getInstance(it)
                manager.notifyAppWidgetViewDataChanged(manager.getAppWidgetIds(ComponentName(it, CalendarWidget::class.java)),R.id.gvCalendar)
                this.onUpdate(it,manager,manager.getAppWidgetIds(ComponentName(it, CalendarWidget::class.java)))
            }
        }
        else if("PreMonth" == intent?.action){
            context?.let {
                val originalDate = DateTime(getCalendarSharedData(it))
                val newDate = originalDate.minusMonths(1)
                setCalendarMillis(it,newDate.millis)
                val manager =AppWidgetManager.getInstance(it)
                manager.notifyAppWidgetViewDataChanged(manager.getAppWidgetIds(ComponentName(it, CalendarWidget::class.java)),R.id.gvCalendar)
                this.onUpdate(it,manager,manager.getAppWidgetIds(ComponentName(it, CalendarWidget::class.java)))
                Log.d("도원","pre onUpdate ${getCalendarSharedData(it)}")
            }
        }
        else if("NextMonth"==intent?.action){
            context?.let {
                val originalDate = DateTime(getCalendarSharedData(it))
                val newDate = originalDate.plusMonths(1)
                setCalendarMillis(it,newDate.millis)
                val manager =AppWidgetManager.getInstance(it)
                manager.notifyAppWidgetViewDataChanged(manager.getAppWidgetIds(ComponentName(it, CalendarWidget::class.java)),R.id.gvCalendar)
                this.onUpdate(it,manager,manager.getAppWidgetIds(ComponentName(it, CalendarWidget::class.java)))
                Log.d("도원","next onUpdate ${getCalendarSharedData(it)}")
            }
        }
        else if (RECEIVE_ADAPTER == intent?.action){
            context?.let {
                val receiveDate = intent.getStringExtra(COLLECTION_VIEW_EXTRA)
                Log.d("도원"," Code : $receiveDate  =>> currentReceiveDate : $currentReceiveDate")
                if (currentReceiveDate == receiveDate){
                    val mainIntent = Intent(it,MainActivity::class.java)
                    mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    mainIntent.putExtra("sendDate",currentReceiveDate)
                    it.startActivity(mainIntent)
                }else{
                    if (receiveDate != null) {
                        SharedDataUtils.setClickDate(it,receiveDate)
                        currentReceiveDate=receiveDate
                        val manager =AppWidgetManager.getInstance(it)
                        manager.notifyAppWidgetViewDataChanged(manager.getAppWidgetIds(ComponentName(it, CalendarWidget::class.java)),R.id.gvCalendar)
                        this.onUpdate(it,manager,manager.getAppWidgetIds(ComponentName(it, CalendarWidget::class.java)))

                    }
                }

            }
        }else if ("Recycler"==intent?.action){
            context?.let {
                val manager =AppWidgetManager.getInstance(it)
                manager.notifyAppWidgetViewDataChanged(manager.getAppWidgetIds(ComponentName(it, CalendarWidget::class.java)),R.id.gvCalendar)
                this.onUpdate(it,manager,manager.getAppWidgetIds(ComponentName(it, CalendarWidget::class.java)))
            }
        }
    }

}

internal fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
    // Construct the RemoteViews object
    val views = RemoteViews(context.packageName, R.layout.calendar_widget)

    views.setOnClickPendingIntent(R.id.btnPreMonth, getPendingSelfIntent(context,"PreMonth","??"))

    views.setOnClickPendingIntent(R.id.btnNextMonth, getPendingSelfIntent(context,"NextMonth","??"))

    views.setOnClickPendingIntent(R.id.ivMoveToToday, getPendingSelfIntent(context,"MoveToday","??"))

    views.setOnClickPendingIntent(R.id.ivSearchDate, getPendingSelfIntent(context,"Recycler","??"))
    //그리드 뷰에 어댑터 셋팅
    val intent =Intent(context,WidgetAdapter::class.java)
    intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,appWidgetId)
    intent.putExtra("showDate",getCalendarSharedData(context))
//    intent.data = Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME))
    views.setRemoteAdapter(R.id.gvCalendar,intent)
    views.setEmptyView(R.id.gvCalendar,R.id.tvTask1)
    //어댑터 터치 기능 셋팅
    val gridIntent =Intent(context,CalendarWidget::class.java)
    gridIntent.action = RECEIVE_ADAPTER
    gridIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,appWidgetId)
    gridIntent.data = Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME))
    val pendingIntent = PendingIntent.getBroadcast(context,0,gridIntent,PendingIntent.FLAG_UPDATE_CURRENT)
    views.setPendingIntentTemplate(R.id.gvCalendar,pendingIntent)
    val showDateTime = DateTime(getCalendarSharedData(context))
    views.setTextViewText(R.id.tvTopDate,"${showDateTime.year}.${showDateTime.monthOfYear}.${showDateTime.dayOfMonth}")

    // Instruct the widget manager to update the widget
    appWidgetManager.updateAppWidget(appWidgetId, views)
}

fun getPendingSelfIntent (context: Context,code1 :String, code:String): PendingIntent {
    val intent = Intent(context,CalendarWidget::class.java)
    intent.action = code1
    intent.putExtra("date",code)
    return PendingIntent.getBroadcast(context,0,intent,0)
}

fun getCalendarSharedData(context : Context ) : Long{
//    val widgetPreferences = context.getSharedPreferences("WidgetData",Context.MODE_PRIVATE)
//    val editor = widgetPreferences.edit()
    var showTime by Delegates.notNull<Long>()
    showTime = if (SharedDataUtils.getDateMillis(context)==0L){
        SharedDataUtils.setDateMillis(context,System.currentTimeMillis())
//        editor.putLong("ShowCalendarMillis",System.currentTimeMillis());
//        editor.commit()
        System.currentTimeMillis()
        }else{
    //        showTime=widgetPreferencesetPreferences.getLong("ShowCalendarMillis",0)
            SharedDataUtils.getDateMillis(context)
        }
    return showTime
}

private fun setCalendarMillis(context : Context,millis : Long){
    SharedDataUtils.setDateMillis(context,millis)
//    val widgetPreferences = context.getSharedPreferences("WidgetData",Context.MODE_PRIVATE)
//    val editor = widgetPreferences.edit()
//    editor.putLong("ShowCalendarMillis",millis);
//    editor.commit()

}