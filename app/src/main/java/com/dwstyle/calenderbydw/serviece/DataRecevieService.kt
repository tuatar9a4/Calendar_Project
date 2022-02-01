package com.dwstyle.calenderbydw.serviece

import android.util.Log
import com.dwstyle.calenderbydw.database.TaskDatabaseHelper
import com.dwstyle.calenderbydw.utils.WriteDBFile
import com.google.android.gms.wearable.DataEvent
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.WearableListenerService

class DataRecevieService : WearableListenerService() {

    override fun onDataChanged(p0: DataEventBuffer) {
        super.onDataChanged(p0)
        Log.d("도원","mobile onDataChaned service????")
        for (event in p0){
            if (event.type == DataEvent.TYPE_CHANGED){
                val path = event.dataItem.uri.path
                Log.d("도원","path ${path}")
                if ("/taskdata" == path){
                    val dataMapItem = DataMapItem.fromDataItem(event.dataItem)
                    val dbString :String? =dataMapItem.dataMap.getString("taskDBPath")
                    if (dataMapItem.dataMap.getAsset("taskDB")!=null){
                        val receiveDataToFile = WriteDBFile(applicationContext,dataMapItem.dataMap.getAsset("taskDB")!!)
                        receiveDataToFile.createDBFromSendData()
                    }
                }
            }
        }

    }
}