package com.dwstyle.calenderbydw.calendardacorator

import android.graphics.Color
import android.text.style.ForegroundColorSpan
import android.util.Log
import com.dwstyle.calenderbydw.R
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.DayViewDecorator
import com.prolificinteractive.materialcalendarview.DayViewFacade
import com.prolificinteractive.materialcalendarview.MaterialCalendarView

class OutOfRangeDecorator (cal : CalendarDay):DayViewDecorator {
    val cal =cal

    override fun shouldDecorate(day: CalendarDay): Boolean {
        return day.month != cal.month;
    }

    override fun decorate(view: DayViewFacade) {
        view.addSpan(ForegroundColorSpan(Color.parseColor("#808080")))
    }
}