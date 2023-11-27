package com.devd.calenderbydw.service

import android.util.Log
import androidx.wear.tiles.TileService
import com.devd.calenderbydw.CalendarTile
import com.devd.calenderbydw.data.dao.TaskDao
import com.devd.calenderbydw.data.local.entity.TaskDBEntity
import com.devd.calenderbydw.database.TaskDatabase
import com.devd.calenderbydw.utils.ReceiveDataToFile
import com.google.android.gms.wearable.DataEvent
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.WearableListenerService
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class DataLayerListenerService : WearableListenerService() {

    override fun onDataChanged(p0: DataEventBuffer) {
        super.onDataChanged(p0)
        for (event in p0){
            if (event.type == DataEvent.TYPE_CHANGED){
                val path = event.dataItem.uri.path
                Log.d("도원","path ${path}")
                if ("/taskdata" == path){
                    val dataMapItem = DataMapItem.fromDataItem(event.dataItem)
                    Log.d("도원","arra ::  ${dataMapItem.dataMap.getString("taskDBPath")}")
                    val arra=  Gson().fromJson(dataMapItem.dataMap.getString("taskDBPath"), Array<TaskDBEntity>::class.java).toList()
                    Log.d("도원","arra ::  ${arra}")
                    CoroutineScope(Dispatchers.IO).launch {
                        TaskDatabase.buildDatabase(applicationContext).taskDao().insertTaskItemList(arra).run {
                            TileService.getUpdater(applicationContext).requestUpdate(CalendarTile::class.java)
                        }
                    }
//                    if (dataMapItem.dataMap.getAsset("taskDB")!=null){
//                        val receiveDataToFile = ReceiveDataToFile(applicationContext,dataMapItem.dataMap.getAsset("taskDB")!!)
//                        receiveDataToFile.createDBFromSendData()
//                    }
                }
            }
        }

    }

}