package com.dwstyle.calenderbydw.calendardacorator

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.Log
import com.prolificinteractive.materialcalendarview.spans.DotSpan

class CustomDotSpan : DotSpan {
    private var color=0
    private var xOffset=0
    private var radius = 6f;

    private lateinit var colors: String
    private var colorList=ArrayList<String>()
    private var oldColorList=ArrayList<String>()
    private var oldTurn=false;
    private lateinit var mType :String
    constructor(colorList :ArrayList<String>,type : String){
        this.colorList=colorList
        mType=type
    }

    constructor(type : String){
        mType=type
    }

    override fun drawBackground(
        canvas: Canvas,
        paint: Paint,
        left: Int,
        right: Int,
        top: Int,
        baseline: Int,
        bottom: Int,
        charSequence: CharSequence,
        start: Int,
        end: Int,
        lineNum: Int
    ) {

        val oldColor =paint.color
        var x :Float = ((left+right)/2f)

//        Log.d("도원","1left: ${left} , right : ${right} , bottom :  ${bottom}, top : ${top} " +
//                ", start : ${start} , end : ${end} , lineNum : ${lineNum}, ");
        //디바이스마다 달력의 크기가 달라서 원 모양을 동적으로 조정
        val radius=right/12f
        if (mType.equals("Year")){
            //년간
            paint.color= Color.parseColor("#FF0000")
            canvas.drawCircle((right-radius)-(radius*99/70f)*2-6f,0f-radius,radius,paint)
        }else if (mType.equals("Month")){
            //월간
            paint.color= Color.parseColor("#0000FF")
            canvas.drawCircle((right-radius)-radius*99/70f-3f,0f-radius+radius*99/70f,radius,paint)
        }else if (mType.equals("Week")){
            //주간
            paint.color= Color.parseColor("#00FF00")
            canvas.drawCircle(right-radius,0f-radius+(radius*99/70f)*2,radius,paint)
        }else{
            //반복 없음
            paint.color= Color.parseColor("#000000")
            canvas.drawCircle(x,bottom+(radius),radius,paint)
        }


//        //가운데 오른쪽
//        paint.color= Color.parseColor("#990000")
//        canvas.drawCircle(right-radius,(bottom+top)/2f,radius,paint)
//
//        //가운데 왼쪽
//        paint.color= Color.parseColor("#990099")
//        canvas.drawCircle(0+radius,(bottom+top)/2f,radius,paint)

        //가운데 아래


        paint.color = oldColor

    }

}