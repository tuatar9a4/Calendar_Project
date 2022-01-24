package com.dwstyle.calenderbydw

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Intent
import android.database.sqlite.SQLiteDatabase
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import com.dwstyle.calenderbydw.database.TaskDatabaseHelper
import com.dwstyle.calenderbydw.fragments.CalendarFragment
import com.dwstyle.calenderbydw.fragments.TaskListFragment
import com.dwstyle.calenderbydw.item.HolidayItem
import com.dwstyle.calenderbydw.item.TaskItem
import com.dwstyle.calenderbydw.retrofit.HolidayRetrofit
import com.dwstyle.calenderbydw.utils.WidgetUtils
import com.google.android.gms.tasks.Task
import com.google.android.gms.wearable.*
import com.prolificinteractive.materialcalendarview.CalendarDay
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.lang.Exception
import java.nio.file.Files
import java.time.Instant
import java.util.*
import java.util.concurrent.CancellationException
import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity() {
    private val calendarFragment=CalendarFragment.newInstance()
    private val taskListFragment=TaskListFragment.newInstance()
    private lateinit var btnPlus : ImageView
    private lateinit var btnList :ImageView
    private lateinit var btnHome :ImageView
    private lateinit var ivSendWatch :ImageView
    private lateinit var resultLauncher : ActivityResultLauncher<Intent>

    private var backKeyPressedTime:Long=0;

    private lateinit var dbHelper : TaskDatabaseHelper
    private lateinit var database : SQLiteDatabase

    private lateinit var mainTopToolbar : Toolbar

    private val testRetrofit : HolidayRetrofit =HolidayRetrofit()
    private val dataClient by lazy { Wearable.getDataClient(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initView()
        dbHelper=TaskDatabaseHelper(applicationContext,"task.db",null,3)
        supportFragmentManager.beginTransaction()
            .add(R.id.flmainFragment,calendarFragment)
            .add(R.id.flmainFragment,taskListFragment)
            .hide(taskListFragment)
            .commit()
        binddata()
        clickFunction()

//        CoroutineScope(Dispatchers.Main).launch {
////            testRetrofit.getHoliday(applicationContext,"2022")
//            Log.d("도원","myTaskTbl : check |${TaskDatabaseHelper.isExistsTable(dbHelper.readableDatabase,"myTaskTbl")}")
//            Log.d("도원","myTaskTbwwl :  check |${TaskDatabaseHelper.isExistsTable(dbHelper.readableDatabase,"myTaskTbwwl")}")
//            Log.d("도원","holiday2021Tbl : check |${TaskDatabaseHelper.isExistsTable(dbHelper.readableDatabase,"holiday2021Tbl")}")
//        }

    }

    private fun binddata(){
//        testRetrofit.getholidayItms().observe(this,object  : Observer<ArrayList<HolidayItem>>{
//            override fun onChanged(t: ArrayList<HolidayItem>?) {
//                Log.d("도원","onChange ${t}")
//            }
//        })

    }

    //clickFun
    private fun clickFunction(){
        //달력화면 이동
        btnHome.setOnClickListener {
//            supportFragmentManager.beginTransaction().replace(R.id.flmainFragment,calendarFragment).commit()
            val transaction =supportFragmentManager.beginTransaction()
            transaction.hide(taskListFragment)
            transaction.show(calendarFragment)
            mainTopToolbar.visibility= View.VISIBLE
            transaction.commit()
            calendarFragment.refreshTaskList(taskListFragment.getSelectDateInfo())
        }

        //TaskList 화면 이동
        btnList.setOnClickListener {
//            supportFragmentManager.beginTransaction().replace(R.id.flmainFragment,taskListFragment).commit()
            val transaction =supportFragmentManager.beginTransaction()
            transaction.hide(calendarFragment)
            transaction.show(taskListFragment)
            mainTopToolbar.visibility=View.GONE
            taskListFragment.setCalendarDay(calendarFragment.getSelectDateInfo())
            transaction.commit()
        }

        //일정 생성으로 이동
        btnPlus.setOnClickListener {
            val intent = Intent(applicationContext,CreateTaskActivity::class.java)
            intent.putExtra("type","create")
            Log.d("도원","taskListFragment.isVisible : ${taskListFragment.isVisible}")
            Log.d("도원","calendarFragment.isVisible : ${calendarFragment.isVisible}")
            var sendCalendar :CalendarDay? =null
            if (calendarFragment.isVisible)
                sendCalendar=calendarFragment.getSelectDateInfo()
            if (taskListFragment.isVisible && taskListFragment.getSelectDateInfo()!=null)
                sendCalendar=taskListFragment.getSelectDateInfo()

            sendCalendar?.let {
                intent.putExtra("dateInfo",it)
                resultLauncher.launch(intent)
            }
        }

        resultLauncher=registerForActivityResult(ActivityResultContracts.StartActivityForResult(),
            ActivityResultCallback {
                //Task 등록 데이터 받는 곳
                Log.d("도원","recevie Data ? ")
                if (it.resultCode == RESULT_OK){
                    val intent = it.data
                    if (intent?.getParcelableExtra<TaskItem>("createItem")!=null){
                        database=dbHelper.writableDatabase
                        dbHelper.onCreate(database)
                        TaskDatabaseHelper.createTask(intent.getParcelableExtra<TaskItem>("createItem")!!,database)
                        calendarFragment.notifydataChange()
                        taskListFragment.notifydataChange()
                        WidgetUtils.updateWidgetData(applicationContext)
                    }
                }
            })


        //워치로 데이터 전송시키기
        ivSendWatch.setOnClickListener {
            changeDBToBytes()
            Toast.makeText(applicationContext,"sending....",Toast.LENGTH_SHORT).show()
        }



    }

    override fun onResume() {
        super.onResume()
        intent.getStringExtra("sendDate")?.let {
            val str = it.split(".")
            val transaction =supportFragmentManager.beginTransaction()
            transaction.hide(calendarFragment)
            transaction.show(taskListFragment)
            mainTopToolbar.visibility=View.GONE
            taskListFragment.setCalendarDay(CalendarDay.from(str[0].toInt(),str[1].toInt(),str[2].toInt()))
            transaction.commit()
        }
    }


    //데이터 전송하기 전에 DB를 byteArray형태로 변경
    private fun changeDBToBytes(){
        //DB 경로를 구한 한다.
        val dbPath = TaskDatabaseHelper(applicationContext,"task.db",null,3).readableDatabase.path
        val dbFile = File(dbPath)
        val dbUri = Uri.fromFile(dbFile)
//        val realAsset = Asset.createFromUri(dbUri)
        val bytesFromDB = Files.readAllBytes(dbFile.toPath())
        val realAsset = Asset.createFromBytes(bytesFromDB)
        sendDBData(realAsset,dbPath)
    }


    //Asset 으로 만든 데이터 보내기
    private fun sendDBData(sendData :Asset,dBPtah : String){
//        val dataMap : PutDataMapRequest = PutDataMapRequest.create("/taskdata")
//        dataMap.dataMap.putAsset("taskDB",sendData)
//        dataMap.dataMap.putString("taskDBPath",dBPtah)
//        Log.d("도원","dBPtah :  ${dBPtah}")
//        val request : PutDataRequest= dataMap.asPutDataRequest()
//        request.setUrgent()
//        val putTask : Task<DataItem> =Wearable.getDataClient(this).putDataItem(request)

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

                Log.d("도원", "DataItem saved: $result")
            } catch (cancellationException: CancellationException) {
                Log.d("도원", "Saving DataItem failed: ${cancellationException.localizedMessage}")
                throw cancellationException
            } catch (exception: Exception) {
                Log.d("도원", "Saving DataItem failed: $exception")
            }
        }



//        val putTask =dataClient.putDataItem(putDataReq).await()



//        try {
//
//            Thread(Runnable {
//                val dataItem = Tasks.await(putTask)
//                Log.d("도원","dataItem :  ${dataItem}")
//            }).start()
//
//        }catch (e : Exception){
//
//            Log.d("도원","e :  ${e.localizedMessage}")
//        }

//        putTask.addOnSuccessListener {
//            Toast.makeText(applicationContext,"send Successful!!",Toast.LENGTH_SHORT).show()
//        }
//
//        putTask.addOnCompleteListener {
//            Log.d("도원","result :   ${it.isSuccessful} | ${it.exception}")
//        }
//
//        putTask.addOnFailureListener {
//            Toast.makeText(applicationContext,"send Error!!",Toast.LENGTH_SHORT).show()
//        }
//
//        putTask.addOnCanceledListener {
//            Toast.makeText(applicationContext,"send Error!!",Toast.LENGTH_SHORT).show()
//        }

    }


    //bytes 를 파일로 만드는 method
    fun writeBytesToFile(theFile : File,bytes : ByteArray){
        var bos : BufferedOutputStream? = null
        try{
            val fos :FileOutputStream = FileOutputStream(theFile)
            bos = BufferedOutputStream(fos)
            bos.write(bytes)
        }catch (e : IOException){
            Log.d("도원"," 에러에러   ${e.localizedMessage}")
        }finally {
            try {
                bos?.flush()
                bos?.close()
            }catch (e1 :Exception){
                Log.d("도원"," 에러에러2   ${e1.localizedMessage}")
            }
        }
    }

    override fun onBackPressed() {
        if (System.currentTimeMillis()>backKeyPressedTime+2000){
            backKeyPressedTime= System.currentTimeMillis()
            Toast.makeText(this,"'뒤로'버튼을 한번 더 누르면 종료 됩니다.",Toast.LENGTH_SHORT).show()
            return;
        }
        if (System.currentTimeMillis()<=backKeyPressedTime+2000){
            this.finish()
            return;
        }
    }

    fun initView(){
        btnPlus=findViewById(R.id.btnPlus)
        btnHome=findViewById(R.id.btnHome)
        btnList=findViewById(R.id.btnList)
        ivSendWatch=findViewById(R.id.ivSendWatch)
        mainTopToolbar=findViewById(R.id.mainTopToolbar)

    }



}