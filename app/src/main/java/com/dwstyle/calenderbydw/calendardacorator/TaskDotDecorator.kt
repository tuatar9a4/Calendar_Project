package com.dwstyle.calenderbydw.calendardacorator

import android.graphics.Color
import android.text.style.LineBackgroundSpan
import android.util.Log
import com.dwstyle.calenderbydw.item.TaskItem
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.DayViewDecorator
import com.prolificinteractive.materialcalendarview.DayViewFacade

class TaskDotDecorator : DayViewDecorator {
//    private val taskList2=taskList;
    var taskCnt=1;
    private lateinit var taskList :HashMap<Int,String>;

    private var color=ArrayList<String>()


    constructor(taskList : HashMap<Int,String>){
        this.taskList=taskList
        for (key in taskList.keys){
            color.add("${key}&"+taskList.get(key)!!)
        }
    }

    override fun shouldDecorate(day: CalendarDay): Boolean {
        if (taskList.containsKey(day.day)){
            Log.d("도원","${day.day}")
            current=day.day
            return true
        }
        return false
//        for (taskDay in taskList2){
//            if (taskDay.day==day.day && taskDay.month==day.month){
//                if (taskCnt==3)break
//                taskCnt++;
//            }
//        }
//        return when(taskCnt){
//            0 -> {
//                false
//            }
//            else -> {
//                true
//            }
//        }
    }

    override fun decorate(view: DayViewFacade) {
        //시작하자마자 decorate가 생기기 때문에 조건문 처리가 어려움 점을 여러개 생성하기 원할 경우 클래스 여러개 생성 필요
        val span1 : LineBackgroundSpan = CustomDotSpan(color)
        view.addSpan(span1)
//        when(taskCnt){
//            1->{
//                val span1 : LineBackgroundSpan = CustomDotSpan(Color.DKGRAY,0)
//                view.addSpan(span1)
//            }
//            2->{
//                val span1 : LineBackgroundSpan = CustomDotSpan(Color.DKGRAY,-10)
//                val span2 : LineBackgroundSpan = CustomDotSpan(Color.DKGRAY,10)
//                view.addSpan(span1)
//                view.addSpan(span2)
//            }
//            3->{
//                val span1 : LineBackgroundSpan = CustomDotSpan(Color.DKGRAY,-20)
//                val span2 : LineBackgroundSpan = CustomDotSpan(Color.DKGRAY,0)
//                val span3 : LineBackgroundSpan = CustomDotSpan(Color.DKGRAY,20)
//                view.addSpan(span1)
//                view.addSpan(span2)
//                view.addSpan(span3)
//            }
//        }
    }

    companion object {
        var current=0
        fun getDay() :Int {
            return current
        }
    }

}