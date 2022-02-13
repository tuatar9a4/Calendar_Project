package com.devd.calenderbydw.calendardacorator

import android.graphics.Color
import android.text.style.ForegroundColorSpan
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.DayViewDecorator
import com.prolificinteractive.materialcalendarview.DayViewFacade
import kotlin.collections.HashMap

class HolidayDecorator(private val holidayLists : HashMap<String,String>) : DayViewDecorator {


    override fun shouldDecorate(day: CalendarDay): Boolean {
        return holidayLists.keys.contains(day.date.dayOfMonth.toString());
    }

    override fun decorate(view: DayViewFacade?) {
        view?.addSpan(ForegroundColorSpan(Color.RED))
    }
}