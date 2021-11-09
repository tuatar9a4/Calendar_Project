package com.dwstyle.calenderbydw.calendardacorator

import android.graphics.Color
import android.text.style.ForegroundColorSpan
import android.util.Log
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.DayViewDecorator
import com.prolificinteractive.materialcalendarview.DayViewFacade
import java.util.*

class SaturdayDecorator() : DayViewDecorator{


    override fun shouldDecorate(day: CalendarDay): Boolean {
        return  day.date.dayOfWeek.value==6;

    }

    override fun decorate(view: DayViewFacade) {
        view.addSpan(ForegroundColorSpan(Color.BLUE))
    }
}