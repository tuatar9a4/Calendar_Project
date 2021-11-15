package com.dwstyle.calenderbydw.service

import android.util.Log
import com.google.android.gms.tasks.Tasks
import com.google.android.gms.wearable.*
import java.io.InputStream

class DataLayerListenerService : WearableListenerService() {


    override fun onDataChanged(p0: DataEventBuffer) {
//        super.onDataChanged(p0)

        Log.d("도원","onDataChaned service????")
        for (event in p0){
            if (event.type == DataEvent.TYPE_CHANGED){
                val path = event.dataItem.uri.path
                if ("/taskdata".equals(path)){
                    val dataMapItem = DataMapItem.fromDataItem(event.dataItem)
                    val dbString :String? =dataMapItem.dataMap.getString("taskDBPath")
                    val dbAsset : ByteArray =dataMapItem.dataMap.getAsset("taskDB").let {
                            asset ->bytesArrayFromAsset(asset!!)
                    }
                    Log.d("도원", applicationContext.getDatabasePath("test").path)
                    Log.d("도원","${dbAsset}")
//                    val newFile =File(applicationContext.getDatabasePath("test").path)
//                    writeBytesToFile(newFile,dbAsset)

//                    CoroutineScope(Dispatchers.IO).launch {
//
//                    }

                }
            }
        }

    }

    fun bytesArrayFromAsset(asset : Asset)  : ByteArray{
        val assetInputStream : InputStream = Tasks.await(Wearable.getDataClient(applicationContext).getFdForAsset(asset))
            .inputStream

        return assetInputStream.readBytes()

    }
}