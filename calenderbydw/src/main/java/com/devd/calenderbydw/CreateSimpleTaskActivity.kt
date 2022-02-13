package com.devd.calenderbydw

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.CompoundButton
import android.widget.Toast
import com.devd.calenderbydw.databinding.ActivityCreateSimpleTaskBinding
import com.devd.calenderbydw.item.CreateTaskData
import com.devd.calenderbydw.utils.Consts
import com.devd.calenderbydw.utils.MyDatePicker
import com.devd.calenderbydw.utils.MyTimePicker
import org.joda.time.DateTime

class CreateSimpleTaskActivity : Activity() {

    val binding by lazy { ActivityCreateSimpleTaskBinding.inflate(layoutInflater) }

    private lateinit var taskYear :String
    private lateinit var taskMonth :String
    private lateinit var taskDay :String
    private lateinit var taskWeek :String
    private var taskMillis =0L
    private lateinit var taskTitle :String
    private var taskNotice = 0
    private var taskRepeatY = 0
    private var taskRepeatM = 0
    private var taskRepeatW = 0
    private var taskRepeatN = 1
    private var taskPriority = 1
    private var taskHoliday = 0
    private lateinit var taskExpectDay :String

    private var completeTextTask =false
    private var completeWeekTask =false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        initValue()
        clickViewFunc()
    }

    private fun clickViewFunc(){
        binding.tvDate.setOnClickListener {
            binding.btnDatePicker.performClick()
        }
        binding.btnDatePicker.setOnClickListener {
            val intent = Intent(this,MyDatePicker::class.java);
            startActivityForResult(intent, Consts.TASKCREATECODE)
//            DatePickerDialog(this,AlertDialog.THEME_DEVICE_DEFAULT_LIGHT,DatePickerDialog.OnDateSetListener { datePicker, y, m, d ->
//
//            },2022,3,22).show()
        }
        binding.tvTime.setOnClickListener {
            binding.btnTimePicker.performClick()
        }
        binding.btnTimePicker.setOnClickListener {
            val intent = Intent(this,MyTimePicker::class.java);
            startActivityForResult(intent, Consts.TASKTIMECODE)
        }

        binding.repeatRadio.setOnCheckedChangeListener { radioGroup, id ->
            when(id) {
                R.id.rdoWeek -> binding.weekRepeatBox.visibility= View.VISIBLE
                else -> binding.weekRepeatBox.visibility= View.GONE
            }
        }

        binding.edtTitleText.setOnEditorActionListener { view, actionId, keyEvent ->

            return@setOnEditorActionListener when(actionId){
                EditorInfo.IME_ACTION_SEND ->{
                    val imm =getSystemService(INPUT_METHOD_SERVICE) as (InputMethodManager)
                    imm.hideSoftInputFromWindow(view.windowToken,0)
                    true;
                }
                else -> false
            }
        }

        val checkedListener :  CompoundButton.OnCheckedChangeListener = CompoundButton.OnCheckedChangeListener{compoundButton, checked ->
            if (checked){
                compoundButton.setTextColor(Color.parseColor("#0000ff"))
            }else{
                compoundButton.setTextColor(Color.parseColor("#ffffff"))
            }
        }

        binding.checkMon.setOnCheckedChangeListener(checkedListener)
        binding.checkTue.setOnCheckedChangeListener(checkedListener)
        binding.checkWen.setOnCheckedChangeListener(checkedListener)
        binding.checkThu.setOnCheckedChangeListener(checkedListener)
        binding.checkFri.setOnCheckedChangeListener(checkedListener)
        binding.checkSat.setOnCheckedChangeListener(checkedListener)
        binding.checkSun.setOnCheckedChangeListener(checkedListener)

        binding.btnTaskCreate.setOnClickListener {
            settingSendData()
            if (completeTextTask && completeWeekTask){
                val intent =Intent()
                val temp =CreateTaskData(
                    0,
                    taskYear.toInt(),
                    taskMonth.toInt(),
                    taskDay.toInt(),
                    taskWeek,
                    taskMillis,
                    taskTitle,
                    "",
                    taskNotice,
                    taskRepeatY,
                    taskRepeatM,
                    taskRepeatW,
                    taskRepeatN,
                    taskPriority,
                    taskHoliday,
                    taskExpectDay
                )
                intent.putExtra(Consts.REVICETASKINFO,temp)
                setResult(RESULT_OK,intent)
                finish()
                completeTextTask=false
                completeWeekTask=false
            }
        }

        binding.btnCancel.setOnClickListener {
            Log.d("도원", "btnCancee} ")

            finish()
        }

    }

    private fun initValue(){
        val currentDate =DateTime(System.currentTimeMillis()).toLocalDateTime()
        taskMillis =System.currentTimeMillis()
        taskYear =currentDate.year.toString()
        taskMonth =if (currentDate.monthOfYear.toString().length==1) "0${currentDate.monthOfYear.toString()}" else "${currentDate.monthOfYear}"
        taskDay =if (currentDate.dayOfMonth.toString().length==1) "0${currentDate.dayOfMonth.toString()}" else "${currentDate.dayOfMonth}"
        binding.tvDate.text = "Date : ${taskYear}.${taskMonth}.${taskDay}"
        binding.tvTime.text="Time : ${currentDate.toString("aa HH:mm")}"

        taskWeek="0&0&0&0&0&0&0"
        taskTitle=""
        taskNotice=0
        binding.repeatRadio.check(R.id.rdoDay)
        taskRepeatY=0
        taskRepeatM=0
        taskRepeatW=0
        taskRepeatN=1
        taskPriority=1
        taskHoliday=0
        taskExpectDay=""
    }


    private fun settingSendData(){
        if (binding.edtTitleText.text.isEmpty() || binding.edtTitleText.text.toString() == ""){
            Toast.makeText(applicationContext,"일정을 입력하셔야합니다.",Toast.LENGTH_SHORT).show()
            return
        }
        taskTitle=binding.edtTitleText.text.toString()
        when(binding.repeatRadio.checkedRadioButtonId){
            R.id.rdoDay ->{
                taskRepeatY=0
                taskRepeatM=0
                taskRepeatW=0
                taskRepeatN=1
                completeWeekTask=true
            }
            R.id.rdoYear -> {
                taskRepeatY=1
                taskRepeatM=0
                taskRepeatW=0
                taskRepeatN=0
                completeWeekTask=true
            }
            R.id.rdoMonth -> {
                taskRepeatY=0
                taskRepeatM=1
                taskRepeatW=0
                taskRepeatN=0
                completeWeekTask=true
            }
            R.id.rdoWeek -> {
                taskRepeatY=0
                taskRepeatM=0
                taskRepeatW=1
                taskRepeatN=0
                getWeekStr()

            }
        }
        completeTextTask=true
    }

    fun getWeekStr(){
        if (!binding.checkSun.isChecked && !binding.checkMon.isChecked && !binding.checkTue.isChecked
            && !binding.checkWen.isChecked && !binding.checkThu.isChecked && !binding.checkFri.isChecked
            && !binding.checkSat.isChecked){
                Toast.makeText(applicationContext,"한개 이상의 요일을 선택해주세요",Toast.LENGTH_SHORT).show()
            completeWeekTask=false
            return
        }

        taskWeek="${if (binding.checkSun.isChecked) 1 else 0}&"+
                "${if (binding.checkMon.isChecked) 1 else 0}&"+
                "${if (binding.checkTue.isChecked) 1 else 0}&"+
                "${if (binding.checkWen.isChecked) 1 else 0}&"+
                "${if (binding.checkThu.isChecked) 1 else 0}&"+
                "${if (binding.checkFri.isChecked) 1 else 0}&"+
                "${if (binding.checkSat.isChecked) 1 else 0}&"
        completeWeekTask=true

    }

    //날짜 시간값은 여기서서 조정한다.
   override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode== RESULT_OK){
            var temp : List<String>? = listOf<String>()
            if (requestCode==Consts.TASKCREATECODE){
                temp=data?.getStringExtra(Consts.TASKCREATEDAET)?.split(".")
                temp?.let {
                    taskYear = it[0]
                    taskMonth = it[1]
                    taskDay = it[2]
                }
                binding.tvDate.text="Date : ${taskYear}.${taskMonth}.${taskDay}"

            }else if(requestCode==Consts.TASKTIMECODE){
                temp=data?.getStringExtra(Consts.TASKCREATETIME)?.split(".")
                var tempHour =3
                var tempMin =22
                temp?.let {
                    if (it[0] == "PM"){
                        tempHour = if (it[1].toInt() == 12) it[1].toInt() else it[1].toInt()+12
                    }else{
                        tempHour = it[1].toInt()
                    }
                    tempMin=it[2].toInt()
                    taskMillis=DateTime(taskYear.toInt(),taskMonth.toInt(),taskDay.toInt(),tempHour,tempMin).millis
                    binding.tvTime.text="Time : ${tempHour}:${tempMin}"
                }
            }
        }

    }
}