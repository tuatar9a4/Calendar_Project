package com.devd.calenderbydw.ui.calendar.custom

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.text.TextPaint
import android.text.TextUtils
import android.util.AttributeSet
import android.view.ContextThemeWrapper
import android.view.MotionEvent
import android.view.View
import androidx.annotation.AttrRes
import androidx.annotation.StyleRes
import androidx.core.content.res.ResourcesCompat
import androidx.core.content.withStyledAttributes
import com.devd.calenderbydw.R
import com.devd.calenderbydw.data.local.entity.CalendarDayEntity
import com.devd.calenderbydw.data.local.entity.TaskDBEntity
import com.devd.calenderbydw.ui.calendar.CalendarMonthAdapter
import com.devd.calenderbydw.utils.ConstVariable
import com.devd.calenderbydw.utils.getDpValue


class CustomDayItemView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    @AttrRes private val defStyleAttr: Int = R.attr.itemViewStyle,
    @StyleRes private val defStyleRes: Int = R.style.Calendar_ItemViewStyle,
    private val dayDate: CalendarDayEntity = CalendarDayEntity(),
    private val taskList :List<TaskDBEntity>,
    private val listener : CalendarMonthAdapter.CalendarClickListener? = null
) : View(ContextThemeWrapper(context, defStyleRes), attrs, defStyleAttr) {

    private var paint: Paint = Paint()
    private val bounds = Rect()

    private var taskPaint : Paint = Paint()
    private val taskBounds = Rect()

    private var linePaint : Paint = Paint()
    private var rect = RectF()

    private lateinit var bitmap :Bitmap

    private var imageSize = context.getDpValue(15f)

    private var taskTitle = ""
    private var sticker = ""
    init {
        /* Attributes */
        context.withStyledAttributes(attrs, R.styleable.CalendarView, defStyleAttr, defStyleRes) {
            val dayTextSize = getDimensionPixelSize(R.styleable.CalendarView_dayTextSize, 0).toFloat()
            val taskTextSize = getDimensionPixelSize(R.styleable.CalendarView_taskTextSize, 0).toFloat()
            val typeface2 = ResourcesCompat.getFont(context,R.font.inchen_font)

            /* 흰색 배경에 유색 글씨 */
            paint = TextPaint().apply {
                isAntiAlias = true
                textSize = dayTextSize
                typeface = typeface2
                color = setDateTextView(dayDate.isCurrentMonth,dayDate.isHoliday,dayDate.weekCount)
            }

            taskPaint = TextPaint().apply {
                isAntiAlias = true
                textSize = taskTextSize
                typeface = typeface2
                color = context.getColor(R.color.commonDayColor)
                isLinearText =true
            }

            bitmap = BitmapFactory.decodeResource(resources,R.drawable.test_icon)
            Bitmap.createScaledBitmap(bitmap,2,2,false)

            val taskItem = taskList.filter { task ->
                if (dayDate.dateTimeLong < task.createDate) {
                    false
                } else {
                    when (task.repeatType) {
                        TaskDBEntity.DAILY_REPEAT -> {
                            true
                        }
                        TaskDBEntity.WEEK_REPEAT -> {
                            dayDate.weekCount == task.weekCount
                        }
                        TaskDBEntity.MONTH_REPEAT -> {
                            dayDate.day == task.day
                        }
                        TaskDBEntity.YEAR_REPEAT -> {
                            dayDate.month == task.month && dayDate.day == task.day
                        }
                        else -> {
                            dayDate.year == task.year && dayDate.month == task.month && dayDate.day == task.day
                        }
                    }
                }
            }
            if(taskItem.isNotEmpty()){
//                Timber.d("task1!!!!${dayDate.year},${dayDate.month},${dayDate.day} ${taskTitle} ")
                taskTitle = taskItem[0].title
                sticker = "testIcon"
            }
            linePaint.apply {
                color = context.getColor(R.color.gray_default)
                style = Paint.Style.STROKE
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        return when(event?.action){
            MotionEvent.ACTION_DOWN->{
//                Timber.d("onDayClick ACTION_DOWN : ${listener!=null}")
//                listener?.onDayClick(dayDate.year.toInt(),dayDate.month.toInt(),dayDate.day.toInt())
                true
            }
            MotionEvent.ACTION_UP->{
                listener?.onDayClick(dayDate.year.toInt(),dayDate.month.toInt(),dayDate.day.toInt())
                true
            }

            else->{
                super.onTouchEvent(event)
            }
        }
    }
    private fun setDateTextView(isCurrentMonth :Boolean,isHoliday:Boolean,weekCount:Int) :Int{
        return if(!isCurrentMonth){
            context.getColor(R.color.grayDayColor)
        }else if(isHoliday || weekCount == ConstVariable.WEEK_SUN_DAY){
            context.getColor(R.color.sunDayColor)
        }else if(weekCount == ConstVariable.WEEK_SAT_DAY){
            context.getColor(R.color.satDayColor)
        }else{
            context.getColor(R.color.commonDayColor)
        }
    }

    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        //today 배경 처리
        if(dayDate.toDay){
            setBackgroundColor(context.getColor(R.color.palette_green_200))
        }else{
            setBackgroundColor(context.getColor(R.color.white))
        }
        //날짜 텍스트 그리기
        val date = if(dayDate.isHoliday){
            "${dayDate.day}[${dayDate.holidayName}]"
        }else{
            dayDate.day
        }
        paint.getTextBounds(date, 0, date.length, bounds)
        canvas.drawText(
            date,
            context.getDpValue(4f),
            context.getDpValue(20f),
            paint
        )
        // 일정 텍스트 그리기
        val task = taskTitle
        paint.getTextBounds(task, 0, task.length, taskBounds)
        val txt = TextUtils.ellipsize(
            task,
            taskPaint as TextPaint,
            width - 30f,
            TextUtils.TruncateAt.END
        )
        canvas.drawText(
            txt,
            0,
            txt.length,
            context.getDpValue(4f),
            bounds.height()+context.getDpValue(20f)+context.getDpValue(4f),
            taskPaint
        )
        // 스티커 이미지 그리기
//        if(sticker=="testIcon"){
//            canvas.drawBitmap(
//                bitmap, null,
//                Rect(
//                    context.getDpValue(8f).toInt() + (imageSize * 2).toInt(),
//                    (bounds.height()+context.getDpValue(20f)+context.getDpValue(4f)+taskBounds.height()).toInt(),
//                    context.getDpValue(8f).toInt() + (imageSize * 3).toInt(),
//                    (bounds.height()+context.getDpValue(20f)+context.getDpValue(4f)+taskBounds.height()+(imageSize)).toInt()
//                ),
//                null
//            )
//        }
        rect = RectF(0f,0f,width.toFloat(),height.toFloat()-2)
        canvas.drawRect(rect, linePaint)
    }
}