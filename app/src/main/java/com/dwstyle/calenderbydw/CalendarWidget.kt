package com.dwstyle.calenderbydw

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.widget.Button
import android.widget.FrameLayout
import android.widget.RemoteViews
import com.dwstyle.calenderbydw.adapters.WidgetAdapter

/**
 * Implementation of App Widget functionality.
 */
class CalendarWidget : AppWidgetProvider() {
    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        // There may be multiple widgets active, so update all of them
//        for (appWidgetId in appWidgetIds) {
//            updateAppWidget(context, appWidgetManager, appWidgetId)
//        }
        val views = RemoteViews(context.packageName, R.layout.calendar_widget)
        val intent =Intent(context,WidgetAdapter::class.java)
        Log.d("도원","app ids : ${appWidgetIds.size}")
        val appWidgetId = appWidgetIds[0]
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,appWidgetId)
        intent.data = Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME))

        views.setRemoteAdapter(R.id.gvCalendar,intent)

        views.setEmptyView(R.id.gvCalendar,R.id.tvTask)
        views.setOnClickPendingIntent(R.id.calendarContainer, getPendingSelfIntent(context,"2234","gi~?"))

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views)



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
            val appWidgetManager = AppWidgetManager.getInstance(context)
            Log.d("도원"," ??intent Code : ${intent?.action}")
        }
    }

}

internal fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
    // Construct the RemoteViews object
    val views = RemoteViews(context.packageName, R.layout.calendar_widget)
    val intent =Intent(context,WidgetAdapter::class.java)
    intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,appWidgetId)

    intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)))

    views.setRemoteAdapter(R.id.gvCalendar,intent)

    views.setEmptyView(R.id.gvCalendar,R.id.tvTask)

    views.setOnClickPendingIntent(R.id.tvTopDate,getPendingSelfIntent(context,"2234","12.55"))

    // Instruct the widget manager to update the widget
    appWidgetManager.updateAppWidget(appWidgetId, views)
}

fun getPendingSelfIntent (context: Context,code1 :String, code:String): PendingIntent {
    val intent : Intent = Intent(context,CalendarWidget::class.java)
    intent.action = code1
    intent.putExtra("date",code)
    return PendingIntent.getBroadcast(context,0,intent,0)
}