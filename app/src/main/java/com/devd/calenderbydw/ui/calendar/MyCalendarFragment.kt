package com.devd.calenderbydw.ui.calendar

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.fragment.app.Fragment
import com.devd.calenderbydw.databinding.FragmentMyCalendarBinding
import com.devd.calenderbydw.utils.autoCleared
import com.google.android.gms.wearable.DataClient
import com.google.android.gms.wearable.Wearable

class MyCalendarFragment : Fragment() {

    /*
      private val calendarFragment=CalendarFragment.newInstance()
    private val taskListFragment=TaskListFragment.newInstance()
     */
    private var binding by autoCleared<FragmentMyCalendarBinding>()
    private lateinit var dataClient :DataClient

    private lateinit var resultLauncher : ActivityResultLauncher<Intent>
//    private lateinit var dbHelper : TaskDatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        dbHelper=TaskDatabaseHelper(context,"task.db",null,3)
    }

    override fun onResume() {
        super.onResume()
//        intent.getStringExtra("sendDate")?.let {
//            val str = it.split(".")
//            val transaction =supportFragmentManager.beginTransaction()
//            transaction.hide(calendarFragment)
//            transaction.show(taskListFragment)
//            mainTopToolbar.visibility=View.GONE
//            taskListFragment.setCalendarDay(CalendarDay.from(str[0].toInt(),str[1].toInt(),str[2].toInt()))
//            transaction.commit()
//        }
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMyCalendarBinding.inflate(inflater,container,false)
        dataClient = Wearable.getDataClient(requireActivity())
        return binding.root
    }

    private fun setAddFunc(){
        //일정 생성으로 이동
        binding.btnPlus.setOnClickListener {
//            var sendCalendar : CalendarDay? =null
//            if (calendarFragment.isVisible)
//                sendCalendar=calendarFragment.getSelectDateInfo()
//            if (taskListFragment.isVisible && taskListFragment.getSelectDateInfo()!=null)
//                sendCalendar=taskListFragment.getSelectDateInfo()
//
//            sendCalendar?.let {
//                intent.putExtra("dateInfo",it)
//                resultLauncher.launch(intent)
//            }
        }
    }

    private fun setHomeClickFunc(){
        //달력화면 이동
        binding.btnHome.setOnClickListener {
//            val transaction =supportFragmentManager.beginTransaction()
//            transaction.hide(taskListFragment)
//            transaction.show(calendarFragment)
//            mainTopToolbar.visibility= View.VISIBLE
//            transaction.commit()
//            calendarFragment.refreshTaskList(taskListFragment.getSelectDateInfo())
        }
    }

    private fun setTaskListClickFunc(){
        //TaskList 화면 이동
        binding.btnList.setOnClickListener {
//            val transaction =supportFragmentManager.beginTransaction()
//            transaction.hide(calendarFragment)
//            transaction.show(taskListFragment)
//            mainTopToolbar.visibility=View.GONE
//            taskListFragment.setCalendarDay(calendarFragment.getSelectDateInfo())
//            transaction.commit()
        }

    }

    private fun setSendWatchClickFunc(){
        //워치로 데이터 전송시키기
        binding.ivSendWatch.setOnClickListener {
        }

    }
}