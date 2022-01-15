package com.dwstyle.calenderbydw.calendardacorator

import android.content.Context
import android.graphics.Color
import android.text.style.ForegroundColorSpan
import com.dwstyle.calenderbydw.R
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.DayViewDecorator
import com.prolificinteractive.materialcalendarview.DayViewFacade

class TodayDecorator(private val context: Context?,private val calendarDay: CalendarDay) : DayViewDecorator {
    override fun shouldDecorate(day: CalendarDay?): Boolean {
        if (day==null){
            return false
        }
        return day.year==calendarDay.year && day.month==calendarDay.month && day.day==calendarDay.day

    }

    override fun decorate(view: DayViewFacade?) {
        view?.addSpan(ForegroundColorSpan(Color.WHITE))
//        context?.getDrawable(R.drawable.today_background)?.let {
//            view?.setBackgroundDrawable(it)
//        }
        context?.getDrawable(R.drawable.today_selection_background)?.let {
            view?.setSelectionDrawable(it)
        }

    }

}