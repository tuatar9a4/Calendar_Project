package com.devd.calenderbydw.calendardacorator

import android.annotation.SuppressLint
import android.app.Activity
import android.graphics.drawable.Drawable
import android.text.style.LineBackgroundSpan
import com.devd.calenderbydw.R
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.DayViewDecorator
import com.prolificinteractive.materialcalendarview.DayViewFacade

class TaskDotDecorator : DayViewDecorator {
//    private val taskList2=taskList;
    private var taskList =HashSet<String>();
    private var mType :String
    private var month =0
    private var context :Activity
    private var selectDrawable : Drawable
    private var weekRepeat = HashSet<String>()



    @SuppressLint("UseCompatLoadingForDrawables")
    constructor(context: Activity, taskList : HashSet<String>, month :Int, type :String){
        this.taskList=taskList
        this.context=context
        selectDrawable=context.resources.getDrawable(R.drawable.select_ractangle,null)
        mType=type
        this.month=month
        if (mType.equals("Week")){
            for (a in taskList){
                val weekStr=a.split("&")
                checkWeekRepeat(weekStr)
            }
        }

    }

    override fun shouldDecorate(day: CalendarDay): Boolean {
//        Log.d("도원","CalendarDay : ${day.month}.${day.day} }")
        if (mType.equals("Year")){
            if (taskList.contains("${day.month}.${day.day}") && month==day.month){
//                Log.d("도원","${day.day}  | ${day.month} | ${day.date.dayOfWeek} | }")
                return true
            }
        }else if (mType.equals("Month")){
            if (taskList.contains("${day.day}") && month==day.month){
//                Log.d("도원","${day.day}  | ${day.month} | ${day.date.dayOfWeek} | }")
                return true
            }
        }else if (mType.equals("Week")){
//            Log.d("도원","${day.date.dayOfWeek} == ${day.date.dayOfWeek.value}  |  ${weekRepeat}}")
            if (weekRepeat.contains("${day.date.dayOfWeek.value}")  && month==day.month){
                return true
            }
        }else if (mType.equals("No")){
            if (taskList.contains("${day.year}.${day.month}.${day.day}") && month==day.month){
//                Log.d("도원","${day.day}  | ${day.month} | ${day.date.dayOfWeek} | }")
                return true
            }
        }
//        if (taskList.containsKey(day.day.toString()) && month==day.month){
//            Log.d("도원","${day.day}  | ${day.month} | ${day.date.dayOfWeek} | }")
//            return true
//        }
        return false
    }

    fun checkWeekRepeat(weekStr : List<String>){
       for ( a in 0.. weekStr.size){
           when(a){
               0-> if (weekStr[a].equals("1")) weekRepeat.add("7") //SUN
               1-> if (weekStr[a].equals("1")) weekRepeat.add("1") //MON
               2-> if (weekStr[a].equals("1")) weekRepeat.add("2") //TUE
               3-> if (weekStr[a].equals("1")) weekRepeat.add("3") //WEN
               4-> if (weekStr[a].equals("1")) weekRepeat.add("4") //THU
               5-> if (weekStr[a].equals("1")) weekRepeat.add("5") //FRI
               6-> if (weekStr[a].equals("1")) weekRepeat.add("6") //SAT
           }
       }

    }

    override fun decorate(view: DayViewFacade) {
        //시작하자마자 decorate가 생기기 때문에 조건문 처리가 어려움 점을 여러개 생성하기 원할 경우 클래스 여러개 생성 필요
        val span1 : LineBackgroundSpan = CustomDotSpan(mType)
        view.addSpan(span1)
        view.setSelectionDrawable(selectDrawable)
    }
}