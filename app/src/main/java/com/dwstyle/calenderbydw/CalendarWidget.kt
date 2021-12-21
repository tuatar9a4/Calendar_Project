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
import org.joda.time.DateTime

/**
 * Implementation of App Widget functionality.
 */
class CalendarWidget : AppWidgetProvider() {

    companion object{
        public final const val COLLECTION_VIEW_ACTION="com.dwstyle.calenderbydw.COLLECTION_VIEW_ACTION"
        public final const val COLLECTION_VIEW_EXTRA="com.dwstyle.calenderbydw.COLLECTION_VIEW_EXTRA"
        public final const val BT_REFRESH_ACTION="com.dwstyle.calenderbydw.BT_REFRESH_ACTION"
        public final const val RECEIVE_ADAPTER="AdapterData"

    }

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
        Log.d("도원"," ??intent Code : ${intent?.action}")
        if ("2234".equals(intent?.action)){
            val appWidgetManager = AppWidgetManager.getInstance(context);
            val testWidge = ComponentName(context!!, CalendarWidget::class.java);
            val widgetIds = appWidgetManager.getAppWidgetIds(testWidge)
            this.onUpdate(context,appWidgetManager,widgetIds)
        }else if (RECEIVE_ADAPTER == intent?.action){
            val viewIndex = intent.getStringExtra(COLLECTION_VIEW_EXTRA);
            Log.d("도원"," Code : ${viewIndex}")
        }
    }

}

internal fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
    // Construct the RemoteViews object
    val views = RemoteViews(context.packageName, R.layout.calendar_widget)
    views.setOnClickPendingIntent(R.id.tvTopDate, getPendingSelfIntent(context,"2234","gi~?"))


    //그리드 뷰에 어댑터 셋팅
    val intent =Intent(context,WidgetAdapter::class.java)
    intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,appWidgetId)
//    intent.data = Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME))
    views.setRemoteAdapter(appWidgetId,R.id.gvCalendar,intent)

    //어댑터 터치 기능 셋팅
    val gridIntent =Intent(context,CalendarWidget::class.java)
    gridIntent.action = RECEIVE_ADAPTER
    gridIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,appWidgetId)
    gridIntent.data = Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME))
    val pendingIntent = PendingIntent.getBroadcast(context,0,gridIntent,PendingIntent.FLAG_UPDATE_CURRENT)
    views.setPendingIntentTemplate(R.id.gvCalendar,pendingIntent)


    views.setTextViewText(R.id.tvTopDate,"${DateTime().toLocalDate().year}.${DateTime().toLocalDate().monthOfYear}.${DateTime().toLocalDate().dayOfMonth}")

    // Instruct the widget manager to update the widget
    appWidgetManager.updateAppWidget(appWidgetId, views)
}

fun getPendingSelfIntent (context: Context,code1 :String, code:String): PendingIntent {
    val intent : Intent = Intent(context,CalendarWidget::class.java)
    intent.action = code1
    intent.putExtra("date",code)
    return PendingIntent.getBroadcast(context,0,intent,0)
}