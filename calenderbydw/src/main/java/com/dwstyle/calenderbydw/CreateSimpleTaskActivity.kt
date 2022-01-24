package com.dwstyle.calenderbydw

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import com.dwstyle.calenderbydw.utils.MyDatePicker

class CreateSimpleTaskActivity : Activity() {

    private lateinit var tvDate :TextView
    private lateinit var btnDatePicker:Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_simple_task)

        initView()
        clickViewFunc()
    }

    private fun initView(){
        tvDate=findViewById(R.id.tvDate)
        btnDatePicker=findViewById(R.id.btnDatePicker)

    }

    private fun clickViewFunc(){
        btnDatePicker.setOnClickListener {
            val intent = Intent(this,MyDatePicker::class.java);
            startActivity(intent)
//            DatePickerDialog(this,AlertDialog.THEME_DEVICE_DEFAULT_LIGHT,DatePickerDialog.OnDateSetListener { datePicker, y, m, d ->
//
//            },2022,3,22).show()
        }

    }
}