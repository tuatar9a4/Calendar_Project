package com.dwstyle.calenderbydw.service

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.util.Log
import android.widget.Toast
import com.dwstyle.calenderbydw.database.TaskDatabaseHelper
import com.dwstyle.calenderbydw.utils.ReceiveDataToFile
import com.google.android.gms.tasks.Tasks
import com.google.android.gms.wearable.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.*
import java.lang.Exception
import java.time.Duration

class DataLayerListenerService : WearableListenerService() {

    private lateinit var dbHelper : TaskDatabaseHelper
    private lateinit var database : SQLiteDatabase


    override fun onDataChanged(p0: DataEventBuffer) {
//        super.onDataChanged(p0)
        dbHelper= TaskDatabaseHelper(applicationContext,"wearTask.db",null,2);
        database=dbHelper.readableDatabase
        Log.d("도원","onDataChaned service????")
        for (event in p0){
            if (event.type == DataEvent.TYPE_CHANGED){
                val path = event.dataItem.uri.path
                Log.d("도원","path ${path}")
                if ("/taskdata".equals(path)){
                    val dataMapItem = DataMapItem.fromDataItem(event.dataItem)
                    val dbString :String? =dataMapItem.dataMap.getString("taskDBPath")
                    if (dataMapItem.dataMap.getAsset("taskDB")!=null){
                        val receiveDataToFile = ReceiveDataToFile(applicationContext,dataMapItem.dataMap.getAsset("taskDB")!!)
                        receiveDataToFile.createDBFromSendData()
                    }
                }
            }
        }

    }

}