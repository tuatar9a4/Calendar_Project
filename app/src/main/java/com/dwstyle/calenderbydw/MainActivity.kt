package com.dwstyle.calenderbydw

import android.content.Intent
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
import com.dwstyle.calenderbydw.database.TaskDatabaseHelper
import com.dwstyle.calenderbydw.fragments.CalendarFragment
import com.dwstyle.calenderbydw.fragments.TaskListFragment
import com.dwstyle.calenderbydw.item.TaskItem
import com.google.android.gms.tasks.Task
import com.google.android.gms.wearable.*
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.lang.Exception
import java.nio.file.Files

class MainActivity : AppCompatActivity() {
    private val calendarFragment=CalendarFragment.newInstance()
    private val taskListFragment=TaskListFragment.newInstance()
    private lateinit var btnPlus : Button
    private lateinit var btnList :Button
    private lateinit var btnHome :Button
    private lateinit var ivSendWatch :ImageView
    private lateinit var resultLauncher : ActivityResultLauncher<Intent>

    private var backKeyPressedTime:Long=0;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initView()

        supportFragmentManager.beginTransaction()
            .add(R.id.flmainFragment,calendarFragment)
            .add(R.id.flmainFragment,taskListFragment)
            .hide(taskListFragment)
            .commit()

        clickFunction()




    }

    //clickFun
    private fun clickFunction(){
        //달력화면 이동
        btnHome.setOnClickListener {
//            supportFragmentManager.beginTransaction().replace(R.id.flmainFragment,calendarFragment).commit()
            val transaction =supportFragmentManager.beginTransaction()
            transaction.hide(taskListFragment)
            transaction.show(calendarFragment)
            transaction.commit()


        }

        //TaskList 화면 이동
        btnList.setOnClickListener {
//            supportFragmentManager.beginTransaction().replace(R.id.flmainFragment,taskListFragment).commit()
            val transaction =supportFragmentManager.beginTransaction()
            transaction.hide(calendarFragment)
            transaction.show(taskListFragment)
            transaction.commit()
        }

        //일정 생성으로 이동
        btnPlus.setOnClickListener {
            val intent = Intent(applicationContext,CreateTaskActivity::class.java)
            intent.putExtra("type","create")
            intent.putExtra("dateInfo",calendarFragment.getSelectDateInfo())
            resultLauncher.launch(intent)
        }

        resultLauncher=registerForActivityResult(ActivityResultContracts.StartActivityForResult(),
            ActivityResultCallback {
                //Task 등록 데이터 받는 곳
                Log.d("도원","recevie Data ? ")
                if (it.resultCode == RESULT_OK){
                    val intent = it.data
                    if (intent?.getParcelableExtra<TaskItem>("createItem")!=null){
                        calendarFragment.createTask(intent.getParcelableExtra<TaskItem>("createItem")!!)
                    }
                }
            })


        //워치로 데이터 전송시키기
        ivSendWatch.setOnClickListener {
            changeDBToBytes()
        }



    }


    //데이터 전송하기 전에 DB를 byteArray형태로 변경
    private fun changeDBToBytes(){
        //DB 경로를 구한 한다.
        val dbPath = TaskDatabaseHelper(applicationContext,"task.db",null,2).readableDatabase.path
        val dbFile = File(dbPath)
        val dbUri = Uri.fromFile(dbFile)
//        val realAsset = Asset.createFromUri(dbUri)
        val bytesFromDB = Files.readAllBytes(dbFile.toPath())
        val realAsset = Asset.createFromBytes(bytesFromDB)
        sendDBData(realAsset,dbPath)
    }


    //Asset 으로 만든 데이터 보내기
    private fun sendDBData(sendData :Asset,dBPtah : String){
        val dataMap : PutDataMapRequest = PutDataMapRequest.create("/taskdata")
        dataMap.dataMap.putAsset("taskDB",sendData)
        dataMap.dataMap.putString("taskDBPath",dBPtah)
        Log.d("도원","dBPtah :  ${dBPtah}")
        val request : PutDataRequest= dataMap.asPutDataRequest()
        request.setUrgent()
        val putTask : Task<DataItem> =Wearable.getDataClient(this).putDataItem(request)

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
        putTask.addOnSuccessListener {
            Log.d("도원","isSuccessful :  ${putTask.isSuccessful}")
        }

        putTask.addOnCompleteListener {
            Log.d("도원","result :  ${putTask.result}")
        }

        putTask.addOnFailureListener {
            Log.d("도원","exception :  ${putTask.exception}")
        }

        putTask.addOnCanceledListener {
            Log.d("도원","exception :  ${putTask.isCanceled}")
        }

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

    }



}