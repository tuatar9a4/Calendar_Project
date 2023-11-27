package com.devd.calenderbydw.utils

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.util.Log
import com.devd.calenderbydw.data.local.AppDatabase
import com.devd.calenderbydw.data.local.entity.CalendarDayEntity
import com.devd.calenderbydw.data.local.entity.TaskDBEntity
import com.devd.calenderbydw.ui.widget.CalendarWidget
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.android.gms.wearable.*
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import java.io.File
import java.nio.file.Files
import java.time.Instant

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
            val dbPath = context.getDatabasePath("calendar_db").absolutePath
            val dbFile = File(dbPath)
            val bytesFromDB = Files.readAllBytes(dbFile.toPath())
            val realAsset = Asset.createFromBytes(bytesFromDB)
            CoroutineScope(Dispatchers.IO).launch {
                sendDBData(realAsset,dbPath,context)
            }
        }

        //Asset 으로 만든 데이터 워치 보내기
        private suspend fun sendDBData(sendData :Asset,dBPtah : String, context: Context){
            try{
                val datab =  AppDatabase.buildDatabase(context).taskDao().getAllTaskForWidget()
//                Gson().fromJson(Gson().toJson(datab), Array<TaskDBEntity>::class.java).toList()
                val dataMap : PutDataMapRequest = PutDataMapRequest.create("/taskdata")
                dataMap.dataMap.putAsset("taskDB",sendData)
                dataMap.dataMap.putString("taskDBPath",Gson().toJson(datab))
                dataMap.dataMap.putLong("KEY", Instant.now().epochSecond)
                val request : PutDataRequest = dataMap.asPutDataRequest()
                request.setUrgent()
                val putTask  = Wearable.getDataClient(context).putDataItem(request)
                Log.d("도원","dBPtah :  ${request}")
                putTask.addOnSuccessListener {
                    Log.d("도원","isSuccessful :  ${putTask.isSuccessful}")
                }

                putTask.addOnCompleteListener {
                    try{
                        Log.d("도원","result :  ${putTask.result}")
                    }catch (e :Exception){
                        Log.d("도원","Exception localizedMessage :  ${e.localizedMessage}")
                    }
                }

                putTask.addOnFailureListener {
                    Log.d("도원","exception Failure:  ${putTask.exception}")
                }

                putTask.addOnCanceledListener {
                    Log.d("도원","exception isCanceled:  ${putTask.isCanceled}")
                }
            }catch (e :Exception){
                Log.d("도원","exception :  ${e.localizedMessage}")
            }catch (e : ApiException){
                Log.d("도원","ApiException :  ${e.localizedMessage}")
            }

        }
    }
}