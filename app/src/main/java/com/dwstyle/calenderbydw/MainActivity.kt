package com.dwstyle.calenderbydw

import android.annotation.SuppressLint
import android.app.Notification
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.NotificationCompat
import com.dwstyle.calenderbydw.database.TaskDatabaseHelper
import com.dwstyle.calenderbydw.fragments.CalendarFragment
import com.dwstyle.calenderbydw.item.TaskItem
import com.dwstyle.calenderbydw.utils.MakeTaskDialog
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.android.gms.wearable.*
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.lang.Exception
import java.nio.file.Files

class MainActivity : AppCompatActivity() {
    private val calendarFragment=CalendarFragment.newInstance()
    private lateinit var plus : Button
    private lateinit var send :Button
    private lateinit var resultLauncher : ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initView()
        supportFragmentManager.beginTransaction()
            .replace(R.id.flmainFragment,calendarFragment)
            .commit()

        resultLauncher=registerForActivityResult(ActivityResultContracts.StartActivityForResult(),
        ActivityResultCallback {
            if (it.resultCode == RESULT_OK){
                val intent = it.data
                if (intent?.getParcelableExtra<TaskItem>("createItem")!=null){
                    calendarFragment.createTask(intent.getParcelableExtra<TaskItem>("createItem")!!)
                }
            }
        })

        plus.setOnClickListener {
            val intent = Intent(applicationContext,CreateTaskActivity::class.java)
            intent.putExtra("dateInfo",calendarFragment.getSelectDateInfo())
            resultLauncher.launch(intent)
        }

        send.setOnClickListener(View.OnClickListener {
            changeDBToBytes()
        })




    }

    //데이터 전송하기 전에 DB를 byteArray형태로 변경
    fun changeDBToBytes(){
        //DB 경로를 구한 한다.
        Log.d("도원","changeDBToBytes1 : ")
        val dbPath = TaskDatabaseHelper(applicationContext,"task.db",null,2).readableDatabase.path
        val dbFile = File(dbPath)
        val dbUri = Uri.fromFile(dbFile)
//        val realAsset = Asset.createFromUri(dbUri)
        val bytesFromDB = Files.readAllBytes(dbFile.toPath())
        val realAsset = Asset.createFromBytes(bytesFromDB)
        Log.d("도원","changeDBToBytes2 : ")
        sendDBData(realAsset,dbPath)
    }


    //Asset 으로 만든 데이터 보내기
    fun sendDBData(sendData :Asset,dBPtah : String){
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


    fun initView(){
        plus=findViewById(R.id.plus)
        send=findViewById(R.id.send)
    }



}