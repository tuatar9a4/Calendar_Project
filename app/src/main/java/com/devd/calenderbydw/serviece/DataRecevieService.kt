package com.devd.calenderbydw.serviece

import android.util.Log
import com.devd.calenderbydw.utils.WriteDBFile
import com.google.android.gms.wearable.DataEvent
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.WearableListenerService
import timber.log.Timber

class DataRecevieService : WearableListenerService() {

    override fun onDataChanged(p0: DataEventBuffer) {
        super.onDataChanged(p0)
        for (event in p0){
            if (event.type == DataEvent.TYPE_CHANGED){
                val path = event.dataItem.uri.path
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