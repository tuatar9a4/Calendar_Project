package com.devd.calenderbydw.utils

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import com.devd.calenderbydw.ui.widget.CalendarWidget
import com.devd.calenderbydw.database.TaskDatabaseHelper
import com.google.android.gms.tasks.Task
import com.google.android.gms.wearable.*
import java.io.File
import java.nio.file.Files

class WidgetUtils {

    companion object{

        fun updateWidgetData(context: Context){
            val widgetIntent = Intent(context, CalendarWidget::class.java)
            widgetIntent.action= AppWidgetManager.ACTION_APPWIDGET_UPDATE
            context.sendBroadcast(widgetIntent)
        }

        //데이터 전송하기 전에 DB를 byteArray형태로 변경
        fun changeDBToBytes(context: Context){
            //DB 경로를 구한 한다.
            val dbPath = TaskDatabaseHelper(context,"task.db",null,3).readableDatabase.path
            val dbFile = File(dbPath)
            val dbUri = Uri.fromFile(dbFile)
//        val realAsset = Asset.createFromUri(dbUri)
            val bytesFromDB = Files.readAllBytes(dbFile.toPath())
            val realAsset = Asset.createFromBytes(bytesFromDB)
            Log.d("도원","흠..$dbPath : ${File(dbPath).exists()}")
            sendDBData(realAsset,dbPath,context)
        }

        //Asset 으로 만든 데이터 보내기
        private fun sendDBData(sendData :Asset,dBPtah : String, context: Context){
            val dataMap : PutDataMapRequest = PutDataMapRequest.create("/taskdata")
            dataMap.dataMap.putAsset("taskDB",sendData)
            dataMap.dataMap.putString("taskDBPath",dBPtah)
            Log.d("도원","dBPtah :  ${dBPtah}")
            val request : PutDataRequest = dataMap.asPutDataRequest()
            request.setUrgent()
            val putTask : Task<DataItem> = Wearable.getDataClient(context).putDataItem(request)

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
    }
}