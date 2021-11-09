package com.dwstyle.calenderbydw.calendardacorator

import android.app.Activity
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.text.style.LineBackgroundSpan
import android.util.Log
import com.dwstyle.calenderbydw.R
import com.dwstyle.calenderbydw.item.TaskItem
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.DayViewDecorator
import com.prolificinteractive.materialcalendarview.DayViewFacade

class SelectDecorator : DayViewDecorator {

    private lateinit var selectDrawable : Drawable

    constructor(context :Activity){
        selectDrawable=context.resources.getDrawable(R.drawable.select_background)

    }

    override fun shouldDecorate(day: CalendarDay): Boolean {
        return true
    }

    override fun decorate(view: DayViewFacade) {
        //시작하자마자 decorate가 생기기 때문에 조건문 처리가 어려움 점을 여러개 생성하기 원할 경우 클래스 여러개 생성 필요
        view.setSelectionDrawable(selectDrawable)
    }
}