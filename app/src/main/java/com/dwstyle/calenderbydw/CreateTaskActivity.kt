package com.dwstyle.calenderbydw

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.core.widget.doOnTextChanged
import com.dwstyle.calenderbydw.item.TaskItem
import com.prolificinteractive.materialcalendarview.CalendarDay
import java.text.SimpleDateFormat
import java.util.*

class CreateTaskActivity : AppCompatActivity() {

    //날짜
    lateinit var tvTaskDate : TextView
    lateinit var ivChangeDate : ImageView

    //시간
    lateinit var tvTaskTime : TextView
    lateinit var ivChangeTime : ImageView

    //일정 입력
    lateinit var edtTaskTitle : EditText
    lateinit var edtTaskContents : EditText

    //반복
    lateinit var radioRepeat:RadioGroup

    private lateinit var repeatWeekBox : LinearLayout
    private lateinit var repeatMon : CheckBox
    private lateinit var repeatTue: CheckBox
    private lateinit var repeatWEN: CheckBox
    private lateinit var repeatThu: CheckBox
    private lateinit var repeatFri: CheckBox
    private lateinit var repeatSat: CheckBox
    private lateinit var repeatSun: CheckBox

    private lateinit var btnCancel: Button
    private lateinit var btnWrite : Button

    private lateinit var tvEditCount :TextView

    var taskWeek=0;
    var type :String?=null

    private var _id = 0
    // DB로 보낼 정보 변수들
    private var sendYear =2021
    private var sendMonth =12
    private var sendDay =15
    private var sendWeek = "0"
    private var sendTime = "03:22"
    private var sendTimemllis :Long= 0
    private var sendTitle ="Title"
    private var sendText = "Task Example"
    private var sendNotice =0
    private var sendRepeatY =0
    private var sendRepeatM =0
    private var sendRepeatW =0
    private var sendRepeatN =1
    private var sendPriority=0


    private val format =SimpleDateFormat("HH:mm")
    private var strT=format.parse(sendTime)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_task)
        initView()
        clickFuncForCreate()
        type = intent.getStringExtra("type")
        if (type==null){
            Toast.makeText(applicationContext,"에러가 발생했습니다. 다시 시도해 주시기 바랍니다.",Toast.LENGTH_LONG).show()
            finish()
        }
        if (type.equals("create")){
            btnWrite.text = "작성"
            intent.getParcelableExtra<CalendarDay>("dateInfo")?.let {
                initValueForCreate(it)
            }
        }else if (type.equals("change")){
            btnWrite.text = "수정"
            intent.getParcelableExtra<TaskItem>("taskForChange")?.let {
                _id=it._id
                initValueForChange(it)
            }
        }



    }



    //뷰 초기화
    private fun initView(){
        tvTaskDate=findViewById(R.id.tvTaskDate)
        ivChangeDate=findViewById(R.id.ivChangeDate)

        tvTaskTime=findViewById(R.id.tvTaskTime)
        ivChangeTime=findViewById(R.id.ivChangeTime)

        edtTaskTitle=findViewById(R.id.edtTaskTitle)
        edtTaskContents=findViewById(R.id.edtTaskContents)


        radioRepeat=findViewById(R.id.radioRepeat)
        repeatWeekBox=findViewById(R.id.repeatWeekBox)
        repeatMon=findViewById(R.id.repeatMon)
        repeatTue=findViewById(R.id.repeatTue)
        repeatWEN=findViewById(R.id.repeatWEN)
        repeatThu=findViewById(R.id.repeatThu)
        repeatFri=findViewById(R.id.repeatFri)
        repeatSat=findViewById(R.id.repeatSat)
        repeatSun=findViewById(R.id.repeatSun)
        resetWeekRepeat()

        tvEditCount=findViewById(R.id.tvEditCount);
        tvEditCount.text="0/300"

        btnCancel=findViewById(R.id.btnCancel)

        btnWrite=findViewById(R.id.btnDelete)

    }

    //값들 초기화(생성용)
    private fun initValueForCreate(calendarDay: CalendarDay?){
        sendYear = calendarDay?.year ?: 2021
        sendMonth = calendarDay?.month ?: 3
        sendDay = calendarDay?.day ?: 22
        sendTime = "03:22"
        sendTitle="Title"
        sendText = "No Text"
        sendNotice =0
        sendRepeatY =0
        sendRepeatM =0
        sendRepeatW =0
        sendRepeatN =1
        sendPriority=0
        sendTimemllis=strT.time

        tvTaskDate.text="${sendYear}.${sendMonth}.${sendDay}  [${changeWeekIntToString(getWeekOfDate("${sendYear}.${sendMonth}.$sendDay}"))}]"
        tvTaskTime.text="${sendTime}"
    }

    private fun initValueForChange(item :TaskItem){
        sendYear =item.year
        sendMonth = item.month
        sendDay = item.day

        sendTitle=item.title
        sendText = item.text
        sendNotice =item.notice
        sendRepeatY =item.repeatY
        sendRepeatM =item.repeatM
        sendRepeatW =item.repeatW
        sendRepeatN =item.repeatN
        sendPriority=item.priority
        sendTimemllis=item.time
        sendTime = SimpleDateFormat("HH:mm").format(sendTimemllis)
        sendWeek= item.week

        changeViewForChange()

    }

    private fun changeViewForChange(){
        //타이틀
        edtTaskTitle.setText(sendTitle.toString())
        edtTaskContents.setText(sendText)
        tvTaskDate.text = "${sendYear}.${sendMonth}.${sendDay}"
        tvTaskTime.text = "${sendTime}"

        //반복
        if (sendRepeatY==1){
            radioRepeat.check(R.id.radioYear)
        }else if (sendRepeatM==1){
            radioRepeat.check(R.id.radioMonth)
        }else if (sendRepeatW==1){
            radioRepeat.check(R.id.radioWeek)
            val tempWeekStr = sendWeek.split("&")
            for (a in 0..tempWeekStr.size){
                when(a){
                    0 -> repeatSun.isChecked= tempWeekStr[a]=="1"
                    1 -> repeatMon.isChecked= tempWeekStr[a]=="1"
                    2 -> repeatTue.isChecked= tempWeekStr[a]=="1"
                    3 -> repeatWEN.isChecked= tempWeekStr[a]=="1"
                    4 -> repeatThu.isChecked= tempWeekStr[a]=="1"
                    5 -> repeatFri.isChecked= tempWeekStr[a]=="1"
                    6 -> repeatSat.isChecked= tempWeekStr[a]=="1"
                }
            }
        }else{
            radioRepeat.check(R.id.radioNone)
        }



    }

    //클릭 리스너
    private fun clickFuncForCreate(){
        //날짜 선택
        tvTaskDate.setOnClickListener { ivChangeDate.performClick() }
        ivChangeDate.setOnClickListener {
            val datePickerDialog = DatePickerDialog(this, DatePickerDialog.OnDateSetListener { view, year, month, dayOfMonth ->

                sendYear=year
                sendMonth=month+1
                sendDay=dayOfMonth

                taskWeek=getWeekOfDate("${sendYear}.${sendMonth}.${sendDay}")

                tvTaskDate.text="${year}.${month+1}.${dayOfMonth}  (${changeWeekIntToString(taskWeek)})"

            },sendYear, sendMonth-1,sendDay)
            datePickerDialog.show()
        }

        //시간 선택
        tvTaskTime.setOnClickListener { ivChangeTime.performClick() }
        ivChangeTime.setOnClickListener {
            val TimePickerDialog =TimePickerDialog(this,android.R.style.Theme_Holo_Dialog,
                TimePickerDialog.OnTimeSetListener { view, hourOfDay, minute ->

                    sendTime="${if(hourOfDay.toString().length==1) "0${hourOfDay}" else hourOfDay}:${if(minute.toString().length==1) "0${minute}" else minute}"
                    strT=format.parse(sendTime)
                    sendTimemllis=strT.time
                    tvTaskTime.text=sendTime
                },3,22,true)
            TimePickerDialog.show()
        }

        radioRepeat.setOnCheckedChangeListener(RadioGroup.OnCheckedChangeListener { group, checkedId ->
            resetWeekRepeat()
            if (checkedId==R.id.radioNone){
                sendRepeatY =0
                sendRepeatM =0
                sendRepeatW =0
                sendRepeatN =1
                sendPriority=4
                repeatWeekBox.visibility= View.GONE
            }
            else if (checkedId==R.id.radioYear){
                sendRepeatY =1
                sendRepeatM =0
                sendRepeatW =0
                sendRepeatN =0
                sendPriority=1
                repeatWeekBox.visibility= View.GONE
            }
            else if (checkedId==R.id.radioMonth){
                sendRepeatY =0
                sendRepeatM =1
                sendRepeatW =0
                sendRepeatN =0
                sendPriority=2
                repeatWeekBox.visibility= View.GONE
            }
            else if (checkedId==R.id.radioWeek){
                sendRepeatY =0
                sendRepeatM =0
                sendRepeatW =1
                sendRepeatN =0
                sendPriority=3
                repeatWeekBox.visibility= View.VISIBLE
            }
        })

        edtTaskTitle.doOnTextChanged { text, start, before, count ->
            btnWrite.isClickable = !text.toString().equals("")
        }

        edtTaskContents.doOnTextChanged { text, start, before, count ->
            tvEditCount.text="${text.toString().length}/300"
            if (text.toString().length ==0){
                tvEditCount.setTextColor(getColor(R.color.outOfRangeColor))
            }else if (text.toString().length >300){
                tvEditCount.setTextColor(getColor(R.color.yearTopColor))
            }else if(text.toString().length < 300){
                tvEditCount.setTextColor(getColor(R.color.dayTopColor))
            }
        }

        btnCancel.setOnClickListener {
            finish()
        }

        btnWrite.setOnClickListener {
            if (edtTaskTitle.text.toString().equals("")){
                Toast.makeText(applicationContext,"일정의 제목이 없으면 저장할 수 없습니다.",Toast.LENGTH_SHORT).show()
            }else if (edtTaskContents.text.toString().length>300){
                Toast.makeText(this,"일정의 내용은 300자를 넘을 수 없습니다.",Toast.LENGTH_SHORT).show()
            }else{
                sendTitle=edtTaskTitle.text.toString()

                sendText=if (edtTaskContents.text.toString().equals("")) "내용 없음" else edtTaskContents.text.toString()

                sendWeek="${if(repeatSun.isChecked) 1 else 0}&" +
                        "${if(repeatMon.isChecked) 1 else 0}&" +
                        "${if(repeatTue.isChecked) 1 else 0}&" +
                        "${if(repeatWEN.isChecked) 1 else 0}&" +
                        "${if(repeatThu.isChecked) 1 else 0}&" +
                        "${if(repeatFri.isChecked) 1 else 0}&" +
                        "${if(repeatSat.isChecked) 1 else 0}"
                val intent =Intent()
                if (type.equals("create")){
                    intent.putExtra("createItem",
                        TaskItem(
                            0,sendYear,sendMonth,sendDay,sendWeek,sendTimemllis,sendTitle,sendText,
                            sendNotice,sendRepeatY,sendRepeatM,sendRepeatW,sendRepeatN,sendPriority,0,""))

                }else if (type.equals("change")){
                    intent.putExtra("changeItem",
                        TaskItem(
                            _id,sendYear,sendMonth,sendDay,sendWeek,sendTimemllis,sendTitle,sendText,
                            sendNotice,sendRepeatY,sendRepeatM,sendRepeatW,sendRepeatN,sendPriority,0,""))
                }
                setResult(RESULT_OK,intent)
                finish()

            }
        }
    }

    //날짜로 요일을 확인하는 method 1일 ~ 7토
    private fun getWeekOfDate(inPutDate :String) :Int {
        val dateFormat: SimpleDateFormat = SimpleDateFormat("yyyy.MM.dd")
        val date = dateFormat.parse(inPutDate)

        val cal = Calendar.getInstance()
        cal.time = date

        val dayWeekNum = cal.get(Calendar.DAY_OF_WEEK)
        return dayWeekNum
    }

    //숫자를 한글 요일로 변환하는 method
    private fun changeWeekIntToString(weekInt :Int): String = run {
        return when(weekInt){
            1-> "일"
            2-> "월"
            3-> "화"
            4-> "수"
            5-> "목"
            6-> "금"
            7-> "토"
            else -> ""
        }
    }

    //라디오 버튼 초기화
    private fun resetWeekRepeat(){
        repeatSun.isChecked=false
        repeatMon.isChecked=false
        repeatTue.isChecked=false
        repeatWEN.isChecked=false
        repeatThu.isChecked=false
        repeatFri.isChecked=false
        repeatSat.isChecked=false
    }

}