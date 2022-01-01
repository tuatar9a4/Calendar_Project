package com.dwstyle.calenderbydw.utils

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import com.dwstyle.calenderbydw.CalendarWidget

class WidgetUtils {

    companion object{

        fun updateWidgetData(context: Context){
            val widgetIntent = Intent(context, CalendarWidget::class.java)
            widgetIntent.action= AppWidgetManager.ACTION_APPWIDGET_UPDATE
            context.sendBroadcast(widgetIntent)
        }
    }
}