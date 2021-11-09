package com.dwstyle.calenderbydw.utils

import android.app.DatePickerDialog
import android.app.Dialog
import android.app.TimePickerDialog
import android.content.Context
import android.util.Log
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.*
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.MutableLiveData
import com.dwstyle.calenderbydw.R
import com.dwstyle.calenderbydw.item.TaskItem
import com.prolificinteractive.materialcalendarview.CalendarDay
import java.text.SimpleDateFormat
import java.util.*
import kotlin.time.milliseconds

class MakeTaskDialog(context: Context) {

    private lateinit var tvTaskDate:TextView
    private lateinit var tvTaskTime:TextView
    private lateinit var edtTaskText:EditText
    private lateinit var tvRepeat:TextView
    private lateinit var radioRepeat:RadioGroup
    private lateinit var repeatWeekBox : LinearLayout
    private lateinit var repeatMon :CheckBox
    private lateinit var repeatTue:CheckBox
    private lateinit var repeatWEN:CheckBox
    private lateinit var repeatThu:CheckBox
    private lateinit var repeatFri:CheckBox
    private lateinit var repeatSat:CheckBox
    private lateinit var repeatSun:CheckBox
    private lateinit var btnCancel:Button
    private lateinit var btnWrite :Button

    var taskWeek=0;
    private val sendTaskLiveData =MutableLiveData<String>()

    private val context=context;
    private lateinit var dialog :Dialog

    // DB로 보낼 정보 변수들
    private var sendYear =2021
    private var sendMonth =12
    private var sendDay =15
    private var sendWeek = "0"
    private var sendTime = "03:22"
    private var sendTimemllis :Long= 0
    private var sendText = "Task Example"
    private var sendNotice =0
    private var sendRepeatY =0
    private var sendRepeatM =0
    private var sendRepeatW =0
    private var sendRepeatN =1


    //dialog 보여주는 method
    fun showDialog(calendarDay: CalendarDay, writeClickListener : View.OnClickListener, cancelListener: View.OnClickListener){
        dialog= Dialog(context)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.write_task_layout)
        val params=dialog.window?.attributes;
        params?.width=WindowManager.LayoutParams.MATCH_PARENT
        params?.height=WindowManager.LayoutParams.WRAP_CONTENT
        dialog.window?.attributes=params
        dialog.show()
        initViw(dialog)

        btnWrite.isClickable=false
        sendYear=calendarDay.year
        sendMonth=calendarDay.month
        sendDay=calendarDay.day
        tvTaskDate.text="${calendarDay.year}.${calendarDay.month}.${calendarDay.day}  [${changeWeekIntToString(getWeekOfDate("${calendarDay.year}.${calendarDay.month}.${calendarDay.day}"))}]"
        tvTaskTime.text="${sendTime}"


        //시간
        tvTaskTime.setOnClickListener(View.OnClickListener {
            val TimePickerDialog =TimePickerDialog(context,android.R.style.Theme_Holo_Dialog,
                TimePickerDialog.OnTimeSetListener { view, hourOfDay, minute ->

                    sendTime="${if(hourOfDay.toString().length==1) "0${hourOfDay}" else hourOfDay}:${if(minute.toString().length==1) "0${minute}" else minute}"
                    val format =SimpleDateFormat("mm:ss")
                    val strT=format.parse(sendTime)
                    Log.d("도원","${strT.time}")
                    sendTimemllis=strT.time
                    tvTaskTime.text=sendTime
                },3,22,true)
            TimePickerDialog.show()
        })

        //날짜
        tvTaskDate.setOnClickListener(View.OnClickListener {
           val datePickerDialog = DatePickerDialog(context, DatePickerDialog.OnDateSetListener { view, year, month, dayOfMonth ->

               sendYear=year
               sendMonth=month+1
               sendDay=dayOfMonth

               taskWeek=getWeekOfDate("${sendYear}.${sendMonth}.${sendDay}")

               tvTaskDate.text="${year}.${month+1}.${dayOfMonth}  (${changeWeekIntToString(taskWeek)})"

           },calendarDay.year,calendarDay.month-1,calendarDay.day)
            datePickerDialog.show()
        })

        //반복
        radioRepeat.setOnCheckedChangeListener(RadioGroup.OnCheckedChangeListener { group, checkedId ->
            resetWeekRepeat()
            if (checkedId==R.id.radioNone){
                sendRepeatY =0
                sendRepeatM =0
                sendRepeatW =0
                sendRepeatN =1
                repeatWeekBox.visibility=View.GONE
            }
            else if (checkedId==R.id.radioYear){
                sendRepeatY =1
                sendRepeatM =0
                sendRepeatW =0
                sendRepeatN =0
                repeatWeekBox.visibility=View.GONE
            }
            else if (checkedId==R.id.radioMonth){
                sendRepeatY =0
                sendRepeatM =1
                sendRepeatW =0
                sendRepeatN =0
                repeatWeekBox.visibility=View.GONE
            }
            else if (checkedId==R.id.radioWeek){
                sendRepeatY =0
                sendRepeatM =0
                sendRepeatW =1
                sendRepeatN =0
                repeatWeekBox.visibility=View.VISIBLE
            }
        })

        edtTaskText.doOnTextChanged { text, start, before, count ->
            Log.d("도원","${text} , ${start} , ${before}, ${count}, ${ !text.toString().equals("")!!}")
            btnWrite.isClickable = !text.toString().equals("")
        }
        btnWrite.setOnClickListener(writeClickListener)
        btnCancel.setOnClickListener(cancelListener)
    }

    fun dismissDialog(){
        dialog.dismiss()
    }

    fun resetWeekRepeat(){
        repeatSun.isChecked=false
        repeatMon.isChecked=false
        repeatTue.isChecked=false
        repeatWEN.isChecked=false
        repeatThu.isChecked=false
        repeatFri.isChecked=false
        repeatSat.isChecked=false
    }

    //작성 버튼 선택시 작성내용을 보내주는 method
    fun getInfo() :TaskItem {
        sendText=edtTaskText.text.toString()
        sendWeek="${if(repeatSun.isChecked) 1 else 0}&" +
                "${if(repeatMon.isChecked) 1 else 0}&" +
                "${if(repeatTue.isChecked) 1 else 0}&" +
                "${if(repeatWEN.isChecked) 1 else 0}&" +
                "${if(repeatThu.isChecked) 1 else 0}&" +
                "${if(repeatFri.isChecked) 1 else 0}&" +
                "${if(repeatSat.isChecked) 1 else 0}"
        return  TaskItem(
            sendYear,sendMonth,sendDay,sendWeek,sendTimemllis,sendText,
            sendNotice,sendRepeatY,sendRepeatM,sendRepeatW,sendRepeatN
        )
    }

    //날짜로 요일을 확인하는 method 1일 ~ 7토
    private fun getWeekOfDate(inPutDate :String) :Int{
        val dateFormat : SimpleDateFormat=SimpleDateFormat("yyyy.MM.dd")
        val date=dateFormat.parse(inPutDate)

        val cal =Calendar.getInstance()
        cal.time=date

        val dayWeekNum = cal.get(Calendar.DAY_OF_WEEK)
        return dayWeekNum
    }

    //숫자를 한글 요일로 변환하는 method
    private fun changeWeekIntToString(weekInt :Int): String = run {
        when(weekInt){
            1-> return "일"
            2-> return "월"
            3-> return "화"
            4-> return "수"
            5-> return "목"
            6-> return "금"
            7-> return "토"
            else -> return ""
        }
    }

    //뷰 초기화
    private fun initViw(dialog :Dialog){
        tvTaskDate=dialog.findViewById(R.id.tvTaskDate);
        tvTaskTime=dialog.findViewById(R.id.tvTaskTime);
        edtTaskText=dialog.findViewById(R.id.edtTaskText);
        tvRepeat=dialog.findViewById(R.id.tvRepeat);
        radioRepeat=dialog.findViewById(R.id.radioRepeat);
        repeatWeekBox=dialog.findViewById(R.id.repeatWeekBox);
        repeatMon=dialog.findViewById(R.id.repeatMon);
        repeatTue=dialog.findViewById(R.id.repeatTue);
        repeatWEN=dialog.findViewById(R.id.repeatWEN);
        repeatThu=dialog.findViewById(R.id.repeatThu);
        repeatFri=dialog.findViewById(R.id.repeatFri);
        repeatSat=dialog.findViewById(R.id.repeatSat);
        repeatSun=dialog.findViewById(R.id.repeatSun);
        btnCancel=dialog.findViewById(R.id.btnCancel);
        btnWrite=dialog.findViewById(R.id.btnWrite);

    }



}