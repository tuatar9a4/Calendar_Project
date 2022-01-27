package com.dwstyle.calenderbydw

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import com.dwstyle.calenderbydw.databinding.ActivityCreateSimpleTaskBinding
import com.dwstyle.calenderbydw.utils.Consts
import com.dwstyle.calenderbydw.utils.MyDatePicker
import com.dwstyle.calenderbydw.utils.MyTimePicker
import org.joda.time.DateTime
import org.joda.time.LocalDateTime
import org.joda.time.LocalTime

class CreateSimpleTaskActivity : Activity() {

    val binding by lazy { ActivityCreateSimpleTaskBinding.inflate(layoutInflater) }

    private lateinit var tvDate :TextView
    private lateinit var btnDatePicker:Button


    private var taskYear ="2022"
    private var taskMonth ="03"
    private var taskDay ="22"
    private var taskMillis =0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        initView()
        clickViewFunc()
        initValue()
    }

    private fun initView(){
        tvDate=findViewById(R.id.tvDate)
        btnDatePicker=findViewById(R.id.btnDatePicker)

    }

    private fun clickViewFunc(){
        btnDatePicker.setOnClickListener {
            val intent = Intent(this,MyDatePicker::class.java);
            startActivityForResult(intent, Consts.TASKCREATECODE)
//            DatePickerDialog(this,AlertDialog.THEME_DEVICE_DEFAULT_LIGHT,DatePickerDialog.OnDateSetListener { datePicker, y, m, d ->
//
//            },2022,3,22).show()
        }
        binding.btnTimePicker.setOnClickListener {
            val intent = Intent(this,MyTimePicker::class.java);
            startActivityForResult(intent, Consts.TASKTIMECODE)
        }

    }

    private fun initValue(){
        val currentDate =DateTime(System.currentTimeMillis()).toLocalDateTime()
        taskMillis =System.currentTimeMillis()
        taskYear =currentDate.year.toString()
        taskMonth =if (currentDate.monthOfYear.toString().length==1) "0${currentDate.monthOfYear.toString()}" else "${currentDate.monthOfYear}"
        taskDay =if (currentDate.dayOfMonth.toString().length==1) "0${currentDate.dayOfMonth.toString()}" else "${currentDate.dayOfMonth}"
        binding.tvDate.text="Date : ${taskYear}.${taskMonth}.${taskDay}"
        binding.tvTime.text="Time : ${currentDate.toString("aa HH:mm")}"
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode== RESULT_OK){
            var temp : List<String>? = listOf<String>()
            if (requestCode==Consts.TASKCREATECODE){
                Log.d("도원","${data?.getStringExtra(Consts.TASKCREATEDAET)}")
                temp=data?.getStringExtra(Consts.TASKCREATEDAET)?.split(".")
                temp?.let {
                    taskYear = it[0]
                    taskMonth = it[1]
                    taskDay = it[2]
                }
                binding.tvDate.text="Date : ${taskYear}.${taskMonth}.${taskDay}"

            }else if(requestCode==Consts.TASKTIMECODE){
                Log.d("도원","${data?.getStringExtra(Consts.TASKCREATETIME)}")
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
                    binding.tvTime.text="Time : ${tempHour}:${tempMin}"
                }

                Log.d("도원","${LocalDateTime(taskYear.toInt(),taskMonth.toInt(),taskDay.toInt(),tempHour,tempMin,0).toDateTime().millis}")
                Log.d("도원","${System.currentTimeMillis()} ㄹㄹㄹ ${LocalTime(tempHour,tempMin,0).toDateTimeToday().millis}")
                Log.d("도원","ㄹㄹㄹ ${DateTime(LocalDateTime(taskYear.toInt(),taskMonth.toInt(),taskDay.toInt(),tempHour,tempMin,0).toDateTime().millis).toString("yyyy.MM.dd")}")

            }
        }

    }
}