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
import com.devd.calenderbydw.ui.calendar.CalendarMonthAdapter
import com.devd.calenderbydw.utils.ConstVariable
import com.devd.calenderbydw.utils.getDpValue
import timber.log.Timber


class CustomDayItemView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    @AttrRes private val defStyleAttr: Int = R.attr.itemViewStyle,
    @StyleRes private val defStyleRes: Int = R.style.Calendar_ItemViewStyle,
    private val dayDate: CalendarDayEntity = CalendarDayEntity(),
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

            linePaint.apply {
                color = context.getColor(R.color.grayColor)
                style = Paint.Style.STROKE
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        Timber.d("onDayClick onTouchEvent : ${event?.action}")
        return when(event?.action){
            MotionEvent.ACTION_DOWN->{
//                Timber.d("onDayClick ACTION_DOWN : ${listener!=null}")
//                listener?.onDayClick(dayDate.year.toInt(),dayDate.month.toInt(),dayDate.day.toInt())
                true
            }
            MotionEvent.ACTION_UP->{
                Timber.d("onDayClick asdasd ${listener!=null}")
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
        val task = "asdlkadflakflds"
        paint.getTextBounds(task, 0, task.length, taskBounds)
        val txt = TextUtils.ellipsize(
            task,
            taskPaint as TextPaint,
            taskBounds.width() - 80f,
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
        rect = RectF(0f,0f,width.toFloat(),height.toFloat())

        canvas.drawBitmap(
            bitmap, null,
            Rect(
                context.getDpValue(8f).toInt() + (imageSize * 2).toInt(),
                (bounds.height()+context.getDpValue(20f)+context.getDpValue(4f)+taskBounds.height()).toInt(),
                context.getDpValue(8f).toInt() + (imageSize * 3).toInt(),
                (bounds.height()+context.getDpValue(20f)+context.getDpValue(4f)+taskBounds.height()+(imageSize)).toInt()
            ),
            null
        )
        canvas.drawRect(rect, linePaint)
    }
}