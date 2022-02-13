package com.devd.calenderbydw.calendardacorator

import android.graphics.Color
import android.text.style.ForegroundColorSpan
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.DayViewDecorator
import com.prolificinteractive.materialcalendarview.DayViewFacade

class RangeDayDecorator (cal : CalendarDay): DayViewDecorator {
    val cal =cal

    override fun shouldDecorate(day: CalendarDay): Boolean {
        return day.month == cal.month;
    }

    override fun decorate(view: DayViewFacade) {
        view.addSpan(ForegroundColorSpan(Color.parseColor("#000000")))
    }
}