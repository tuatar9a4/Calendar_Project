package com.dwstyle.calenderbydw.calendardacorator

import android.content.Context
import android.graphics.Color
import android.text.style.ForegroundColorSpan
import android.text.style.LineBackgroundSpan
import android.text.style.TextAppearanceSpan
import android.util.Log
import com.dwstyle.calenderbydw.R
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.DayViewDecorator
import com.prolificinteractive.materialcalendarview.DayViewFacade
import com.prolificinteractive.materialcalendarview.MaterialCalendarView
import com.prolificinteractive.materialcalendarview.spans.DotSpan
import java.util.*
import kotlin.collections.HashMap

class HolidayDecorator(private val holidayLists : HashMap<String,String>) : DayViewDecorator {


    override fun shouldDecorate(day: CalendarDay): Boolean {
        return holidayLists.keys.contains(day.date.dayOfMonth.toString());
    }

    override fun decorate(view: DayViewFacade?) {
        view?.addSpan(ForegroundColorSpan(Color.RED))
    }
}