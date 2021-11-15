package com.dwstyle.calenderbydw

import android.annotation.SuppressLint
import android.app.Notification
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.core.app.NotificationCompat
import com.dwstyle.calenderbydw.database.TaskDatabaseHelper
import com.dwstyle.calenderbydw.fragments.CalendarFragment
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
    @SuppressLint("SdCardPath")
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
//            makeTaskDialog.showDialog(calendarFragment.getSelectDateInfo(), {
//
//                calendarFragment.createTask(makeTaskDialog.getInfo())
//                makeTaskDialog.dismissDialog()
//            }, {
//
//                makeTaskDialog.dismissDialog()
//            })

            //----테스트
            changeDBToBytes()


        }

//        val dbPath = TaskDatabaseHelper(applicationContext,"task.db",null,1).readableDatabase.path
//        val newPath = TaskDatabaseHelper(applicationContext,"new.db",null,1).readableDatabase
//        val cursor = newPath.rawQuery("Select * From myTaskTbl",null)
//        Log.d("도원","! ${cursor.count}")
//        while (cursor.moveToNext()){
//            Log.d("도원","! ${cursor.getInt(cursor.getColumnIndex("_id"))}")
//        }
//        Log.d("도원","dbPath : ${dbPath}  |||")
//        val dbFile = File(dbPath)
//        val dbUri = Uri.fromFile(dbFile)
//        val realAsset = Asset.createFromUri(dbUri)
//        val tes = Files.readAllBytes(dbFile.toPath())
//        Log.d("도원"," ㅅㄷㄴ : ${tes}")
//        val newFile =File("/data/user/0/com.dwstyle.calenderbydw/databases/newdsdsd.db")
//        writeBytesToFile(newFile,tes)


    }

    //데이터 전송하기 전에 DB를 byteArray형태로 변경
    fun changeDBToBytes(){
        //DB 경로를 구한 한다.
        val dbPath = TaskDatabaseHelper(applicationContext,"task.db",null,1).readableDatabase.path
        val dbFile = File(dbPath)
        val dbUri = Uri.fromFile(dbFile)
//        val realAsset = Asset.createFromUri(dbUri)
        val bytesFromDB = Files.readAllBytes(dbFile.toPath())
        val realAsset = Asset.createFromBytes(bytesFromDB)

        sendDBData(realAsset,dbPath)
    }


    //Asset 으로 만든 데이터 보내기
    fun sendDBData(sendData :Asset,dBPtah : String){
        val dataMap : PutDataMapRequest = PutDataMapRequest.create("/taskdata")
        dataMap.dataMap.putAsset("taskDB",sendData)
        dataMap.dataMap.putString("taskDBPath",dBPtah)

        val request : PutDataRequest= dataMap.asPutDataRequest()

         request.setUrgent()

        val putTask : Task<DataItem> =Wearable.getDataClient(this).putDataItem(request)

        try {

            Thread(Runnable {
                val dataItem = Tasks.await(putTask)
                Log.d("도원","dataItem :  ${dataItem}")
            }).start()

        }catch (e : Exception){

            Log.d("도원","e :  ${e.localizedMessage}")
        }
        putTask.addOnSuccessListener {
            Log.d("도원","isSuccessful :  ${putTask.isSuccessful}")
        }

        putTask.addOnCompleteListener {
         Log.d("도원","result :  ${putTask.result}")
        }

        putTask.addOnFailureListener {
            Log.d("도원","exception :  ${putTask.exception}")

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
    }



}