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
import com.google.android.gms.wearable.Asset
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
            makeTaskDialog.showDialog(calendarFragment.getSelectDateInfo(), {

                calendarFragment.createTask(makeTaskDialog.getInfo())
                makeTaskDialog.dismissDialog()
            }, {

                makeTaskDialog.dismissDialog()
            })

//            calendarFragment.createTask()
        }

        val builder : NotificationCompat.Builder =NotificationCompat.Builder(this).setContentTitle("HI? ").setContentText("Hello")
            .setLargeIcon(BitmapFactory.decodeResource(resources,R.drawable.check))
        val dbPath = TaskDatabaseHelper(applicationContext,"task.db",null,1).readableDatabase.path
        val newPath = TaskDatabaseHelper(applicationContext,"new.db",null,1).readableDatabase
        val cursor = newPath.rawQuery("Select * From myTaskTbl",null)
        Log.d("도원","! ${cursor.count}")
        while (cursor.moveToNext()){
            Log.d("도원","! ${cursor.getInt(cursor.getColumnIndex("_id"))}")
        }
        Log.d("도원","dbPath : ${dbPath}  ||| ${newPath}")
        val dbFile = File(dbPath)
        val dbUri = Uri.fromFile(dbFile)
        val realAsset = Asset.createFromUri(dbUri)
        val tes = Files.readAllBytes(dbFile.toPath())
        Log.d("도원"," ㅅㄷㄴ : ${tes}")
        val newFile =File("/data/user/0/com.dwstyle.calenderbydw/databases/newdsdsd.db")
        writeBytesToFile(newFile,tes)
    }

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