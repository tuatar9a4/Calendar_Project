package com.dwstyle.calenderbydw

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import com.dwstyle.calenderbydw.fragments.CalendarFragment
import com.dwstyle.calenderbydw.utils.MakeTaskDialog

class MainActivity : AppCompatActivity() {
    private val calendarFragment=CalendarFragment.newInstance()
    private lateinit var plus : Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initView()
        val makeTaskDialog=MakeTaskDialog(this)
        supportFragmentManager.beginTransaction()
            .replace(R.id.flmainFragment,calendarFragment)
            .commit()
        plus.setOnClickListener {
//            makeTaskDialog.show()
            makeTaskDialog.showDialog(calendarFragment.getSelectDateInfo(), {

                calendarFragment.createTask(makeTaskDialog.getInfo())
                makeTaskDialog.dismissDialog()
            }, {

                makeTaskDialog.dismissDialog()
            })

//            calendarFragment.createTask()
        }
    }

    fun initView(){
        plus=findViewById(R.id.plus)
    }



}