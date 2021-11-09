package com.dwstyle.calenderbydw.calendardacorator

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.Log
import com.prolificinteractive.materialcalendarview.spans.DotSpan

class CustomDotSpan : DotSpan {
    private var color=0
    private var xOffset=0
    private var radius = 8f;

    private lateinit var colors: String

    constructor() {
        this.color=0
        this.xOffset=0
    }

    constructor(color:Int,xOffset:Int){
        this.color=color
        this.xOffset=xOffset
    }

    constructor(colors :String,xOffset: Int){
        this.colors=colors
        this.xOffset=xOffset
    }
    var a=1;
    var b=2;
    private var colorList=ArrayList<String>()

    constructor(colorList :ArrayList<String>){
        this.colorList=colorList
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
//        super.drawBackground(canvas,paint,left,right,top,baseline,bottom,charSequence,start,end,lineNum)
        lateinit var colorStr :List<String>
//        Log.d("도원","TaskDotDecorator.getDay() :${TaskDotDecorator.getDay()}")
//        Log.d("도원","TaskDotDecorator.getDay() :${TaskDotDecorator.getDay()}")
        for ((c, color) in colorList.withIndex()){
//            if (a==c){
                colorStr=color.split("&");
//            Log.d("도원","colorStr :${colorStr}")
//            Log.d("도원","colorStr : ${(colorStr[0].toInt())==TaskDotDecorator.current}")
//            }else if (b==c){
//                colorStr=color.split("&");
//            }
        }
//        Log.d("도원","color1 = ${colorStr} ")
//        val oldColor =paint.color
//
//        var x :Float = ((left+right)/2f)
//
//        for ((c,color) in colorStr.withIndex()){
//            paint.color= Color.parseColor(color)
//            canvas.drawCircle(x+xOffset+20f*c,bottom+radius,radius,paint)
//            paint.color = oldColor
//        }
//        Log.d("도원","color1 = ${color} ")
////        if (color!=0){
//
//            Log.d("도원","color 2= ${paint.color}   || colorStr : ${colorStr[0]}")
////        }
//        canvas.drawCircle(x+xOffset-20f,bottom+radius,radius,paint)
//        paint.color = oldColor
//        Log.d("도원","color3 = ${color} ")
////        if (color!=0){
//            paint.color= Color.parseColor(colorStr[1])
//            Log.d("도원","color4 = ${paint.color}   || colorStr : ${colorStr[1]}")
////        }
//        canvas.drawCircle(x+xOffset,bottom+radius,radius,paint)
//        paint.color = oldColor
//        Log.d("도원","color5 = ${color} ")
////        if (color!=0){
//            paint.color= Color.parseColor(colorStr[2])
//            Log.d("도원","color6 = ${paint.color}   || colorStr : ${colorStr[2]}")
////        }
//        canvas.drawCircle(x+xOffset+20f,bottom+radius,radius,paint)
//        paint.color = oldColor

    }

}