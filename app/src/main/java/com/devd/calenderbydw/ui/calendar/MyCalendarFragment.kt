package com.devd.calenderbydw.ui.calendar

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.devd.calenderbydw.CreateTaskActivity
import com.devd.calenderbydw.R
import com.devd.calenderbydw.database.TaskDatabaseHelper
import com.devd.calenderbydw.databinding.FragmentMyCalendarBinding
import com.devd.calenderbydw.item.TaskItem
import com.devd.calenderbydw.utils.WidgetUtils
import com.devd.calenderbydw.utils.autoCleared
import com.google.android.gms.wearable.Asset
import com.google.android.gms.wearable.DataClient
import com.google.android.gms.wearable.PutDataMapRequest
import com.google.android.gms.wearable.Wearable
import com.prolificinteractive.materialcalendarview.CalendarDay
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.io.File
import java.lang.Exception
import java.nio.file.Files
import java.time.Instant
import java.util.concurrent.CancellationException

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
            val intent = Intent(context, CreateTaskActivity::class.java)
            intent.putExtra("type","create")
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

        resultLauncher=registerForActivityResult(
            ActivityResultContracts.StartActivityForResult(),
            ActivityResultCallback {
                //Task 등록 데이터 받는 곳
                Log.d("도원","recevie Data ? ")
                if (it.resultCode == AppCompatActivity.RESULT_OK){
                    val intent = it.data
                    if (intent?.getParcelableExtra<TaskItem>("createItem")!=null){
//                        database=dbHelper.writableDatabase
//                        dbHelper.onCreate(database)
//                        TaskDatabaseHelper.createTask(intent.getParcelableExtra<TaskItem>("createItem")!!,database,applicationContext,Wearable.getDataClient(applicationContext))
//                        calendarFragment.notifydataChange()
//                        taskListFragment.notifydataChange()
//                        WidgetUtils.updateWidgetData(applicationContext)
//                        WidgetUtils.changeDBToBytes(applicationContext)
                    }
                }
            })
    }

    private fun setSendWatchClickFunc(){
        //워치로 데이터 전송시키기
        binding.ivSendWatch.setOnClickListener {
            changeDBToBytes()
            Toast.makeText(requireContext(),"sending....", Toast.LENGTH_SHORT).show()
        }

    }
    //데이터 전송하기 전에 DB를 byteArray형태로 변경
    private fun changeDBToBytes(){
        //DB 경로를 구한 한다.
        val dbPath = TaskDatabaseHelper(requireContext(),"task.db",null,3).readableDatabase.path
        val dbFile = File(dbPath)
        val dbUri = Uri.fromFile(dbFile)
//        val realAsset = Asset.createFromUri(dbUri)
        val bytesFromDB = Files.readAllBytes(dbFile.toPath())
        val realAsset = Asset.createFromBytes(bytesFromDB)
        sendDBData(realAsset,dbPath)
    }

    //Asset 으로 만든 데이터 보내기
    private fun sendDBData(sendData :Asset,dBPtah : String){
        lifecycleScope.launch {
            try {
                val putDataReq = PutDataMapRequest.create("/taskdata").apply {
                    this.dataMap.putAsset("taskDB",sendData)
                    this.dataMap.putString("taskDBPath",dBPtah)
                    this.dataMap.putLong("KEY", Instant.now().epochSecond)

                }
                    .asPutDataRequest()
                    .setUrgent()

                val result =dataClient.putDataItem(putDataReq).await()
                Log.d("도원", "moblie saved: ${putDataReq.uri}")
                Log.d("도원", "DataItem saved: $result")
            } catch (cancellationException: CancellationException) {
                Log.d("도원", "Saving DataItem failed: ${cancellationException.localizedMessage}")
                throw cancellationException
            } catch (exception: Exception) {
                Log.d("도원", "Saving DataItem failed: $exception")
            }
        }

    }
}