package com.devd.calenderbydw.service

import android.database.sqlite.SQLiteDatabase
import android.util.Log
import com.devd.calenderbydw.database.TaskDatabaseHelper
import com.devd.calenderbydw.utils.ReceiveDataToFile
import com.google.android.gms.wearable.*

class DataLayerListenerService : WearableListenerService() {

    private lateinit var dbHelper : TaskDatabaseHelper
    private lateinit var database : SQLiteDatabase


    override fun onDataChanged(p0: DataEventBuffer) {
        super.onDataChanged(p0)
        dbHelper= TaskDatabaseHelper(applicationContext,"wearTask.db",null,3);
        database=dbHelper.readableDatabase
        Log.d("도원","onDataChaned service????")
        for (event in p0){
            if (event.type == DataEvent.TYPE_CHANGED){
                val path = event.dataItem.uri.path
                Log.d("도원","path ${path}")
                if ("/taskdata" == path){
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