package com.dwstyle.calenderbydw.utils

import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.util.Log
import androidx.wear.tiles.TileService
import com.dwstyle.calenderbydw.CalendarTile
import com.dwstyle.calenderbydw.database.TaskDatabaseHelper
import com.google.android.gms.tasks.Tasks
import com.google.android.gms.wearable.Asset
import com.google.android.gms.wearable.Wearable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.*
import java.lang.Exception

class ReceiveDataToFile(context :Context,receiveAsset :Asset) {

    private val context =context
    private val receiveAsset=receiveAsset

    private lateinit var dbHelper : TaskDatabaseHelper
    private lateinit var database : SQLiteDatabase

    fun createDBFromSendData(){
        val dbAsset : ByteArray = bytesArrayFromAsset(receiveAsset)
        val newFile = File(context.getDatabasePath("wearTask.db").path)

        CoroutineScope(Dispatchers.IO).launch {
            writeBytesToFile(newFile,dbAsset)
        }

    }

    //asset 를 byte array로
    fun bytesArrayFromAsset(asset :Asset)  : ByteArray{
        Log.d("도원","assetInputStream.readBytes()");
        val assetInputStream : InputStream = Tasks.await(Wearable.getDataClient(context).getFdForAsset(asset)).inputStream
        return assetInputStream.readBytes()

    }

    //bytes 를 파일로 만드는 method
    fun writeBytesToFile(theFile : File,bytes : ByteArray){
        var bos : BufferedOutputStream? = null
        try{
            val fos : FileOutputStream = FileOutputStream(theFile,false)
            bos = BufferedOutputStream(fos)
            bos.write(bytes)
        }catch (e : IOException){
            Log.d("도원"," 에러에러   ${e.localizedMessage}")
        }finally {
            try {
                bos?.flush()
                bos?.close()
            }catch (e1 : Exception){
                Log.d("도원"," 에러에러2   ${e1.localizedMessage}")
            }
            TileService.getUpdater(context).requestUpdate(CalendarTile::class.java)
        }


        dbHelper= TaskDatabaseHelper(context,"wearTask.db",null,3);
        database=dbHelper.readableDatabase
//        try{
//            var c2: Cursor =
//                database.rawQuery("SELECT month,day,time,text,notice FROM myTaskTbl WHERE repeatY = 1", null);
//            while (c2.moveToNext()) {
//                Log.d("도원","month : ${c2.getColumnIndex("month")} | " +
//                        "day : ${c2.getColumnIndex("day")} | " +
//                        "time : ${c2.getColumnIndex("time")} | " +
//                        "text : ${c2.getColumnIndex("text")} | " +
//                        "notice : ${c2.getColumnIndex("notice")} | ")
//            }
//
//        }catch (e:Exception){
//
//
//        }

    }

}