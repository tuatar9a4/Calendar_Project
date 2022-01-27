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

class CreateSimpleTaskActivity : Activity() {

    val binding by lazy { ActivityCreateSimpleTaskBinding.inflate(layoutInflater) }

    private lateinit var tvDate :TextView
    private lateinit var btnDatePicker:Button


    private var taskYear ="2022"
    private var taskMonth ="03"
    private var taskDay ="22"

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
        val currentDate =DateTime(System.currentTimeMillis()).toLocalDate()

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode== RESULT_OK){
            if (requestCode==Consts.TASKCREATECODE){
                Log.d("도원","${data?.getStringExtra(Consts.TASKCREATEDAET)}")
            }else if(requestCode==Consts.TASKTIMECODE){
                Log.d("도원","${data?.getStringExtra(Consts.TASKCREATETIME)}")
            }
        }

    }
}