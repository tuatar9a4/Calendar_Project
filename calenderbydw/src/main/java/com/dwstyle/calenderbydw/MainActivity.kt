package com.dwstyle.calenderbydw

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import com.dwstyle.calenderbydw.database.TaskDatabaseHelper
import com.dwstyle.calenderbydw.databinding.ActivityMainBinding
import com.dwstyle.calenderbydw.utils.ReceiveDataToFile
import com.google.android.gms.tasks.Tasks
import com.google.android.gms.wearable.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.*
import java.lang.Exception
import java.util.concurrent.*

class MainActivity : ComponentActivity() ,DataClient.OnDataChangedListener{

    private lateinit var binding: ActivityMainBinding

//    private lateinit var tileUiClient :TileUiClient


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        Log.d("도원","WearApp Open ")
//        타일 새로 고침
//        TileService.getUpdater(applicationContext).requestUpdate(CalendarTile::class.java)
//        val rootLayout = findViewById<FrameLayout>(R.id.tile_container)
//        tileUiClient = TileUiClient(
//            context = this,
//            component = ComponentName(this,CalendarTile::class.java),
//            parentView = rootLayout
//        )
//        tileUiClient.connect()

    }

    override fun onResume() {
        super.onResume()
        Log.d("도원","WearApp onResume ")
//        Wearable.getDataClient(this).addListener(this)
    }

    override fun onPause() {
        super.onPause()
//        Wearable.getDataClient(this).removeListener(this)
    }

    override fun onDestroy() {
        super.onDestroy()
//        tileUiClient.close()
    }
    override fun onDataChanged(p0: DataEventBuffer) {
       Log.d("도원","onDataChanged main")
       Log.d("도원","p0 ${p0.count}")
        for (event in p0){
           Log.d("도원","event ${event.type}")
            if (event.type == DataEvent.TYPE_CHANGED){
                 Log.d("도원","event.dataItem.uri ${event.dataItem.uri}")
                 Log.d("도원","event.dataItem.uri.path ${event.dataItem.uri.path}")
                val path = event.dataItem.uri.path
                 Log.d("도원","path ${path}")
                if ("/taskdata".equals(path)){
                    val dataMapItem =DataMapItem.fromDataItem(event.dataItem)
                    val dbString :String? =dataMapItem.dataMap.getString("taskDBPath")
                    binding.tvText.setText("${dbString},")
//                    val dbAsset : ByteArray =dataMapItem.dataMap.getAsset("taskDB").let {
//                            asset ->bytesArrayFromAsset(asset)
//                    }
//                    if (dataMapItem.dataMap.getAsset("taskDB")!=null){
//                        val receiveDataToFile =ReceiveDataToFile(applicationContext,dataMapItem.dataMap.getAsset("taskDB")!!)
//                        receiveDataToFile.createDBFromSendData()
//                    }
                }
            }
        }
    }

    fun bytesArrayFromAsset(asset :Asset)  : ByteArray{
        val assetInputStream : InputStream = Tasks.await(Wearable.getDataClient(applicationContext).getFdForAsset(asset)).inputStream
        return assetInputStream.readBytes()

    }
}