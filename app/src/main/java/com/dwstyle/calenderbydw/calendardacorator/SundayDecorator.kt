package com.dwstyle.calenderbydw.calendardacorator

import android.graphics.Color
import android.text.style.ForegroundColorSpan
import android.text.style.LineBackgroundSpan
import android.util.Log
import com.dwstyle.calenderbydw.R
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.DayViewDecorator
import com.prolificinteractive.materialcalendarview.DayViewFacade
import com.prolificinteractive.materialcalendarview.MaterialCalendarView
import com.prolificinteractive.materialcalendarview.spans.DotSpan
import java.util.*

class SundayDecorator() : DayViewDecorator {
    override fun shouldDecorate(day: CalendarDay): Boolean {
        return day.date.dayOfWeek.value== 7;
    }

    override fun decorate(view: DayViewFacade?) {
        view?.addSpan(ForegroundColorSpan(Color.RED))
    }
}