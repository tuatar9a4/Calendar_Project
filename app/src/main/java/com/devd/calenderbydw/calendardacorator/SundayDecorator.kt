package com.devd.calenderbydw.calendardacorator

import android.graphics.Color
import android.text.style.ForegroundColorSpan
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.DayViewDecorator
import com.prolificinteractive.materialcalendarview.DayViewFacade

class SundayDecorator : DayViewDecorator {
    override fun shouldDecorate(day: CalendarDay): Boolean {
        return day.date.dayOfWeek.value== 7;
    }

    override fun decorate(view: DayViewFacade?) {
        view?.addSpan(ForegroundColorSpan(Color.RED))
    }
}